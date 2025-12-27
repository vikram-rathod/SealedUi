package com.devvikram.otpviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpView(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 4,
    boxSize: Dp = 50.dp,
    cornerRadius: Dp = 8.dp,
    borderWidth: Dp = 2.dp,
    borderColor: Color = Color.Gray,
    backgroundColor: Color = Color.White,
    textColor: Color = Color.Black,
    textStyle: TextStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    spaceBetween: Dp = 8.dp,
    filledBorderColor: Color = Color.Blue,
) {
    val normalizedOtp = otpText.take(otpCount)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(otpCount) { index ->
            OtpBox(
                value = normalizedOtp.getOrNull(index)?.toString() ?: "",
                boxSize = boxSize,
                cornerRadius = cornerRadius,
                borderWidth = borderWidth,
                borderColor = if (index < normalizedOtp.length) filledBorderColor else borderColor,
                backgroundColor = backgroundColor,
                textColor = textColor,
                textStyle = textStyle
            )
        }
    }
}

@Composable
private fun OtpBox(
    value: String,
    boxSize: Dp,
    cornerRadius: Dp,
    borderWidth: Dp,
    borderColor: Color,
    backgroundColor: Color,
    textColor: Color,
    textStyle: TextStyle
) {
    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = textStyle,
                color = textColor
            )
        }
    }
}

// Extension function for easy validation
fun String.isValidOtp(otpCount: Int): Boolean {
    return this.length == otpCount && this.all { it.isDigit() }
}

@Preview
@Composable
fun OtpViewPreview() {
    OtpView(otpText = "1278")
}