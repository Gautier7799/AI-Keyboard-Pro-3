package com.example.iosdock

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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

fun startDockForegroundService(context: Context) {
    val intent = Intent(context, DockOverlayService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
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
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    var isServiceRunning by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(prefs.getBoolean("dock_is_locked", true)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasAccessibilityPermission = isAccessibilityServiceEnabled(context, DockAccessibilityService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasNotificationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Superposition
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("1. إذن الظهور فوق التطبيقات", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (hasOverlayPermission) "الحالة: مفعل ✔" else "الحالة: غير مفعل ✖",
                            color = if (hasOverlayPermission) Color(0xFF34C759) else Color(0xFFFF3B30),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (hasOverlayPermission) Color(0xFF2C2C2E) else Color(0xFF007AFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (hasOverlayPermission) "تم منح الإذن بنجاح" else "تفعيل إذن Superposition")
                        }
                    }
                }
            }

            // Card 2: Accessibility
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("2. إذن إمكانية الوصول (Accessibility)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (hasAccessibilityPermission) "الحالة: مفعل ✔" else "الحالة: غير مفعل ✖",
                            color = if (hasAccessibilityPermission) Color(0xFF34C759) else Color(0xFFFF3B30),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (hasAccessibilityPermission) Color(0xFF2C2C2E) else Color(0xFF34C759)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (hasAccessibilityPermission) "تم منح الإذن بنجاح" else "فتح إعدادات Accessibility")
                        }
                    }
                }
            }

            // Card 3: Notifications
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("3. إذن الإشعارات", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (hasNotificationPermission) "الحالة: مفعل ✔" else "الحالة: غير مفعل ✖",
                            color = if (hasNotificationPermission) Color(0xFF34C759) else Color(0xFFFF3B30),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (hasNotificationPermission) Color(0xFF2C2C2E) else Color(0xFFFF9500)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (hasNotificationPermission) "الإشعارات مفعّلة بنجاح" else "السماح بالإشعارات")
                        }
                    }
                }
            }

            // Card 4: Control & Lock Position
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("4. التحكم بشريط iOS Dock", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                        context.startActivity(intent)
                                    } else {
                                        isServiceRunning = checked
                                        if (checked) {
                                            startDockForegroundService(context)
                                        } else {
                                            context.stopService(Intent(context, DockOverlayService::class.java))
                                        }
                                    }
                                }
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2C2C2E))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("قفل موضع الشريط (Lock Position)", color = Color.White, fontSize = 14.sp)
                                Text(
                                    if (isLocked) "مثبت وتنفذ اللمسات للأيقونات" else "يمكنك تحريك مكان الشريط الآن",
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val intent = Intent(context, DockOverlayService::class.java).apply {
                                    action = DockOverlayService.ACTION_RESET_POSITION
                                }
                                context.startService(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إعادة ضبط موضع الشريط لأسفل الشاشة")
                        }
                    }
                }
            }

            // Preview
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("معاينة الشريط الشفاف النقي:", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                IosStyleEmptyDock()
            }
        }
    }
}
