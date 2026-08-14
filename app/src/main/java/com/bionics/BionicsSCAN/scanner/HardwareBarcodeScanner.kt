package com.bionics.BionicsSCAN.scanner

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.bionics.BionicsSCAN.data.InventoryType

/**
 * App-wide and system-wide handler for Bluetooth/USB HID barcode scanners (e.g. Zebra, Inateck, Eyoyo).
 *
 * Scanners present as physical keyboards and send rapid bursts of key events.
 *
 * If the first two characters match a known Bionics prefix ("B9", "B1", "B15", "GR", "SP"):
 * 1. Key events are consumed immediately so they NEVER leak into external apps (like Google Search).
 * 2. Scanned barcodes (even without Enter or with/without dashes) are captured and routed directly to BionicsSCAN.
 */
class HardwareBarcodeScanner(
    private val consumeDuringScan: Boolean = true,
    private val onBarcodeScanned: (String) -> Unit
) {

    private val buffer = StringBuilder()
    private var lastKeyTime = 0L
    private var isConfirmedPrefix = false

    private val handler = Handler(Looper.getMainLooper())
    private val autoFlushRunnable = Runnable {
        flushBuffer()
    }

    /**
     * Feed every key event dispatched by the Activity or AccessibilityService into this method.
     *
     * @return true if the event was consumed as part of a hardware scan.
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount > 0) {
            return isConfirmedPrefix && consumeDuringScan
        }

        when (event.keyCode) {
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_TAB -> {
                handler.removeCallbacks(autoFlushRunnable)
                return flushBuffer()
            }
        }

        val char = event.unicodeChar.toChar()
        if (char.code !in PRINTABLE_CHAR_RANGE) {
            return isConfirmedPrefix && consumeDuringScan
        }

        val now = System.currentTimeMillis()
        if (buffer.isNotEmpty() && now - lastKeyTime > KEY_BURST_TIMEOUT_MS) {
            buffer.clear()
            isConfirmedPrefix = false
        }

        if (buffer.isEmpty()) {
            val upper = char.uppercaseChar()
            // First character of B9/B15 (B), GR (G), or SP (S)
            if (upper != 'B' && upper != 'G' && upper != 'S') {
                return false
            }
            buffer.append(char)
            lastKeyTime = now
            isConfirmedPrefix = false
            handler.removeCallbacks(autoFlushRunnable)
            handler.postDelayed(autoFlushRunnable, SCAN_IDLE_TIMEOUT_MS)
            return false
        } else {
            buffer.append(char)
            lastKeyTime = now

            if (!isConfirmedPrefix) {
                if (buffer.length >= 2) {
                    val prefix2 = buffer.substring(0, 2).uppercase()
                    if (isKnownPrefix(prefix2)) {
                        isConfirmedPrefix = true
                    } else {
                        // Not a recognized prefix (e.g. user is typing "BO", "GA", etc.)
                        buffer.clear()
                        isConfirmedPrefix = false
                        handler.removeCallbacks(autoFlushRunnable)
                        return false
                    }
                }
            }

            handler.removeCallbacks(autoFlushRunnable)
            handler.postDelayed(autoFlushRunnable, SCAN_IDLE_TIMEOUT_MS)

            // Once the prefix is confirmed, consume the keys so they don't leak into Google Search
            return isConfirmedPrefix && consumeDuringScan
        }
    }

    private fun flushBuffer(): Boolean {
        handler.removeCallbacks(autoFlushRunnable)
        if (buffer.isEmpty()) {
            isConfirmedPrefix = false
            return false
        }

        val scanCode = buffer.toString().trim().uppercase()
        buffer.clear()
        val wasConfirmed = isConfirmedPrefix
        isConfirmedPrefix = false
        lastKeyTime = 0L

        if (isBionicsBarcode(scanCode)) {
            onBarcodeScanned(scanCode)
            return true
        }
        return wasConfirmed
    }

    companion object {
        /**
         * Checks if the first 2 characters match any known inventory prefix.
         */
        fun isKnownPrefix(prefix2: String): Boolean {
            val upper = prefix2.uppercase()
            return upper == "B9" || upper == "B1" || upper == "GR" || upper == "SP"
        }

        /**
         * Checks if a string starts with a known Bionics barcode prefix.
         */
        fun isBionicsBarcode(value: String): Boolean {
            val upper = value.trim().uppercase()
            return upper.startsWith("B9") ||
                    upper.startsWith("B15") ||
                    upper.startsWith("B1") ||
                    upper.startsWith("GR") ||
                    upper.startsWith("SP")
        }

        /**
         * Maps a barcode prefix to its inventory category. Used to route a
         * hardware scan to the window for that category.
         */
        fun inventoryTypeForBarcode(barcode: String): InventoryType? {
            val upper = barcode.trim().uppercase()
            return when {
                upper.startsWith("B15") || upper.startsWith("B1-") || upper.startsWith("B1") -> InventoryType.BELT_15MM
                upper.startsWith("B9") -> InventoryType.BELT_9MM
                upper.startsWith("GR") -> InventoryType.GEAR
                upper.startsWith("SP") -> InventoryType.SPROCKET
                else -> null
            }
        }

        /** Max gap between keystrokes for them to count as one scan burst. */
        private const val KEY_BURST_TIMEOUT_MS = 300L

        /** Buffer is flushed after this much silence so scans without an Enter key still fire. */
        private const val SCAN_IDLE_TIMEOUT_MS = 250L

        private val PRINTABLE_CHAR_RANGE = 32..126
    }
}
