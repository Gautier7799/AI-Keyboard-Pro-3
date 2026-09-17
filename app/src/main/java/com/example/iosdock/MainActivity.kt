package com.example.iosdock

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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

fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
    val expectedComponentName = ComponentName(context, serviceClass)
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val stringSplitter = TextUtils.SimpleStringSplitter(':')
    stringSplitter.setString(enabledServicesSetting)
    while (stringSplitter.hasNext()) {
        val componentNameString = stringSplitter.next()
        val enabledComponentName = ComponentName.unflattenFromString(componentNameString)
        if (enabledComponentName != null && enabledComponentName == expectedComponentName) {
            return true
        }
    }
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs = remember { context.getSharedPreferences("dock_prefs", Context.MODE_PRIVATE) }

    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasAccessibilityPermission by remember {
        mutableStateOf(isAccessibilityServiceEnabled(context, DockAccessibilityService::class.java))
    }
    var isServiceRunning by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(prefs.getBoolean("dock_is_locked", true)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasAccessibilityPermission = isAccessibilityServiceEnabled(context, DockAccessibilityService::class.java)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasOverlayPermission) "الحالة: مفعل ✔" else "الحالة: غير مفعل ✖",
                        color = if (hasOverlayPermission) Color(0xFF34C759) else Color(0xFFFF3B30),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasOverlayPermission) Color(0xFF2C2C2E) else Color(0xFF007AFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (hasOverlayPermission) "تم منح الإذن بنجاح" else "تفعيل إذن Superposition الآن")
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasAccessibilityPermission) "الحالة: مفعل ✔" else "الحالة: غير مفعل ✖",
                        color = if (hasAccessibilityPermission) Color(0xFF34C759) else Color(0xFFFF3B30),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasAccessibilityPermission) Color(0xFF2C2C2E) else Color(0xFF34C759)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (hasAccessibilityPermission) "تم منح الإذن بنجاح" else "فتح إعدادات Accessibility بالنظام")
                    }
                }
            }

            // Card 3: Toggle Service & Lock Position
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. التحكم بشريط iOS Dock",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تشغيل الشريط العائم", color = Color.White, fontSize = 14.sp)
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

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF2C2C2E))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("قفل موضع الشريط (Lock Position)", color = Color.White, fontSize = 14.sp)
                            Text(
                                text = if (isLocked) "الموضع مفيّث وتعمل لمسات النظام خلفه" else "يمكنك سحب الشريط لمكانه الآن",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isLocked,
                            onCheckedChange = { checked ->
                                isLocked = checked
                                prefs.edit().putBoolean("dock_is_locked", checked).apply()
                                val intent = Intent(context, DockOverlayService::class.java).apply {
                                    action = DockOverlayService.ACTION_UPDATE_LOCK_STATE
                                }
                                context.startService(intent)
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
