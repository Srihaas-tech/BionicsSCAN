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
