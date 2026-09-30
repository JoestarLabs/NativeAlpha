package com.cylonid.nativealpha.fragments.adblocklist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.model.AdblockConfig
import com.cylonid.nativealpha.model.DataManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class AdblockListFragment : Fragment(R.layout.fragment_adblock_list) {
    private lateinit var adapter: AdblockListAdapter
    private lateinit var list: DragDropSwipeRecyclerView

    private var fab: FloatingActionButton? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val globalWebApp = DataManager.getInstance().settings.globalWebApp
        adapter = AdblockListAdapter(globalWebApp.adBlockSettings)

        fab = requireActivity().findViewById(R.id.adblock_fab)
        checkFabEnabledStateIfNecessary()

        list = view.findViewById(R.id.adblock_list)
        list.layoutManager = LinearLayoutManager(requiredActivity())
        list.adapter = adapter
        list.swipeListener = onItemSwipeListener
        list.orientation =
            DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
        list.disableSwipeDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)
    }

    fun updateAdblockList() {
        adapter.updateAdblockList()
        checkFabEnabledStateIfNecessary()
    }

    private fun checkFabEnabledStateIfNecessary() {
        if (fab != null && adapter.itemCount >= 8) {
            fab?.isEnabled = false
        } else {
            fab?.isEnabled = true
        }
    }

    private fun requiredActivity(): FragmentActivity = requireNotNull(activity) { "AdblockListFragment is not attached to an activity." }

    private val onItemSwipeListener =
        object : OnItemSwipeListener<AdblockConfig> {
            override fun onItemSwiped(
                position: Int,
                direction: OnItemSwipeListener.SwipeDirection,
                item: AdblockConfig,
            ): Boolean {
                DataManager.getInstance().apply {
                    settings.globalWebApp.adBlockSettings.removeAt(position)
                    saveGlobalSettings()
                }
                updateAdblockList()
                checkFabEnabledStateIfNecessary()

                val itemSwipedSnackBar =
                    view?.let { Snackbar.make(it, getString(R.string.x_was_removed, item.label), Snackbar.LENGTH_SHORT) }
                itemSwipedSnackBar?.setAction(getString(R.string.undo).uppercase()) {
                    DataManager.getInstance().apply {
                        settings.globalWebApp.adBlockSettings.add(position, item)
                        saveGlobalSettings()
                    }
                    updateAdblockList()
                }
                itemSwipedSnackBar?.show()

                return true
            }
        }
}
