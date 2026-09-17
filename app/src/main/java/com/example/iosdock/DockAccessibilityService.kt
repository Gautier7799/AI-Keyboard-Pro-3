package com.example.iosdock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent

class DockAccessibilityService : AccessibilityService() {

    private var homePackageName: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateHomePackageName()
    }

    private fun updateHomePackageName() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            homePackageName = resolveInfo?.activityInfo?.packageName
        } catch (e: Exception) {
            homePackageName = "com.google.android.apps.nexuslauncher"
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            val className = event.className?.toString() ?: ""

            if (homePackageName == null) {
                updateHomePackageName()
            }

            // فحص واجهة التطبيقات الأخيرة (Overview/Recents) لإخفاء الشريط منها فوراً
            val isRecentsView = className.contains("Recents", ignoreCase = true) ||
                                className.contains("Overview", ignoreCase = true) ||
                                className.contains("TaskSwitcher", ignoreCase = true)

            // التأكد من التواجد في الشاشة الرئيسية فقط وليس في قائمة التنقل أو التطبيقات
            val isLauncherPackage = (packageName == homePackageName) || (packageName == "com.google.android.apps.nexuslauncher")
            val isHome = isLauncherPackage && !isRecentsView

            val serviceIntent = Intent(this, DockOverlayService::class.java).apply {
                action = if (isHome) ACTION_SHOW_DOCK else ACTION_HIDE_DOCK
            }
            startService(serviceIntent)
        }
    }

    override fun onInterrupt() {}

    companion object {
        const val ACTION_SHOW_DOCK = "com.example.iosdock.SHOW_DOCK"
        const val ACTION_HIDE_DOCK = "com.example.iosdock.HIDE_DOCK"
    }
}
