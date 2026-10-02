package com.cylonid.nativealpha.model

import com.cylonid.nativealpha.util.Const
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdblockConfigTest {
    @Test
    fun testDefaultAdblockConfigsAreValid() {
        val defaults = Const.getDefaultAdBlockConfig()
        assertTrue("Default adblock list should not be empty", defaults.isNotEmpty())
        assertTrue("Default adblock list size should be <= 8", defaults.size <= 8)

        defaults.forEach { config ->
            assertTrue("Label should not be empty", config.label.isNotBlank())
            assertTrue(
                "URL should start with http:// or https://",
                config.value.startsWith("http://") || config.value.startsWith("https://"),
            )
        }
    }

    @Test
    fun testAdblockDuplicateDetection() {
        val list =
            mutableListOf(
                AdblockConfig("EasyList", "https://easylist.to/easylist/easylist.txt"),
            )

        val duplicateUrl = "https://easylist.to/easylist/easylist.txt"
        val isDuplicate = list.any { it.value == duplicateUrl }
        assertTrue("Should detect duplicate URL", isDuplicate)

        val newUrl = "https://example.com/custom.txt"
        val isNotDuplicate = list.any { it.value == newUrl }
        assertFalse("Should not flag new URL as duplicate", isNotDuplicate)
    }

    @Test
    fun testAdblockAddAndRemoveWithLimit() {
        val list = mutableListOf<AdblockConfig>()
        for (i in 1..8) {
            list.add(AdblockConfig("Filter $i", "https://example.com/filter$i.txt"))
        }

        assertEquals(8, list.size)

        // Removing item
        val removed = list.removeAt(0)
        assertEquals("Filter 1", removed.label)
        assertEquals(7, list.size)

        // Restoring item
        list.add(0, removed)
        assertEquals(8, list.size)
    }
}
