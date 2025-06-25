package com.capricallctx.campkiosk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.capricallctx.campkiosk.ui.AnimatedMouth
import com.capricallctx.campkiosk.ui.DraggableBlinkingEyes
import com.capricallctx.campkiosk.ui.DraggableMouth
import com.capricallctx.campkiosk.ui.rememberAudioManager
import com.capricallctx.campkiosk.ui.rememberDialogManager
import com.capricallctx.campkiosk.ui.rememberEyePositionManager
import com.capricallctx.campkiosk.ui.rememberMouthPositionManager
import com.capricallctx.campkiosk.ui.rememberRandomAudioManager
import com.capricallctx.campkiosk.ui.FadingText
import com.capricallctx.campkiosk.ui.DustButton
import com.capricallctx.campkiosk.ui.EventsCarousel
import com.capricallctx.campkiosk.ui.PhotoboothButton
import com.capricallctx.campkiosk.ui.PhotoboothSection
import com.capricallctx.campkiosk.ui.theme.CampKioskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampKioskTheme {
                KioskContent()
            }
        }
    }
}

@Composable
fun KioskContent() {
    val randomAudioManager = rememberRandomAudioManager()
    val mouthPositionManager = rememberMouthPositionManager()
    val eyePositionManager = rememberEyePositionManager()
    val dialogManager = rememberDialogManager()

    val isPlaying by randomAudioManager.isPlaying
    val audioLevel by randomAudioManager.audioLevel
    val currentFileName by randomAudioManager.currentFileName
    val currentDialog by dialogManager.currentDialog
    val isShowingDialog by dialogManager.isShowingDialog

    val greetingNames = remember {
        listOf(
            "Dirty Hippie",
            "Degenerate",
            "Dusty Wanderer",
            "Sparkle Pony",
            "Corn Goblin",
            "Burner Trash",
            "Playa Casualty",
            "Desert Rat",
            "Radical Self-Expressionist",
            "Shiny Happy Person",
            "Beautiful Weirdo",
            "Glitter Goblin",
            "Nomadic Soul"
        )
    }

    var randomGreeting by remember {
        mutableStateOf(greetingNames.random())
    }
    
    // Change greeting every 10 seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000)
            randomGreeting = greetingNames.random()
        }
    }

    // Start random audio playback automatically for demo purposes
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000) // Wait 2 seconds then start
        randomAudioManager.startContinuousPlayback()
    }

    // Show text for current audio file
    LaunchedEffect(currentFileName) {
        if (currentFileName.isNotEmpty()) {
            dialogManager.showTextForAudio(currentFileName)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            randomAudioManager.release()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.camp_background),
                contentDescription = "Camp background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Camp title
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            Text(
                text = "Pussy Avalanche",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 5.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                fontSize = (screenWidth.value / 12).sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                style = MaterialTheme.typography.displayLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            DraggableBlinkingEyes(
                positionManager = eyePositionManager,
                modifier = Modifier.fillMaxSize(),
                size = 175f
            )
            DraggableMouth(
                audioLevel = audioLevel,
                isPlaying = isPlaying,
                positionManager = mouthPositionManager,
                modifier = Modifier.fillMaxSize()
            )

            // Show fading text for audio content
            FadingText(
                text = currentDialog,
                isVisible = isShowingDialog,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 65.dp),
                color = Color.Black,
                fontSize = 16
            )

            // Show greeting when no audio text is shown
            if (!isShowingDialog) {
                Greeting(
                    name = randomGreeting,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 85.dp)
                )
            }

            // Dust button positioned above photobooth
            DustButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 270.dp)
                    .width(270.dp)
                    .height(50.dp)
            )
            
            // Photobooth button positioned to the right
            PhotoboothButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 205.dp)
                    .width(270.dp)
                    .height(50.dp)
            )

            // Events carousel at the bottom
            EventsCarousel(
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
        color = Color.Black,
        fontSize = 24.sp,
        style = MaterialTheme.typography.headlineMedium
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CampKioskTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.camp_background),
                contentDescription = "Camp background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            AnimatedMouth(
                modifier = Modifier.align(Alignment.Center),
                audioLevel = 0.7f,
                isPlaying = true
            )
            Greeting(
                "Camp Visitors",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 80.dp)
            )
        }
    }
}
