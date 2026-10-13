package com.cylonid.nativealpha.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReleaseNotesRepositoryTest {
    @Test
    fun testJsonDeserialization() {
        val sampleJson =
            """
            {
                "versionName": "1.6.4",
                "releaseUrl": "https://github.com/JoestarLabs/NativeAlpha/releases/tag/v1.6.4",
                "sections": [
                    {
                        "title": "Features",
                        "items": ["Item A", "Item B"]
                    },
                    {
                        "title": "Bug Fixes",
                        "items": ["Fix 1"]
                    }
                ]
            }
            """.trimIndent()

        val releaseNotes = Gson().fromJson(sampleJson, ReleaseNotes::class.java)

        assertNotNull(releaseNotes)
        assertEquals("1.6.4", releaseNotes.versionName)
        assertEquals("https://github.com/JoestarLabs/NativeAlpha/releases/tag/v1.6.4", releaseNotes.releaseUrl)
        assertEquals(2, releaseNotes.sections.size)
        assertEquals("Features", releaseNotes.sections[0].title)
        assertEquals(2, releaseNotes.sections[0].items.size)
        assertEquals("Bug Fixes", releaseNotes.sections[1].title)
        assertEquals(1, releaseNotes.sections[1].items.size)
    }

    @Test
    fun testFallbackWhenAssetNotFound() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = ReleaseNotesRepository.load(context)

        assertNotNull(result)
        assertTrue(result.versionName.isNotEmpty())
        assertTrue(result.releaseUrl.startsWith("https://github.com/"))
    }
}
