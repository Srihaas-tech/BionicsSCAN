# BionicsSCAN v2.5.0 Release Notes

**Release Date:** August 12, 2026

## Overview
BionicsSCAN v2.5.0 is the most significant update in the app's history. We have migrated away from Google Sheets to a custom **Bionic Inventory REST API**, transforming the app into a professional-grade, offline-first power transmission management tool. This version introduces a bi-directional synchronization engine, a completely redesigned dashboard, and a guided scanner experience.

## Features

### 🚀 Bionic Inventory REST API Migration
- **REST Integration**: Replaced the legacy Google Sheets engine with a dedicated backend API for faster response times and improved reliability.
- **Offline-First Architecture**: Uses a local **Room Database** as the primary source of truth. The app is fully functional with zero signal.
- **Queued Transactions**: Check-ins and check-outs are queued locally and automatically replayed to the server once a connection is restored.
- **Bi-Directional Live Sync**: 
    - **App → Backend**: Changes sync instantly when online.
    - **Backend → App**: Automatic background polling refreshes your inventory every 15 seconds while the app is open.

### 📊 Redesigned Dashboard & UI
- **Branded Header**: New "Bionics 4909" branding and visual hierarchy matching the web platform.
- **Live Connection Banner**: Real-time status bar showing "Synced," "Syncing," or "Pending" counts with a "Last checked" timestamp.
- **Dashboard Metrics**: Four new summary cards providing instant insight into:
    - **Total Sizes** in the current category.
    - **Total Units** currently in stock.
    - **Low Stock** items (clickable for a filtered view).
    - **Out of Stock** items (clickable for a filtered view).
- **Live Search**: Integrated search bar to filter inventory by size or barcode ID instantly.
- **Dynamic Tab Counts**: Category tabs now display the number of items they contain (e.g., "Gears [18]").

### 📷 Professional Scanner Experience
- **Visual Guides**: Added a scan frame with corner guides and an animated red scan line.
- **Manual Lookup**: Added an offline-capable manual entry field for situations where the camera cannot scan or for quick ID lookups.
- **Camera Controls**: Explicit "Start Camera" and "Stop Camera" buttons to manage battery and scanner state.

### 🔍 Rich Item Details & History
- **Live Barcode Rendering**: High-contrast Code 128 barcodes are now rendered directly on the detail page.
- **Quantity Panel**: Redesigned quantity controls with dedicated "Check in" and "Check out" actions and visual status feedback.
- **Recent Activity**: A new history section shows your local transactions and their synchronization status (e.g., "Pending sync").
- **Type-Aware Terminology**: The UI now uses "mm" for belts and "T" (Teeth) for gears and sprockets automatically.

### 📄 Barcode Label Enhancements
- **Share & Save**: In addition to printing, you can now share the generated PDF to save it to your device or send it via email.

## Improvements & Fixes

### Fixed Issues
- ✅ **Numerical Sorting**: Fixed a sorting bug where lengths were sorted alphabetically (e.g., 1250 appearing before 180).
- ✅ **Atomic Transactions**: Ensured that local quantity updates and transaction queueing happen simultaneously to prevent data loss.
- ✅ **Duplicate Suppression**: Implemented WorkManager unique work policies to prevent duplicate transaction replays.
- ✅ **UI Stability**: Fixed "StateFlow.value" warnings and improved recomposition performance.

### Performance
- **Optimized Networking**: Reduced JSON payload size by omitting empty fields and using strict serialization rules.
- **Background Sync**: Optimized retry backoff logic for faster recovery after network interruptions.

## Technical Details

### Requirements
- Android 8.0+ (API 24+)
- **Bionic Inventory API Key** (Configured in `local.properties`)
- Internet connection (Required for initial setup and synchronization)

### Architecture
- **Language**: Kotlin 2.0
- **UI**: Jetpack Compose (Material 3)
- **Database**: Room Persistence Library
- **Networking**: Retrofit 2 + OkHttp 4
- **Sync**: WorkManager 2.9

---

**Version:** 2.5.0  
**Build:** 5  
**Target SDK:** Android 15 (API 35)

---

# BionicsSCAN v1.1.0 Release Notes

**Release Date:** August 9, 2026

## Overview
BionicsSCAN v1.1.0 is a major update that expands the application beyond simple belt tracking into a comprehensive FRC power transmission inventory tool. This release introduces support for 15mm belts, Gears, and Sprockets, alongside a completely overhauled synchronization engine that prioritizes cloud data while maintaining a robust offline fallback.

## Features

### Expanded Inventory Support
- **9mm Belts** - Complete library of 32 sizes (180mm - 1250mm).
- **15mm Belts** - Added 29 new sizes (250mm - 3120mm).
- **Gears** - New category supporting 18 gear sizes (22T - 84T).
- **Sprockets** - Initial support for common Sprocket sizes.
- **Unified Barcode Linking** - Every item is now hard-linked to a standardized barcode format (`B9-`, `b15-`, `GR-`, `SP-`) for 100% scan accuracy.

### Enhanced Synchronization Engine
- **Online-First Logic** - The app now automatically treats Google Sheets as the source of truth, fetching live data on every tab switch.
- **Robust Offline Fallback** - If a connection fails, the app instantly switches to the local repository, allowing checkouts to be saved locally until internet is restored.
- **Smart Sync Status Indicator** - A new dynamic cloud icon in the top bar:
    - 🟢 **Green (Done)**: Successfully connected and synced with the cloud.
    - 🔵 **Blue (Syncing)**: Data transfer currently in progress.
    - 🔴 **Red (Offline)**: Network unavailable; using local fallback data.

### Barcode Label Printing v2.0
- **4-Tab Navigation** - Organized the printing screen into categories: 9mm, 15mm, Gears, and Sprockets.
- **Category-Specific Batching** - Print all barcodes for a specific category with one tap.
- **Dynamic Label Formatting** - Labels now automatically display the correct units (`mm` for belts, `T` for teeth) and category names (e.g., "Gear: 48T").

## Improvements & Fixes

### Fixed Issues
- ✅ **Fixed Naming Bug**: Resolved issue where the "Sprocket" tab was incorrectly labeled as "Tabs".
- ✅ **Barcode Case-Sensitivity**: Standardized all prefixes to ensure scanning works regardless of how the barcode was generated.
- ✅ **Improved Error Resiliency**: Network timeouts and "UnknownHostExceptions" no longer trigger popups; the app now fails over to local data silently.
- ✅ **Fixed Inventory Overlap**: Ensured that items in different categories with the same numeric ID (e.g., 250mm belt vs 250mm 15mm belt) are tracked separately.

### Performance
- **Priority Loading**: Optimized the initial load sequence to show local data instantly while the online sync fetches in the background.
- **PDF Optimization**: Reduced generated PDF file size for faster wireless printing.

## Technical Details

### Requirements
- Android 6.0+ (API 24)
- Google Sheets shared with service account (for Online mode)
- **New:** `androidx.compose.material:material-icons-extended` dependency for advanced status icons.

### Architecture
- **MVVM Pattern** - Enhanced with a 3-state `SyncStatus` state machine.
- **Google Sheets API v4** - Multi-sheet support for Gears, Sprockets, and multiple belt widths.

## Spreadsheet Format
To use the new categories, your Google Sheet should now have four tabs named exactly:
1. `Belt Inventory 9mm`
2. `Belt Inventory 15mm`
3. `Gear Inventory`
4. `Sprocket Inventory`

Each tab follows the same format: **Column A (Size), Column B (Quantity), Column C (Barcode).**

---

**Version:** 1.1.0  
**Build:** 2  
**Target SDK:** Android 14 (API 36)

---

# BionicsSCAN v1.0.0 Release Notes

**Release Date:** August 8, 2026

## Overview
BionicsSCAN v1.0.0 is the first stable release of the FRC Belt Inventory Management application. This release provides a comprehensive solution for tracking and managing belt inventory with barcode scanning, Google Sheets integration, and real-time synchronization.

## Features

### Core Functionality
- **Barcode Scanning** - Quickly scan belts using your device's camera with ML Kit integration
- **Belt Inventory Tracking** - View complete inventory of 32 belt sizes (180mm - 1250mm)
- **Checkout/Checkin System** - Track belt usage with one-tap checkout and checkin
- **Real-time Synchronization** - Automatic sync with Google Sheets every 2 seconds
- **Local Fallback** - Works offline with local data when Google Sheets isn't available

### User Interface
- **Home Screen** - View all belts with quantity status at a glance
- **Scan Screen** - Camera-based barcode scanning for quick inventory updates
- **Belt Detail Screen** - Detailed view with checkout/checkin controls
- **Barcode Labels Screen** - Generate and view barcode labels for belts
- **Sync Status Indicator** - Visual indicator showing sync status with cloud

### Integration
- **Google Sheets API** - Seamless integration with Google Sheets for data persistence
- **Service Account Authentication** - Secure authentication using Google Cloud service accounts
- **Automatic Updates** - Data automatically syncs with your spreadsheet

## Improvements & Fixes

### Fixed Issues
- ✅ Resolved PKCS8 credential parsing errors
- ✅ Fixed checkout/checkin belt quantity updates
- ✅ Fixed UI refresh after checkout/checkin operations
- ✅ Corrected Google Sheets API range formatting for sheet names with spaces
- ✅ Fixed belt ID matching between local and remote repositories
- ✅ Improved error handling for invalid credentials

### Performance
- Optimized periodic sync interval (every 2 seconds)
- Efficient local data caching to reduce API calls
- Smooth UI updates using Jetpack Compose

## Technical Details

### Requirements
- Android 6.0+ (API 24)
- Camera permission for barcode scanning
- Internet connection for Google Sheets sync (optional for offline use)

### Architecture
- **MVVM Pattern** - Clean separation of concerns with ViewModel
- **Jetpack Compose** - Modern declarative UI framework
- **Coroutines** - Asynchronous operations for network and database calls
- **Flow/StateFlow** - Reactive data binding for UI updates
- **Google Sheets API v4** - Real-time cloud integration

### Dependencies
- AndroidX Compose Material3
- CameraX for barcode scanning
- ML Kit Barcode Scanning
- Google Sheets API Client
- ZXing for barcode generation

## Setup Instructions

### Initial Configuration
1. Download and install the APK on your Android device
2. Obtain Google Sheets API credentials:
   - Create a Google Cloud project
   - Enable Google Sheets API
   - Create a service account and download credentials.json
3. Place `credentials.json` in `app/src/main/assets/` and rebuild
4. Share your Google Sheet with the service account email

### Spreadsheet Format
Your Google Sheet should have:
- **Column A**: Belt Length (in mm)
- **Column B**: Quantity
- **Data starts at Row 2** (Row 1 can be headers)
- **Sheet name**: Configure in app settings

## Known Limitations
- Offline mode uses local data only (changes won't sync until online)
- Google Sheets must be shared with service account for sync to work
- Barcode generation currently shows auto-generated barcodes (format: BELT-[length])

## Future Roadmap
- Custom barcode format support
- Batch export/import functionality
- Advanced reporting and analytics
- Multi-user support with role-based access
- Offline sync queue for delayed uploads

## Support
For issues or feature requests, please contact the development team or file an issue in the project repository.

---

**Version:** 1.0.0  
**Build:** 1  
**Target SDK:** Android 14 (API 36)  
**Min SDK:** Android 6.0 (API 24)
