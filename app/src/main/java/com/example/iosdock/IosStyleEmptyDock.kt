package com.example.iosdock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun IosStyleEmptyDock(
    modifier: Modifier = Modifier,
    showGeminiIcon: Boolean = true,
    onGeminiClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // الإطار الزجاجي الشفاف المصمم بدقة لـ iOS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.28f))
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.50f),
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (showGeminiIcon) {
                // أيقونة Gemini المضيئة والمدمجة
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.35f))
                        .border(1.dp, Color.White.copy(alpha = 0.70f), CircleShape)
                        .clickable { onGeminiClick() },
                    contentAlignment = Alignment.Center
                ) {
                    GeminiSparkleIcon()
                }
            }
        }
    }
}

@Composable
fun GeminiSparkleIcon() {
    Canvas(modifier = Modifier.size(24.dp)) {
        val width = size.width
        val height = size.height
        val path = Path().apply {
            moveTo(width * 0.5f, 0f)
            quadraticTo(width * 0.5f, height * 0.5f, width, height * 0.5f)
            quadraticTo(width * 0.5f, height * 0.5f, width * 0.5f, height)
            quadraticTo(width * 0.5f, height * 0.5f, 0f, height * 0.5f)
            quadraticTo(width * 0.5f, height * 0.5f, width * 0.5f, 0f)
            close()
        }
        drawPath(path = path, color = Color.White)
    }
}
