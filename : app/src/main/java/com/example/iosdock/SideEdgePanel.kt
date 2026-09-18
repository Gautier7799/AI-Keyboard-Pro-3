package com.example.iosdock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SideEdgePanel(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // المحتوى التفاعلي للشريط الجانبي
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInHorizontally(initialLineWidth = { it }),
            exit = slideOutHorizontally(targetLineWidth = { it })
        ) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight(0.6f)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(Color.DarkGray.copy(alpha = 0.9f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "محتوى الشريط الجانبي",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.width(2.dp))

        // مقبض السحب والتوسيع
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(Color.White.copy(alpha = 0.7f))
                .clickable { onToggleExpand() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}
