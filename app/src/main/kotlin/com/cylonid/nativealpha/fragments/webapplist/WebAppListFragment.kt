package com.cylonid.nativealpha.fragments.webapplist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.WebAppSettingsActivity
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.util.Const
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.google.android.material.snackbar.Snackbar

class WebAppListFragment : Fragment(R.layout.fragment_web_app_list) {
    private lateinit var adapter: WebAppListAdapter

    private lateinit var list: DragDropSwipeRecyclerView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WebAppListAdapter(DataManager.getInstance().activeWebsites, requiredActivity())

        list = view.findViewById(R.id.web_app_list)
        list.layoutManager = LinearLayoutManager(requiredActivity())
        list.adapter = adapter
        list.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
        list.dragListener = onItemDragListener
        list.swipeListener = onItemSwipeListener
    }

    fun updateWebAppList() {
        adapter.updateWebAppList()
    }

    private fun requiredActivity(): FragmentActivity = requireNotNull(activity) { "WebAppListFragment is not attached to an activity." }

    private val onItemSwipeListener =
        object : OnItemSwipeListener<WebApp> {
            override fun onItemSwiped(
                position: Int,
                direction: OnItemSwipeListener.SwipeDirection,
                item: WebApp,
            ): Boolean {
                if (direction == OnItemSwipeListener.SwipeDirection.RIGHT_TO_LEFT) {
                    item.markInactive(requiredActivity())
                    saveCurrentDisplayedOrderOfWebAppsToDisk()

                    val itemSwipedSnackBar =
                        view?.let { Snackbar.make(it, getString(R.string.x_was_removed, item.title), Snackbar.LENGTH_SHORT) }
                    itemSwipedSnackBar?.setAction(getString(R.string.undo).uppercase()) {
                        item.isActiveEntry = true
                        DataManager.getInstance().saveWebAppData()
                        updateWebAppList()
                    }
                    itemSwipedSnackBar?.show()
                }
                if (direction == OnItemSwipeListener.SwipeDirection.LEFT_TO_RIGHT) {
                    val intent =
                        Intent(
                            activity,
                            WebAppSettingsActivity::class.java,
                        )
                    intent.putExtra(Const.INTENT_WEBAPPID, item.ID)
                    intent.setAction(Intent.ACTION_VIEW)
                    context?.let { ContextCompat.startActivity(it, intent, null) }
                    return true
                }
                return false
            }
        }

    private val onItemDragListener =
        object : OnItemDragListener<WebApp> {
            override fun onItemDropped(
                initialPosition: Int,
                finalPosition: Int,
                item: WebApp,
            ) {
                saveCurrentDisplayedOrderOfWebAppsToDisk()
            }

            override fun onItemDragged(
                previousPosition: Int,
                newPosition: Int,
                item: WebApp,
            ) {
            }
        }

    private fun saveCurrentDisplayedOrderOfWebAppsToDisk() {
        for ((i, webapp) in adapter.dataSet.withIndex()) {
            // Do not use "i" as index here, since adapter.dataSet includes only active website.
            // The DataManager's websites array contains both active and inactive websites.
            DataManager.getInstance().websites[webapp.ID].order = i
        }
        DataManager.getInstance().saveWebAppData()
    }

    companion object {
        fun newInstance() = WebAppListFragment()
    }
}
