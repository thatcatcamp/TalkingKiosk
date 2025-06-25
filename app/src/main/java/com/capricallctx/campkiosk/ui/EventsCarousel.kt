package com.capricallctx.campkiosk.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class CampEvent(
    val day: String,
    val title: String,
    val time: String,
    val location: String
)

@Composable
fun EventsCarousel(
    modifier: Modifier = Modifier
) {
    val events = remember {
        listOf(
            CampEvent("MON", "Playa Postcards/Litterbox", "09:00-15:00", "Toxoplasmosis Bar"),
            CampEvent("TUE", "Litterbox", "09:00-15:00", "Toxoplasmosis Bar"),
            CampEvent("WED", "Litterbox", "09:00-15:00", "Toxoplasmosis Bar"),
            CampEvent("THU", "Litterbox", "09:00-15:00", "Toxoplasmosis Bar"),
            CampEvent("FRI", "Buttholes and Bourbon", "11:00-12:00", "Toxoplasmosis Bar")
        )
    }
    
    var highlightedIndex by remember { mutableStateOf(0) }
    
    // Cycle through events every 3 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            highlightedIndex = (highlightedIndex + 1) % events.size
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp)
    ) {
        Text(
            text = "This Week's Events",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(events.size) { index ->
                EventCard(
                    event = events[index],
                    isHighlighted = index == highlightedIndex,
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}

@Composable
private fun EventCard(
    event: CampEvent,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.1f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "card_scale"
    )
    
    val borderColor = if (isHighlighted) Color.Yellow else Color.Transparent
    Card(
        modifier = modifier
            .height(100.dp)
            .scale(scale)
            .border(
                width = if (isHighlighted) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) 
                Color.Yellow.copy(alpha = 0.2f) 
            else 
                Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day
            Text(
                text = event.day,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                textAlign = TextAlign.Center
            )

            // Title
            Text(
                text = event.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            // Time
            Text(
                text = event.time,
                fontSize = 8.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            // Location
            Text(
                text = event.location,
                fontSize = 7.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
