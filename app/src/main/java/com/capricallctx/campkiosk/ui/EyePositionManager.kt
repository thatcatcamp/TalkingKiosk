package com.capricallctx.campkiosk.ui

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberEyePositionManager(): EyePositionManager {
    val context = LocalContext.current
    return remember { EyePositionManager(context) }
}

class EyePositionManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("eye_position", Context.MODE_PRIVATE)
    
    private var _position = mutableStateOf(loadPosition())
    val position: State<Offset> = _position
    
    fun updatePosition(newPosition: Offset) {
        _position.value = newPosition
        savePosition(newPosition)
    }
    
    private fun loadPosition(): Offset {
        val x = prefs.getFloat("eye_x", 0.5f) // Default to center
        val y = prefs.getFloat("eye_y", 0.3f) // Default to upper area where eyes typically are
        return Offset(x, y)
    }
    
    private fun savePosition(position: Offset) {
        prefs.edit()
            .putFloat("eye_x", position.x)
            .putFloat("eye_y", position.y)
            .apply()
    }
}