package com.cylonid.nativealpha.ui.webapplist

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

class ReorderableLazyListState(
    val lazyListState: LazyListState,
    val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    val onDragEnd: () -> Unit,
) {
    var draggedIndex by mutableStateOf<Int?>(null)
        private set

    var draggedOffset by mutableFloatStateOf(0f)
        private set

    val isDragging: Boolean
        get() = draggedIndex != null

    fun onDragStarted(index: Int) {
        draggedIndex = index
        draggedOffset = 0f
    }

    fun onDrag(dragAmount: Float) {
        val currentIndex = draggedIndex ?: return
        draggedOffset += dragAmount

        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
        val currentItemInfo = visibleItems.firstOrNull { it.index == currentIndex }
        val itemHeight = currentItemInfo?.size?.toFloat() ?: 180f

        val threshold = itemHeight * 0.5f

        if (draggedOffset > threshold) {
            val nextIndex = currentIndex + 1
            val totalItems = lazyListState.layoutInfo.totalItemsCount
            if (nextIndex < totalItems) {
                onMove(currentIndex, nextIndex)
                draggedIndex = nextIndex
                draggedOffset -= itemHeight
            }
        } else if (draggedOffset < -threshold) {
            val prevIndex = currentIndex - 1
            if (prevIndex >= 0) {
                onMove(currentIndex, prevIndex)
                draggedIndex = prevIndex
                draggedOffset += itemHeight
            }
        }
    }

    fun onDragStopped() {
        if (draggedIndex != null) {
            draggedIndex = null
            draggedOffset = 0f
            onDragEnd()
        }
    }
}

@Composable
fun rememberReorderableLazyListState(
    lazyListState: LazyListState,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onDragEnd: () -> Unit,
): ReorderableLazyListState =
    remember(lazyListState) {
        ReorderableLazyListState(
            lazyListState = lazyListState,
            onMove = onMove,
            onDragEnd = onDragEnd,
        )
    }

fun Modifier.dragHandle(
    state: ReorderableLazyListState,
    index: Int,
): Modifier =
    this.pointerInput(state, index) {
        detectVerticalDragGestures(
            onDragStart = { state.onDragStarted(index) },
            onDragEnd = { state.onDragStopped() },
            onDragCancel = { state.onDragStopped() },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                state.onDrag(dragAmount)
            },
        )
    }

fun Modifier.reorderableItem(
    state: ReorderableLazyListState,
    index: Int,
): Modifier {
    val isItemDragging = state.draggedIndex == index
    return this
        .zIndex(if (isItemDragging) 2f else 0f)
        .graphicsLayer {
            if (isItemDragging) {
                translationY = state.draggedOffset
                shadowElevation = 12.dp.toPx()
                scaleX = 1.02f
                scaleY = 1.02f
            } else {
                translationY = 0f
                shadowElevation = 0f
                scaleX = 1f
                scaleY = 1f
            }
        }
}
