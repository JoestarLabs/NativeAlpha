package com.cylonid.nativealpha.fragments.webapplist

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.util.WebViewLauncher.startWebView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter

class WebAppListAdapter(
    dataSet: List<WebApp> = emptyList(),
    private val activityOfFragment: Activity,
) : DragDropSwipeAdapter<WebApp, WebAppListAdapter.ViewHolder>(dataSet) {
    class ViewHolder(
        webAppLayout: View,
    ) : DragDropSwipeAdapter.ViewHolder(webAppLayout) {
        val dragAnchor: ImageView = itemView.findViewById(R.id.dragAnchor)
        val titleView: TextView = itemView.findViewById(R.id.btnWebAppTitle)
    }

    override fun getViewHolder(itemView: View) = ViewHolder(itemView)

    override fun onBindViewHolder(
        item: WebApp,
        viewHolder: ViewHolder,
        position: Int,
    ) {
        viewHolder.titleView.text = item.title
        viewHolder.titleView.setOnClickListener {
            openWebView(
                item,
            )
        }
    }

    fun updateWebAppList() {
        dataSet = DataManager.getInstance().activeWebsites
    }

    private fun openWebView(webapp: WebApp) {
        startWebView(webapp, activityOfFragment)
    }

    override fun getViewToTouchToStartDraggingItem(
        item: WebApp,
        viewHolder: ViewHolder,
        position: Int,
    ) = viewHolder.dragAnchor
}
