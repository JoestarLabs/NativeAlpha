package com.cylonid.nativealpha.helper

import com.cylonid.nativealpha.model.AdblockConfig
import com.cylonid.nativealpha.util.DateUtils
import io.github.edsuns.adfilter.AdFilter
import io.github.edsuns.adfilter.Filter

internal class AdblockProviderApiHelper(
    private val adFilterProvider: AdFilter,
) {
    fun synchronizeAdblockProviderWithSettings(settings: List<AdblockConfig>) {
        val map = transformToMapWithUrlKey(adFilterProvider.viewModel.filters.value ?: emptyMap())
        for (config: AdblockConfig in settings) {
            var setFilter = map[config.value]
            if (setFilter == null) {
                setFilter = adFilterProvider.viewModel.addFilter(config.label, config.value)
                adFilterProvider.viewModel.download(setFilter.id)
            }
            if (DateUtils.isOlderThanDays(setFilter.updateTime, 10)) {
                adFilterProvider.viewModel.download(setFilter.id)
            }
        }
        for ((_, filter) in map) {
            val existingConfig = settings.find { it.value == filter.url }
            if (existingConfig == null) {
                adFilterProvider.viewModel.removeFilter(filter.id)
            }
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
