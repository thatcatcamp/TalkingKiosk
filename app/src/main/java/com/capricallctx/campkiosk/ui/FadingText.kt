package com.capricallctx.campkiosk.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FadingText(
    text: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: Int = 18
) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible && text.isNotEmpty()) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500, // 500ms fade in/out
            easing = FastOutSlowInEasing
        ),
        label = "text_fade"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible && text.isNotEmpty()) 1f else 0.8f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "text_scale"
    )
    
    if (alpha > 0f) {
        Text(
            text = text,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .graphicsLayer(
                    alpha = alpha,
                    scaleX = scale,
                    scaleY = scale
                ),
            fontSize = fontSize.sp,
            color = color.copy(alpha = alpha),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}

