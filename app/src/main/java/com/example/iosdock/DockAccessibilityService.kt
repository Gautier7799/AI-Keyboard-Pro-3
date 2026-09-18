package com.example.iosdock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent

class DockAccessibilityService : AccessibilityService() {

    private var lastStateIsHome: Boolean? = null
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
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val eventPackage = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""

        // 1. حظر حزم النظام الشفافة والإشعارات فقط (عدم حظر تطبيقنا لكي يختفي الشريط فور فتح الإعدادات)
        val systemIgnoredPackages = setOf(
            "com.android.systemui",                 // شريط الإشعارات واللوحة العلوية
            "android",                              // الحوارات العائمة
            "com.google.android.inputmethod.latin",  // لوحة المفاتيح
            "com.google.android.permissioncontroller"
        )

        if (eventPackage in systemIgnoredPackages) {
            return // الحفاظ على الحالة الحالية دون تغيير
        }

        if (homePackageName == null) {
            updateHomePackageName()
        }

        // 2. التحقق من حزمة اللانشر الرئيسي
        val isLauncherPackage = (eventPackage == homePackageName) || 
                                (eventPackage == "com.google.android.apps.nexuslauncher")

        // 3. كشف درج التطبيقات (App Drawer) والشرائح الأخيرة (Recents / Overview) والبحث
        val isDrawerOrRecents = className.contains("Recents", ignoreCase = true) ||
                                className.contains("Overview", ignoreCase = true) ||
                                className.contains("TaskSwitcher", ignoreCase = true) ||
                                className.contains("AllApps", ignoreCase = true) ||
                                className.contains("AppsContainer", ignoreCase = true) ||
                                className.contains("Drawer", ignoreCase = true) ||
                                className.contains("Search", ignoreCase = true)

        // الظهور فقط في الشاشة الرئيسية الحقيقية
        val isHome = isLauncherPackage && !isDrawerOrRecents

        if (lastStateIsHome == isHome) return
        lastStateIsHome = isHome

        val serviceIntent = Intent(this, DockOverlayService::class.java).apply {
            action = if (isHome) ACTION_SHOW_DOCK else ACTION_HIDE_DOCK
        }
        startService(serviceIntent)
    }

    override fun onInterrupt() {}

    companion object {
        const val ACTION_SHOW_DOCK = "com.example.iosdock.SHOW_DOCK"
        const val ACTION_HIDE_DOCK = "com.example.iosdock.HIDE_DOCK"
    }
}
