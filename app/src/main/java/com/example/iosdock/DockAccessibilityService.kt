package com.example.iosdock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class DockAccessibilityService : AccessibilityService() {

    private var lastStateIsHome: Boolean? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // 1. تصفية الأحداث: استجابة لتغير الشاشات فقط وتجاهل التمرير واللمس
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // 2. التحقق الحصري من الشاشة الرئيسية لـ Pixel Launcher
        val isPixelLauncher = (packageName == "com.google.android.apps.nexuslauncher")

        // 3. منع تكرار الطلبات إذا لم تتغير الشاشة الفعلية
        if (lastStateIsHome == isPixelLauncher) return
        lastStateIsHome = isPixelLauncher

        val serviceIntent = Intent(this, DockOverlayService::class.java).apply {
            action = if (isPixelLauncher) ACTION_SHOW_DOCK else ACTION_HIDE_DOCK
        }
        startService(serviceIntent)
    }

    override fun onInterrupt() {}

    companion object {
        const val ACTION_SHOW_DOCK = "com.example.iosdock.SHOW_DOCK"
        const val ACTION_HIDE_DOCK = "com.example.iosdock.HIDE_DOCK"
    }
}
