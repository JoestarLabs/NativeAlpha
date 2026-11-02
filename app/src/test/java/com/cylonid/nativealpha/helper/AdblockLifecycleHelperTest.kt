package com.cylonid.nativealpha.helper

import android.app.Activity
import com.cylonid.nativealpha.model.DataManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdblockLifecycleHelperTest {
    private lateinit var activity: Activity
    private lateinit var helper: AdblockLifecycleHelper

    @Before
    fun setup() {
        val controller = Robolectric.buildActivity(androidx.appcompat.app.AppCompatActivity::class.java)
        controller.get().setTheme(com.cylonid.nativealpha.R.style.AppTheme)
        activity =
            controller
                .create()
                .start()
                .resume()
                .get()
        helper = AdblockLifecycleHelper(activity)
        DataManager.getInstance().hasAdblockCrashed = false
    }

    @Test
    fun testTrySyncOperationSuccess() {
        var executed = false
        helper.trySyncOperation {
            executed = true
        }
        assertTrue("Callback should be executed", executed)
        assertFalse("hasAdblockCrashed should be false after success", DataManager.getInstance().hasAdblockCrashed)
    }

    @Test
    fun testTrySyncOperationCatchesException() {
        helper.trySyncOperation {
            throw RuntimeException("Simulated native or engine crash")
        }
        assertTrue("hasAdblockCrashed should be set to true on failure", DataManager.getInstance().hasAdblockCrashed)
    }

    @Test
    fun testTrySyncOperationRecoversFromPreviousCrash() {
        DataManager.getInstance().hasAdblockCrashed = true

        var executed = false
        helper.trySyncOperation {
            executed = true
        }

        assertTrue("Callback should be executed even if previous launch crashed", executed)
        assertFalse("hasAdblockCrashed should be reset to false after recovery", DataManager.getInstance().hasAdblockCrashed)

        val dialog =
            org.robolectric.shadows.ShadowDialog
                .getLatestDialog()
        assertTrue("Warning dialog should have been shown", dialog != null && dialog.isShowing)
    }

    @Test
    fun testBeforeAndAfterAdblockOperation() {
        var executed = false
        helper.beforeAdblockOperation {
            executed = true
        }
        assertTrue("Callback should be executed", executed)
        assertFalse("hasAdblockCrashed should remain false if callback succeeds", DataManager.getInstance().hasAdblockCrashed)

        helper.afterAdblockOperation()
        assertFalse(DataManager.getInstance().hasAdblockCrashed)
    }

    @Test
    fun testBeforeAdblockOperationHandlesException() {
        helper.beforeAdblockOperation {
            throw IllegalStateException("Failure during beforeAdblockOperation")
        }
        assertTrue("hasAdblockCrashed should be set to true on error", DataManager.getInstance().hasAdblockCrashed)
    }

    @Test
    fun testSanitizeFilterDownloadStatesConvertsRunningStates() {
        val prefs = activity.getSharedPreferences(AdblockLifecycleHelper.FILTER_PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val initialJson =
            """
            {
                "filter1": {
                    "url": "https://example.com/rules1.txt",
                    "name": "List 1",
                    "downloadState": "ENQUEUED",
                    "filtersCount": 150000
                },
                "filter2": {
                    "url": "https://example.com/rules2.txt",
                    "name": "List 2",
                    "downloadState": "DOWNLOADING",
                    "filtersCount": 0
                },
                "filter3": {
                    "url": "https://example.com/rules3.txt",
                    "name": "List 3",
                    "downloadState": "SUCCESS",
                    "filtersCount": 500
                }
            }
            """.trimIndent()
        prefs.edit().putString(AdblockLifecycleHelper.KEY_FILTER_MAP, initialJson).commit()

        AdblockLifecycleHelper.sanitizeFilterDownloadStates(activity)

        val sanitizedJson = prefs.getString(AdblockLifecycleHelper.KEY_FILTER_MAP, "") ?: ""
        val jsonObject = org.json.JSONObject(sanitizedJson)

        val filter1 = jsonObject.getJSONObject("filter1")
        val filter2 = jsonObject.getJSONObject("filter2")
        val filter3 = jsonObject.getJSONObject("filter3")

        org.junit.Assert.assertEquals("NONE", filter1.getString("downloadState"))
        org.junit.Assert.assertEquals("FAILED", filter2.getString("downloadState"))
        org.junit.Assert.assertEquals("SUCCESS", filter3.getString("downloadState"))
    }

    @Test
    fun testInitAdFilterSucceeds() {
        val filter = AdblockLifecycleHelper.initAdFilter(activity)
        org.junit.Assert.assertNotNull("AdFilter should be initialized", filter)
    }
}
