package com.devvikram.shimmyy


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.ShimmerBox(
    modifier: Modifier = Modifier,
    width: Int = 200,
    height: Int = 20
) {
    // Shimmer animation
    val infiniteTransition = rememberInfiniteTransition()
    val alphaAnim = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .size(width.dp, height.dp)
            .alpha(alphaAnim.value)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    )
                )
            )
    )
}



