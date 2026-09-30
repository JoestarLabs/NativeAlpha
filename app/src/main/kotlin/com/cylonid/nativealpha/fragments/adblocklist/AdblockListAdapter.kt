package com.cylonid.nativealpha.fragments.adblocklist

import android.view.View
import android.widget.TextView
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.model.AdblockConfig
import com.cylonid.nativealpha.model.DataManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter

class AdblockListAdapter(
    dataSet: List<AdblockConfig>,
) : DragDropSwipeAdapter<AdblockConfig, AdblockListAdapter.ViewHolder>(dataSet) {
    class ViewHolder(
        webAppLayout: View,
    ) : DragDropSwipeAdapter.ViewHolder(webAppLayout) {
        val titleView: TextView = itemView.findViewById(R.id.adblock_title)
        val subtitleView: TextView = itemView.findViewById(R.id.adblock_subtitle)
    }

    override fun getViewHolder(itemView: View) = ViewHolder(itemView)

    override fun onBindViewHolder(
        item: AdblockConfig,
        viewHolder: ViewHolder,
        position: Int,
    ) {
        viewHolder.titleView.text = item.label
        viewHolder.subtitleView.text = item.value
    }

    override fun canBeDragged(
        item: AdblockConfig,
        viewHolder: ViewHolder,
        position: Int,
    ): Boolean = false

    fun updateAdblockList() {
        dataSet =
            DataManager
                .getInstance()
                .settings.globalWebApp.adBlockSettings
    }

    override fun getViewToTouchToStartDraggingItem(
        item: AdblockConfig,
        viewHolder: ViewHolder,
        position: Int,
    ) = viewHolder.titleView
}
