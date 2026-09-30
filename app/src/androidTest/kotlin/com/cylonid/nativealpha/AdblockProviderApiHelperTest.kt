package com.cylonid.nativealpha

import com.cylonid.nativealpha.helper.AdblockProviderApiHelper
import com.cylonid.nativealpha.model.AdblockConfig
import io.github.edsuns.adfilter.AdFilter
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AdblockProviderApiHelperTest {
    private lateinit var adblockProviderApiHelper: AdblockProviderApiHelper
    private lateinit var adFilterProvider: AdFilter

    @Before
    fun setUp() {
        adFilterProvider = AdFilter.create(mock())
        adblockProviderApiHelper = AdblockProviderApiHelper(adFilterProvider)
    }

    private fun getActiveUrls(): List<String> =
        adFilterProvider.viewModel.filters.value
            ?.values
            ?.map { it.url } ?: emptyList()

    @Test
    fun shouldInitEmptyRuntimeConfig() {
        val ourConfig =
            listOf(
                AdblockConfig("Test", "https://someendpoint.xyzabc"),
                AdblockConfig("Test2", "https://someendpoint.xyzdef"),
            )
        adblockProviderApiHelper.synchronizeAdblockProviderWithSettings(ourConfig)
        Assert.assertEquals(
            listOf("https://someendpoint.xyzabc", "https://someendpoint.xyzdef"),
            getActiveUrls(),
        )
    }

    @Test
    fun shouldAddToExistingRuntimeConfig() {
        adFilterProvider.viewModel.addFilter("Test", "https://someendpoint.xyzabc")
        val ourConfig =
            listOf(
                AdblockConfig("Test", "https://someendpoint.xyzabc"),
                AdblockConfig("Test2", "https://someendpoint.xyzdef"),
            )
        adblockProviderApiHelper.synchronizeAdblockProviderWithSettings(ourConfig)
        Assert.assertEquals(
            listOf("https://someendpoint.xyzabc", "https://someendpoint.xyzdef"),
            getActiveUrls(),
        )
    }

    @Test
    fun shouldRemoveDeletedRuntimeConfig() {
        val ourConfig =
            listOf(
                AdblockConfig("Test", "https://someendpoint.xyzabc"),
                AdblockConfig("Test2", "https://someendpoint.xyzdef"),
            )
        adblockProviderApiHelper.synchronizeAdblockProviderWithSettings(ourConfig)

        adblockProviderApiHelper.synchronizeAdblockProviderWithSettings(emptyList())
        Assert.assertEquals(
            emptyList<AdblockConfig>(),
            getActiveUrls(),
        )
    }
}
