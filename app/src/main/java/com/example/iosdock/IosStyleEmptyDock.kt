package com.example.iosdock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IosStyleEmptyDock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // الإطار الزجاجي الشفاف النقي الشبيه بـ iOS (بدون أي أيقونات إضافية)
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
                )
        )
    }
}
