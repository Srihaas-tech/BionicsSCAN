package com.bionics.BionicsSCAN.scanner

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.bionics.BionicsSCAN.data.InventoryType

/**
 * App-wide handler for Bluetooth/USB HID barcode scanners such as the Zebra DS3678.
 *
 * The DS3678 presents itself as a hardware keyboard: it delivers the barcode as a
 * rapid burst of key events terminated by Enter (or Tab, depending on configuration).
 * Camera-based scanning is completely untouched by this class.
 *
 * Only strings that begin with a Bionics prefix ("B15-", "B9-", "GR-" or "SP-") are
 * treated as scans, so regular keyboard input is never intercepted.
 */
class HardwareBarcodeScanner(
    private val onBarcodeScanned: (String) -> Unit
) {

    private val buffer = StringBuilder()
    private var lastKeyTime = 0L

    private val handler = Handler(Looper.getMainLooper())
    private val resetBufferRunnable = Runnable {
        buffer.clear()
        lastKeyTime = 0L
    }

    /**
     * Feed every key event dispatched by the Activity into this method.
     *
     * @return true if the event was consumed as part of a hardware scan.
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount > 0) {
            return false
        }

        when (event.keyCode) {
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_TAB -> return flushOnTerminator()
        }

        val char = event.unicodeChar.toChar()
        if (char.code !in PRINTABLE_CHAR_RANGE) {
            return false
        }

        if (buffer.isEmpty()) {
            // Only begin listening on the first letter of a possible prefix.
            if (char.uppercaseChar() != 'B' &&
                char.uppercaseChar() != 'G' &&
                char.uppercaseChar() != 'S'
            ) {
                return false
            }
        } else {
            // A real scanner bursts keystrokes with almost no gaps between them.
            // A long gap means the user was typing normally, so start fresh.
            val now = System.currentTimeMillis()
            if (now - lastKeyTime > KEY_BURST_TIMEOUT_MS) {
                buffer.clear()
            }
        }

        buffer.append(char)
        lastKeyTime = System.currentTimeMillis()
        handler.removeCallbacks(resetBufferRunnable)
        handler.postDelayed(resetBufferRunnable, SCAN_IDLE_TIMEOUT_MS)

        // Characters pass through so a focused text field still shows them.
        return false
    }

    private fun flushOnTerminator(): Boolean {
        handler.removeCallbacks(resetBufferRunnable)

        if (buffer.isEmpty()) {
            return false
        }

        val scanCode = buffer.toString().trim().uppercase()
        buffer.clear()
        lastKeyTime = 0L

        if (isBionicsBarcode(scanCode)) {
            onBarcodeScanned(scanCode)
            return true
        }
        return false
    }

    private fun isBionicsBarcode(value: String): Boolean =
        value.matches(BIONICS_BARCODE_PATTERN)

    companion object {
        private val BIONICS_BARCODE_PATTERN = Regex("^(B15|B9|GR|SP)-.+")

        /**
         * Maps a barcode prefix to its inventory category. Used to route a
         * hardware scan to the window for that category.
         */
        fun inventoryTypeForBarcode(barcode: String): InventoryType? {
            val prefix = barcode.substringBefore('-')
            return when (prefix) {
                "B9" -> InventoryType.BELT_9MM
                "B15" -> InventoryType.BELT_15MM
                "GR" -> InventoryType.GEAR
                "SP" -> InventoryType.SPROCKET
                else -> null
            }
        }

        /** Max gap between keystrokes for them to count as one scan burst. */
        private const val KEY_BURST_TIMEOUT_MS = 300L

        /** Buffer is dropped after this much silence so a stray Enter never fires. */
        private const val SCAN_IDLE_TIMEOUT_MS = 600L

        private val PRINTABLE_CHAR_RANGE = 32..126
    }
}
