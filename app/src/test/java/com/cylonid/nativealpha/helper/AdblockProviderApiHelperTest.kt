package com.cylonid.nativealpha.helper

import com.cylonid.nativealpha.model.AdblockConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdblockProviderApiHelperTest {
    @Test
    fun testSynchronizeWithNullProviderDoesNotCrash() {
        val helper = AdblockProviderApiHelper(null)
        val configs = listOf(AdblockConfig("EasyList", "https://easylist.to/easylist/easylist.txt"))
        // Must complete without any exceptions
        helper.synchronizeAdblockProviderWithSettings(configs)
        helper.synchronizeAdblockProviderWithSettings(emptyList())
    }
}
