package com.example.iosdock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class DockAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // فحص حاسم: إظهار الشريط حصرياً وفقط على شاشة Pixel Launcher الأصلية
        val isPixelLauncher = (packageName == "com.google.android.apps.nexuslauncher")

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
