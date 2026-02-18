package com.thumbwar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thumbwar.ui.theme.CountdownGlow
import com.thumbwar.ui.theme.CountdownWhite

@Composable
fun CountdownOverlay(
    text: String,
    beat: Int
) {
    var lastBeat by remember { mutableIntStateOf(0) }
    val animKey = if (beat != lastBeat) {
        lastBeat = beat
        beat
    } else beat

    val scale = remember(animKey) { Animatable(2f) }
    val alphaAnim = remember(animKey) { Animatable(0f) }

    LaunchedEffect(animKey) {
        scale.snapTo(2f)
        alphaAnim.snapTo(0f)
        scale.animateTo(1f, animationSpec = tween(250, easing = FastOutSlowInEasing))
        alphaAnim.animateTo(1f, animationSpec = tween(200))
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val isNumber = text.length <= 1
        if (isNumber) {
            Text(
                text = text,
                fontSize = 120.sp,
                fontWeight = FontWeight.Black,
                color = CountdownGlow,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alphaAnim.value)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineLarge,
                color = CountdownWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .alpha(alphaAnim.value)
            )
        }
    }
}
