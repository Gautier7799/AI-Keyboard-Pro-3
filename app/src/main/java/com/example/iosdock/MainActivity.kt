package com.example.iosdock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212)
                ) {
                    SettingsScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var isServiceRunning by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات iOS Dock", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Superposition / Overlay Permission
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. إذن الظهور فوق التطبيقات (Superposition)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hasOverlayPermission) "الحالة: مفعل ✔" else "الحالة: غير مفعل ✖",
                        color = if (hasOverlayPermission) Color.Green else Color.Red,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                    ) {
                        Text("منح إذن Superposition")
                    }
                }
            }

            // Card 2: Accessibility Permission
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. إذن إمكانية الوصول (Accessibility)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "فتح إعدادات الوصول بالنظام",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                    ) {
                        Text("فتح إعدادات Accessibility")
                    }
                }
            }

            // Card 3: Toggle Service
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. تشغيل / إيقاف شريط Dock الشفاف",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تفعيل الشريط العائم (بدون أيقونات)", color = Color.White)
                        Switch(
                            checked = isServiceRunning,
                            onCheckedChange = { checked ->
                                if (checked && !Settings.canDrawOverlays(context)) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } else {
                                    isServiceRunning = checked
                                    val serviceIntent = Intent(context, DockOverlayService::class.java)
                                    if (checked) {
                                        context.startService(serviceIntent)
                                    } else {
                                        context.stopService(serviceIntent)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Lower Dock Preview
            Text("معاينة الشريط الشفاف (Sans Icon):", color = Color.Gray, fontSize = 14.sp)
            IosStyleEmptyDock()
        }
    }
}

@Composable
fun IosStyleEmptyDock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.40f),
                    shape = RoundedCornerShape(32.dp)
                )
        )
    }
}
