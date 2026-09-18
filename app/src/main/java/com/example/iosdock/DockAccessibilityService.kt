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

        // الاستجابة فقط لتغير النوافذ الشاشة وتجاهل باقي اللمسات
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val eventPackage = event.packageName?.toString() ?: return

        // 1. القضاء على الحلقة المفرغة: تجاهل حزم النظام وتطبيقنا لتفادي الظهور والاختفاء المتكرر
        val ignoredPackages = setOf(
            packageName,                           // com.example.iosdock (تطبيقنا نفسه)
            "com.android.systemui",               // شريط النظام العلوي والإشعارات
            "android",                             // حوارات أندرويد
            "com.google.android.inputmethod.latin", // لوحة المفاتيح
            "com.google.android.permissioncontroller"
        )

        if (eventPackage in ignoredPackages) {
            return // تجاهل الحدث تماماً والحفاظ على الحالة الحالية للشريط
        }

        if (homePackageName == null) {
            updateHomePackageName()
        }

        // 2. التحقق من حزمة اللانشر الرئيسي
        val isLauncherPackage = (eventPackage == homePackageName) || 
                                (eventPackage == "com.google.android.apps.nexuslauncher")

        val className = event.className?.toString() ?: ""

        // 3. استثناء واجهة التطبيقات الأخيرة Recents / Overview
        val isRecentsView = className.contains("Recents", ignoreCase = true) ||
                            className.contains("Overview", ignoreCase = true) ||
                            className.contains("TaskSwitcher", ignoreCase = true)

        val isHome = isLauncherPackage && !isRecentsView

        // 4. إرسال الأوامر فقط عند تغير الحالة الفعلية لمنع الثقل والـ Lag
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
