package com.cylonid.nativealpha.helper

import com.cylonid.nativealpha.model.AdblockConfig
import com.cylonid.nativealpha.util.DateUtils
import io.github.edsuns.adfilter.AdFilter
import io.github.edsuns.adfilter.Filter
import timber.log.Timber

internal class AdblockProviderApiHelper(
    private val adFilterProvider: AdFilter?,
) {
    fun synchronizeAdblockProviderWithSettings(settings: List<AdblockConfig>) {
        val provider = adFilterProvider ?: return
        try {
            val map = transformToMapWithUrlKey(provider.viewModel.filters.value ?: emptyMap())
            for (config: AdblockConfig in settings) {
                var setFilter = map[config.value]
                if (setFilter == null) {
                    setFilter = provider.viewModel.addFilter(config.label, config.value)
                    provider.viewModel.download(setFilter.id)
                }
                if (DateUtils.isOlderThanDays(setFilter.updateTime, 10)) {
                    provider.viewModel.download(setFilter.id)
                }
            }
            for ((_, filter) in map) {
                val existingConfig = settings.find { it.value == filter.url }
                if (existingConfig == null) {
                    provider.viewModel.removeFilter(filter.id)
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "Error synchronizing adblock settings with provider")
        }
    }

    private fun transformToMapWithUrlKey(originalMap: Map<String, Filter>): Map<String, Filter> {
        val urlBasedMap: HashMap<String, Filter> = HashMap()
        for ((_, value) in originalMap) {
            urlBasedMap[value.url] = value
        }
        return urlBasedMap
    }
}
