package com.capricallctx.campkiosk.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

@Composable
fun DraggableBlinkingEyes(
    positionManager: EyePositionManager,
    modifier: Modifier = Modifier,
    size: Float = 60f
) {
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val savedPosition by positionManager.position
    var currentPosition by remember { mutableStateOf(savedPosition) }
    
    // Update current position when saved position changes
    LaunchedEffect(savedPosition) {
        currentPosition = savedPosition
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
    ) {
        if (containerSize != IntSize.Zero) {
            val eyeSizePx = with(density) { size.toDp().toPx() }
            
            // Convert relative position (0-1) to actual pixels
            val actualX = (currentPosition.x * (containerSize.width - eyeSizePx)).coerceIn(0f, containerSize.width - eyeSizePx)
            val actualY = (currentPosition.y * (containerSize.height - eyeSizePx)).coerceIn(0f, containerSize.height - eyeSizePx)
            
            BlinkingEyes(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = actualX.roundToInt(),
                            y = actualY.roundToInt()
                        )
                    }
                    .pointerInput(containerSize) {
                        detectDragGestures { change, dragAmount ->
                            val newRelativeX = currentPosition.x + (dragAmount.x / (containerSize.width - eyeSizePx))
                            val newRelativeY = currentPosition.y + (dragAmount.y / (containerSize.height - eyeSizePx))
                            
                            val clampedX = newRelativeX.coerceIn(0f, 1f)
                            val clampedY = newRelativeY.coerceIn(0f, 1f)
                            
                            val newPosition = Offset(clampedX, clampedY)
                            currentPosition = newPosition
                            positionManager.updatePosition(newPosition)
                        }
                    },
                size = size
            )
        }
    }
}