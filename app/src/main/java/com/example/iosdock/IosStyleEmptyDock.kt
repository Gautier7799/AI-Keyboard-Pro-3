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
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // الإطار الزجاجي الشفاف لخلفية أيقونات النظام الأصلية
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .height(86.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .border(
                    width = 1.2.dp,
                    color = Color.White.copy(alpha = 0.40f),
                    shape = RoundedCornerShape(32.dp)
                )
        )
    }
}
