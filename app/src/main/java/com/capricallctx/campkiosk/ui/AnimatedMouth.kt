package com.capricallctx.campkiosk.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AnimatedMouth(
    modifier: Modifier = Modifier,
    mouthColor: Color = Color.Black,
    size: Float = 40f,
    audioLevel: Float = 0.5f,
    isPlaying: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mouth_animation")
    
    // Use audio level to drive mouth openness when audio is playing
    val targetOpenness = if (isPlaying) {
        (audioLevel * 0.8f + 0.2f).coerceIn(0.2f, 1f)
    } else {
        0.2f
    }
    
    val mouthOpenness by animateFloatAsState(
        targetValue = targetOpenness,
        animationSpec = tween(
            durationMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "mouth_openness"
    )
    
    // Faster speaking variation when audio is playing
    val speakingSpeed = if (isPlaying) 400 else 1200
    val speakingPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = speakingSpeed,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "speaking_phase"
    )
    
    Canvas(
        modifier = modifier.size(size.dp)
    ) {
        drawMouth(
            mouthColor = mouthColor,
            openness = mouthOpenness,
            speakingPhase = speakingPhase,
            size = size
        )
    }
}

private fun DrawScope.drawMouth(
    mouthColor: Color,
    openness: Float,
    speakingPhase: Float,
    size: Float
) {
    val centerX = this.size.width / 2
    val centerY = this.size.height / 2
    
    // Create varying mouth shapes for speaking animation
    val mouthVariation = sin(speakingPhase) * 0.2f + 1f
    val mouthWidth = size * 0.8f * mouthVariation
    val mouthHeight = size * 0.4f * openness
    
    // Add more dynamic movement when audio is playing
    val jawDrop = if (openness > 0.6f) (openness - 0.6f) * 0.3f else 0f
    
    // Draw main mouth oval with jaw drop effect
    drawOval(
        color = mouthColor,
        topLeft = Offset(
            centerX - mouthWidth / 2,
            centerY - mouthHeight / 2 + jawDrop * size * 0.1f
        ),
        size = Size(mouthWidth, mouthHeight + jawDrop * size * 0.2f)
    )
    
    // Add teeth effect when mouth is more open
    if (openness > 0.5f) {
        val teethHeight = mouthHeight * 0.2f
        drawRect(
            color = Color.White,
            topLeft = Offset(
                centerX - mouthWidth / 2 + 2,
                centerY - mouthHeight / 2 + 2
            ),
            size = Size(mouthWidth - 4, teethHeight)
        )
        
        // Draw individual teeth
        val teethCount = 6
        val teethWidth = (mouthWidth - 8) / teethCount
        for (i in 0 until teethCount) {
            val x = centerX - mouthWidth / 2 + 4 + i * teethWidth
            drawRect(
                color = Color.LightGray,
                topLeft = Offset(x, centerY - mouthHeight / 2 + 2),
                size = Size(1f, teethHeight)
            )
        }
    }
}