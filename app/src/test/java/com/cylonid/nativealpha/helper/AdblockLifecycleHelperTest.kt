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
}
