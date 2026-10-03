package com.cylonid.nativealpha.ui.webapplist

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ReorderableLazyListState(
    val lazyListState: LazyListState,
    val scope: CoroutineScope,
    val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    val onDragEnd: () -> Unit,
) {
    var draggedIndex by mutableStateOf<Int?>(null)
        private set

    var draggedOffset by mutableFloatStateOf(0f)
        private set

    var isSettling by mutableStateOf(false)
        private set

    val isDragging: Boolean
        get() = draggedIndex != null

    fun onDragStarted(index: Int) {
        if (isSettling) return
        draggedIndex = index
        draggedOffset = 0f
    }

    fun onDrag(dragAmount: Float) {
        val currentIndex = draggedIndex ?: return
        draggedOffset += dragAmount

        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
        val currentItem = visibleItems.firstOrNull { it.index == currentIndex }
        val nextItem = visibleItems.firstOrNull { it.index == currentIndex + 1 }
        val prevItem = visibleItems.firstOrNull { it.index == currentIndex - 1 }

        val defaultHeight = currentItem?.size?.toFloat() ?: 180f
        val downDistance =
            if (currentItem != null && nextItem != null) {
                (nextItem.offset - currentItem.offset).toFloat()
            } else {
                defaultHeight + 24f
            }

        val upDistance =
            if (currentItem != null && prevItem != null) {
                (currentItem.offset - prevItem.offset).toFloat()
            } else {
                defaultHeight + 24f
            }

        if (draggedOffset > downDistance * 0.5f) {
            val nextIndex = currentIndex + 1
            val totalItems = lazyListState.layoutInfo.totalItemsCount
            if (nextIndex < totalItems) {
                onMove(currentIndex, nextIndex)
                draggedIndex = nextIndex
                draggedOffset -= downDistance
            }
        } else if (draggedOffset < -upDistance * 0.5f) {
            val prevIndex = currentIndex - 1
            if (prevIndex >= 0) {
                onMove(currentIndex, prevIndex)
                draggedIndex = prevIndex
                draggedOffset += upDistance
            }
        }
    }

    fun onDragStopped() {
        if (draggedIndex == null) return
        isSettling = true
        scope.launch {
            try {
                animate(
                    initialValue = draggedOffset,
                    targetValue = 0f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                ) { value, _ ->
                    draggedOffset = value
                }
            } finally {
                draggedIndex = null
                draggedOffset = 0f
                isSettling = false
                onDragEnd()
            }
        }
    }
}

@Composable
fun rememberReorderableLazyListState(
    lazyListState: LazyListState,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onDragEnd: () -> Unit,
): ReorderableLazyListState {
    val scope = rememberCoroutineScope()
    return remember(lazyListState, scope) {
        ReorderableLazyListState(
            lazyListState = lazyListState,
            scope = scope,
            onMove = onMove,
            onDragEnd = onDragEnd,
        )
    }
}

fun Modifier.dragHandle(
    state: ReorderableLazyListState,
    index: Int,
): Modifier =
    this.composed {
        val currentIndex by rememberUpdatedState(index)
        this.pointerInput(state) {
            detectVerticalDragGestures(
                onDragStart = { state.onDragStarted(currentIndex) },
                onDragEnd = { state.onDragStopped() },
                onDragCancel = { state.onDragStopped() },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    state.onDrag(dragAmount)
                },
            )
        }
    }

fun Modifier.reorderableItem(
    state: ReorderableLazyListState,
    index: Int,
): Modifier =
    this
        .zIndex(if (state.draggedIndex == index) 2f else 0f)
        .graphicsLayer {
            val isCurrentDragging = state.draggedIndex == index
            if (isCurrentDragging) {
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
