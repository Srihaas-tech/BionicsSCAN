package com.bionics.BionicsSCAN.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.bionics.BionicsSCAN.MainActivity
import com.bionics.BionicsSCAN.scanner.HardwareBarcodeScanner

/**
 * Background accessibility service that intercepts hardware barcode scanner key events
 * system-wide (from home screen, background, or other apps) and brings BionicsSCAN to
 * the foreground with the scanned item open.
 */
class BarcodeAccessibilityService : AccessibilityService() {

    private lateinit var hardwareScanner: HardwareBarcodeScanner

    override fun onCreate() {
        super.onCreate()
        hardwareScanner = HardwareBarcodeScanner { barcode ->
            Log.d(TAG, "Global hardware scan captured: $barcode")
            launchAppWithBarcode(barcode)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "BarcodeAccessibilityService connected and active")
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        }
        serviceInfo = info
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Intercept scanner burst and consume upon valid terminator
        return hardwareScanner.handleKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No UI accessibility inspection needed
    }

    override fun onInterrupt() {
        Log.w(TAG, "BarcodeAccessibilityService interrupted")
    }

    private fun launchAppWithBarcode(barcode: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_BARCODE_SCAN, barcode)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "BarcodeAccessService"

        fun isEnabled(context: Context): Boolean {
            val expectedComponent = ComponentName(context, BarcodeAccessibilityService::class.java)
            val expectedFlattened = expectedComponent.flattenToString()
            val expectedShort = expectedComponent.flattenToShortString()

            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentStr = colonSplitter.next()
                if (componentStr.equals(expectedFlattened, ignoreCase = true) ||
                    componentStr.equals(expectedShort, ignoreCase = true)
                ) {
                    return true
                }
            }
            return false
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}
