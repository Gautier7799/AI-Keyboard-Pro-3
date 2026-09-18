package com.example.iosdock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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

enum class EdgePanelType {
    QUICK_ACCESS, QUICK_VIEW, QUICK_CONTROL
}

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
            SamsungGalaxyEdgeContent(onClose = onToggleExpand)
        }

        Spacer(modifier = Modifier.width(2.dp))

        // المقبض الجانبي الشفاف
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
fun SamsungGalaxyEdgeContent(onClose: () -> Unit) {
    var selectedTab by remember { mutableStateOf(EdgePanelType.QUICK_ACCESS) }

    Card(
        modifier = Modifier
            .width(250.dp)
            .fillMaxHeight(0.85f)
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEC121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // شريط إغلاق اللوحة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Galaxy Edge",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
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

            // شريط التبويب الثلاثي (Quick Access / Quick View / Quick Control)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TabButton("وصول", selectedTab == EdgePanelType.QUICK_ACCESS) {
                    selectedTab = EdgePanelType.QUICK_ACCESS
                }
                TabButton("عرض", selectedTab == EdgePanelType.QUICK_VIEW) {
                    selectedTab = EdgePanelType.QUICK_VIEW
                }
                TabButton("تحكم", selectedTab == EdgePanelType.QUICK_CONTROL) {
                    selectedTab = EdgePanelType.QUICK_CONTROL
                }
            }

            Divider(color = Color.White.copy(alpha = 0.15f))

            // عرض المحتوى حسب التبويب المختار
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    EdgePanelType.QUICK_ACCESS -> QuickAccessPanel()
                    EdgePanelType.QUICK_VIEW -> QuickViewPanel()
                    EdgePanelType.QUICK_CONTROL -> QuickControlPanel()
                }
            }
        }
    }
}

@Composable
fun TabButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF2196F3) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// 1. لوحة الوصول السريع (Quick Access)
@Composable
fun QuickAccessPanel() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("التطبيقات الشائعة", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        
        val apps = listOf(
            Triple(Icons.Default.Phone, "الهاتف", Color(0xFF4CAF50)),
            Triple(Icons.Default.Message, "الرسائل", Color(0xFF2196F3)),
            Triple(Icons.Default.CameraAlt, "الكاميرا", Color(0xFFE91E63)),
            Triple(Icons.Default.ChromeReaderMode, "المتصفح", Color(0xFFFF9800)),
            Triple(Icons.Default.Email, "البريد", Color(0xFF9C27B0)),
            Triple(Icons.Default.Settings, "الإعدادات", Color(0xFF607D8B))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(apps.size) { index ->
                val app = apps[index]
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                    modifier = Modifier.clickable { /* فتح التطبيق */ }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(app.third),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = app.first, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(text = app.second, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 2. لوحة العرض السريع (Quick View)
@Composable
fun QuickViewPanel() {
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val currentDate = remember { SimpleDateFormat("EEEE, d MMMM", Locale("ar")).format(Date()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // ودجت الساعة
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = currentTime, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text(text = currentDate, color = Color.LightGray, fontSize = 11.sp)
            }
        }

        // ودجت الطقس
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("تطاوين", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("31°C - مشمس", color = Color.LightGray, fontSize = 11.sp)
                }
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(32.dp))
            }
        }
    }
}

// 3. لوحة التحكم السريع (Quick Control)
@Composable
fun QuickControlPanel() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("عناصر التحكم", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        ControlRow(icon = Icons.Default.Wifi, label = "شبكة Wi-Fi", isActive = true)
        ControlRow(icon = Icons.Default.Bluetooth, label = "البلوتوث", isActive = true)
        ControlRow(icon = Icons.Default.FlashlightOn, label = "الكشاف", isActive = false)
        ControlRow(icon = Icons.Default.VolumeUp, label = "نمط الصوت", isActive = true)
    }
}

@Composable
fun ControlRow(icon: ImageVector, label: String, isActive: Boolean) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text(text = label, color = Color.White, fontSize = 12.sp)
            }
            Switch(checked = isActive, onCheckedChange = {}, modifier = Modifier.height(24.dp))
        }
    }
}
