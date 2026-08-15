# BionicsSCAN Release Notes

All notable changes and milestones for the **BionicsSCAN** FRC inventory management and scanning platform are documented in this file.

---

## 🚀 [v3.0.0] - August 14, 2026

### 🌟 Major Milestone: System-Wide Background Hardware Scanning & Zero-Leak Interception

Version 3.0.0 transforms BionicsSCAN from an in-app utility into a system-wide hardware inventory hub. Physical Bluetooth and USB barcode scanners can now trigger scans and wake the application from **anywhere on the device** (Home screen, browser, or backgrounded apps), with zero keystroke leakage into Google Search.

---

### ✨ New Features

#### 1. System-Wide Background Hardware Scanner (`BarcodeAccessibilityService`)
* **Global Scan Capture**: Integrates an Android Accessibility Service configured with `FLAG_REQUEST_FILTER_KEY_EVENTS` to intercept physical scanner bursts across the entire operating system.
* **Instant App Awakening**: Automatically launches `MainActivity` with `FLAG_ACTIVITY_NEW_TASK` and `FLAG_ACTIVITY_SINGLE_TOP` whenever an inventory barcode is read.
* **Warm & Cold Intent Delivery**: Handles incoming barcode payloads seamlessly whether the app is running in the background, in memory, or starting cold from a killed process.

#### 2. Strict 2-Letter Prefix Detection & Keystroke Interception
* **Instant Recognition**: Analyzes the first two characters of any keystroke burst. If they match a recognized category prefix (`B9`, `B1`/`B15`, `GR`, `SP`), the system locks into inventory scan mode.
* **Google Search Blocking (Zero-Leak)**: Once a prefix is confirmed, all incoming keystrokes and the terminating `Enter` key are consumed by the service. This prevents scanned strings (e.g. `B9-325`) from typing into Google Search bars or launching web queries.
* **Regular Keyboard Preservation**: Keystrokes that do not match inventory prefixes (e.g. typing notes or browser URLs) pass through untouched.

#### 3. Auto-Flush Fallback Engine
* **Terminator Independence**: Added an intelligent 250ms idle burst timer. If a physical scanner is configured without an `Enter` suffix, the scan buffer automatically flushes and executes without requiring manual keypresses.

#### 4. Normalized Barcode & Database Lookups
* **Hyphen & Spacing Agnostic**: Room DAO queries and ViewModel search filters now utilize normalized string matching (`UPPER(REPLACE(barcode, '-', ''))`).
* **Format Freedom**: Lookups resolve accurately for `B9-325`, `B9325`, `b9 325`, `GR-48`, `GR48`, `SP-18`, `SP18`, `B15-450`, and `B15450`.

#### 5. Background Scanner Setup Helper & Status Banner
* **Live Status Detection**: `BeltListScreen` monitors the real-time status of the accessibility service.
* **1-Tap Quick Setup**: Displays a sleek setup card with a direct shortcut to Android **Accessibility Settings** if the service has not yet been enabled.
* **Android 13/14/15 Restricted Settings Compatibility**: Fully documented and ADB-scripted bypass for Android's sideloaded app security restrictions.

---

### 🔧 Improvements & Fixes
* ✅ **Lifecycle Thread Safety**: Resolved potential `UninitializedPropertyAccessException` on cold start by buffering incoming scan intents until Jetpack Compose `NavHostController` is ready.
* ✅ **Target SDK Alignment**: Fully compatible with Android 15 (API 35) and Gradle 9.5+.
* ✅ **Memory Footprint**: Eliminated redundant string allocations during scanner burst parsing.

---

## 📦 [v2.5.0] - August 12, 2026

### 🌟 Milestone: REST API Migration & Offline-First Room Architecture

Version 2.5.0 marked a complete architectural rebuild, migrating away from Google Sheets to a dedicated, high-speed **Bionic Inventory REST API** backed by a local **Room Database**.

---

### ✨ Features
* **Bionic Inventory REST API**: Replaced Google Sheets API with a custom, authenticated REST backend (`Retrofit 2` + `OkHttp 4` + `Kotlinx Serialization`).
* **Offline-First Room DB**: SQLite local database stores parts, quantities, and metadata. App is 100% operational with zero internet connectivity.
* **WorkManager Transaction Replay Engine**: All check-ins and check-outs are recorded as atomic transactions and automatically synchronized when network connectivity is restored.
* **Bi-Directional Background Sync**:
  * **App $\rightarrow$ Cloud**: Real-time push for all local transactions.
  * **Cloud $\rightarrow$ App**: 15-second background polling keeps local stock updated with changes made by other team members.
* **Modernized Material 3 Dashboard**:
  * Visual metrics for Total Sizes, Total Units, Low Stock, and Out of Stock.
  * Category tabs with dynamic count badges (e.g. `Gears [18]`).
  * Live Sync Status Banner (🟢 *Synced*, 🔵 *Syncing*, 🟠 *Pending*, 🔴 *Error*).
* **Camera Viewfinder & Manual Fallback**: ML Kit Code 128 scanner with animated laser guides, camera pause/resume controls, and manual ID search.
* **Batch PDF Label Printing**: In-app label generation for Avery sheets with direct Android `PrintManager` and Share Sheet export.

---

### 🔧 Fixes
* ✅ **Numerical Sorting**: Corrected numerical sorting logic so 1000mm belts sort after 950mm instead of alphabetically.
* ✅ **Duplicate Prevention**: Implemented `ExistingWorkPolicy.REPLACE` in WorkManager to eliminate duplicate transaction submissions.

---

## 📦 [v1.1.0] - August 9, 2026

### ✨ Features
* **Expanded Motion Transmission Catalog**:
  * **Belt 9mm**: 32 sizes (180mm - 1250mm).
  * **Belt 15mm**: 29 sizes (250mm - 3120mm).
  * **Gears**: 18 tooth counts (22T - 84T).
  * **Sprockets**: Initial library for #25 and #35 chain sprockets.
* **Dynamic Tab Navigation**: 4-category navigation bar.
* **Standardized Barcode System**: Standardized part numbers (`B9-`, `B15-`, `GR-`, `SP-`).
* **Category Batch Label Printing**: Print all labels for a specific category in one tap.

---

## 📦 [v1.0.0] - August 8, 2026

### ✨ Initial Stable Release
* **Camera Barcode Scanning**: First implementation using CameraX and Google ML Kit.
* **Belt Inventory Management**: Core 9mm belt tracking (180mm - 1250mm).
* **1-Tap Checkout / Checkin**: Quantity decrement and increment actions.
* **Google Sheets Cloud Integration**: Service account authentication and periodic syncing.
* **ZXing Barcode Generation**: Bitmap rendering for barcode labels.

---

*BionicsSCAN is developed and maintained by **FRC Team 4909 Bionics**.*
