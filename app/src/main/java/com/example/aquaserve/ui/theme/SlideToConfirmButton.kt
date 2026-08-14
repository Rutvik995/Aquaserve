package com.example.aquaserve.ui.theme


import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SlideToConfirmButton(modifier: Modifier = Modifier, onConfirmed: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var offsetX by remember { mutableStateOf(0f) }
    var isConfirmed by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.LightGray.copy(alpha = 0.2f))
    ) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val thumbSize = 64.dp
        val thumbSizePx = with(LocalDensity.current) { thumbSize.toPx() }
        val maxOffset = maxWidthPx - thumbSizePx - with(LocalDensity.current) { 4.dp.toPx() * 2 }

        val textAlpha by animateFloatAsState(targetValue = 1f - (offsetX / maxOffset * 1.5f))
        Text(
            text = "Slide to Place Order",
            modifier = Modifier.align(Alignment.Center),
            color = Color.DarkGray.copy(alpha = textAlpha.coerceAtLeast(0f))
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .padding(4.dp)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.Blue)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = offsetX + delta
                        offsetX = newOffset.coerceIn(0f, maxOffset)
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            if (offsetX > maxOffset * 0.9f && !isConfirmed) {
                                isConfirmed = true
                                onConfirmed()
                            }
                            offsetX = 0f
                            isConfirmed = false
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowForward, "Slide to confirm", tint = Color.White)
        }
    }
}