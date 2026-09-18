package com.example.iosdock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

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
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInHorizontally(initialLineWidth = { it }),
            exit = slideOutHorizontally(targetLineWidth = { it })
        ) {
            SamsungWidgetEdgeContent(onClose = onToggleExpand)
        }

        Spacer(modifier = Modifier.width(2.dp))

        // المقبض الجانبي العائم
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(Color.White.copy(alpha = 0.35f))
                .clickable { onToggleExpand() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
    }
}

@Composable
fun SamsungWidgetEdgeContent(onClose: () -> Unit) {
    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    val currentDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale("ar")).format(Date())
    }

    Card(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight(0.82f)
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDC1C1C1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Widget Edge",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onClose() }
                )
            }

            // 1. ودجت الساعة
            WidgetCard {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = currentTime,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentDate,
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }

            // 2. ودجت التحكم السريع
            WidgetCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("التحكم السريع", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ToggleIcon(icon = Icons.Default.Wifi, label = "Wi-Fi", isActive = true)
                        ToggleIcon(icon = Icons.Default.Bluetooth, label = "Bluetooth", isActive = true)
                        ToggleIcon(icon = Icons.Default.FlashlightOn, label = "الكشاف", isActive = false)
                        ToggleIcon(icon = Icons.Default.VolumeUp, label = "الصوت", isActive = true)
                    }
                }
            }

            // 3. ودجت الطقس
            WidgetCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("تطاوين", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("31°C - مشمس", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // 4. ودجت الاختصارات
            WidgetCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("التطبيقات", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AppShortcut(icon = Icons.Default.Phone, color = Color(0xFF4CAF50))
                        AppShortcut(icon = Icons.Default.Message, color = Color(0xFF2196F3))
                        AppShortcut(icon = Icons.Default.CameraAlt, color = Color(0xFFE91E63))
                        AppShortcut(icon = Icons.Default.Settings, color = Color(0xFF607D8B))
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
fun ToggleIcon(icon: ImageVector, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFF388E3C) else Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun AppShortcut(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable { /* تنفيذ فتح التطبيق */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}
