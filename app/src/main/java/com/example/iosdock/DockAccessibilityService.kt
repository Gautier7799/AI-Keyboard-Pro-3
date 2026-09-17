package com.example.iosdock

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class DockAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // الخدمة مفعلة وجاهزة
    }

    override fun onInterrupt() {
        // عند إيقاف الخدمة
    }
}
