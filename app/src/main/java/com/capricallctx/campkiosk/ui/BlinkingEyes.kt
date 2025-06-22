package com.capricallctx.campkiosk.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.capricallctx.campkiosk.R
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class EyeState {
    OPEN, HALF, CLOSED
}

@Composable
fun BlinkingEyes(
    modifier: Modifier = Modifier,
    size: Float = 60f
) {
    var currentEyeState by remember { mutableStateOf(EyeState.OPEN) }
    var isBlinking by remember { mutableStateOf(false) }
    
    // Natural blinking pattern
    LaunchedEffect(Unit) {
        while (true) {
            // Random delay between blinks (2-6 seconds)
            val delayTime = Random.nextLong(2000, 6000)
            delay(delayTime)
            
            // Perform blink sequence
            performBlink { newState ->
                currentEyeState = newState
            }
        }
    }
    
    // Eye state animation for smooth transitions
    val eyeStateTransition = updateTransition(
        targetState = currentEyeState,
        label = "eye_state_transition"
    )
    
    val eyeAlpha by eyeStateTransition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 150)
        },
        label = "eye_alpha"
    ) { state ->
        when (state) {
            EyeState.OPEN -> 1f
            EyeState.HALF -> 0.7f
            EyeState.CLOSED -> 0.3f
        }
    }
    
    // Display the current eye state
    Image(
        painter = painterResource(
            id = when (currentEyeState) {
                EyeState.OPEN -> R.drawable.open
                EyeState.HALF -> R.drawable.half
                EyeState.CLOSED -> R.drawable.closed
            }
        ),
        contentDescription = "Character eyes",
        modifier = modifier.size(size.dp),
        alpha = eyeAlpha
    )
}

private suspend fun performBlink(onStateChange: (EyeState) -> Unit) {
    // Natural blink sequence: open -> half -> closed -> half -> open
    onStateChange(EyeState.HALF)
    delay(100)
    onStateChange(EyeState.CLOSED)
    delay(150)
    onStateChange(EyeState.HALF)
    delay(100)
    onStateChange(EyeState.OPEN)
}