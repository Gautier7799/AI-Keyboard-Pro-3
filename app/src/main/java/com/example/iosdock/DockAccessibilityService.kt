package com.example.iosdock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent

class DockAccessibilityService : AccessibilityService() {

    private val knownHomePackages = setOf(
        "com.google.android.apps.nexuslauncher", // Pixel Launcher الخاص بهاتفك
        "com.android.launcher3",
        "com.google.android.launcher",
        "com.example.iosdock",                    // إظهار الشريط فوراً داخل التطبيق للسيادة والتأكد
        "com.android.systemui"                   // واجهة النظام العائمة
    )
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
            e.printStackTrace()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (homePackageName == null) {
                updateHomePackageName()
            }

            // إظهار الشريط عند التواجد في الشاشة الرئيسية أو داخل التطبيق
            val isHome = (packageName == homePackageName) || knownHomePackages.contains(packageName)

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
