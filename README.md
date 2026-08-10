# BionicsSCAN

BionicsSCAN is a comprehensive inventory management application designed specifically for FIRST Robotics Competition (FRC) teams to track power transmission components. It streamlines the process of managing belts, gears, and sprockets using barcode scanning and real-time Google Sheets synchronization.

## 🚀 Overview

The app serves as a digital librarian for your team's mechanical inventory. By linking physical components to a Google Spreadsheet via a service account, BionicsSCAN ensures that your inventory data is always accurate, whether you're in the workshop or the competition pits.

## ✨ Key Features

### 📦 Comprehensive Inventory Support
- **Multi-Category Tracking**: Manage 9mm Belts, 15mm Belts, Gears, and Sprockets in one place.
- **Pre-loaded Libraries**: Comes with standard FRC sizes (e.g., 180mm - 3120mm belts, 22T - 84T gears).
- **Unified Barcode System**: Automated barcode generation using standardized prefixes:
  - `B9-` for 9mm Belts
  - `b15-` for 15mm Belts
  - `GR-` for Gears
  - `SP-` for Sprockets

### 🔄 Advanced Synchronization
- **Online-First Architecture**: Automatically treats Google Sheets as the source of truth for real-time quantity updates.
- **Robust Offline Fallback**: If internet connection is lost, the app fails over to a local repository instantly. Checkouts are saved locally and synced back when online.
- **Dynamic Sync Indicator**: A top-bar icon showing live status:
  - 🟢 **Online**: Fully synced.
  - 🔵 **Syncing**: Data transfer in progress.
  - 🔴 **Offline**: Using local backup.

### 📷 Barcode Scanning & Printing
- **ML Kit Integration**: High-speed camera-based scanning for instant check-in/out.
- **Label Generation v2.0**: Integrated PDF engine to print standardized barcode labels for your storage bins.
- **Categorized Batch Printing**: Dedicated tabs for each inventory type to print labels in organized batches.

## 🛠️ Technical Stack

- **UI**: Jetpack Compose (Modern declarative UI)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Concurrency**: Kotlin Coroutines & Flow
- **Scanning**: Google ML Kit Barcode Scanning & CameraX
- **Database**: Local Repository with Google Sheets API v4 integration
- **Barcode Engine**: ZXing (Zebra Crossing)

## ⚙️ Setup Instructions

### 1. Google Cloud Configuration
To enable cloud sync, you must set up a Google Cloud Service Account:
1. Create a project in the [Google Cloud Console](https://console.cloud.google.com/).
2. Enable the **Google Sheets API**.
3. Create a **Service Account** and download the `credentials.json` key file.
4. Move `credentials.json` to `app/src/main/assets/`.

### 2. Spreadsheet Setup
Your Google Sheet must contain four tabs named exactly:
1. `Belt Inventory 9mm`
2. `Belt Inventory 15mm`
3. `Gear Inventory`
4. `Sprocket Inventory`

Each tab must follow this column structure (starting at row 2):
- **Column A**: Size (e.g., 180 or 84)
- **Column B**: Quantity
- **Column C**: Barcode (optional, app will auto-generate if empty)

**Important**: Share the spreadsheet with the Service Account email address (found in your `credentials.json`) with **Editor** permissions.

## 🏗️ Building & Deployment

### Commands
Use the Gradle wrapper to build the project:

- **Build APK**: `./gradlew :app:assembleDebug`
- **Install on Device**: `./gradlew :app:installDebug`

**Note**: Always use `app-debug.apk` for testing, as the unsigned release version will block Google API communication.

## 📋 Documentation
- [Release Notes](RELEASE_NOTES.md) - Detailed changelog and version history.
- [Google Sheets Setup Guide](GOOGLE_SHEETS_SETUP.md) - Step-by-step walkthrough for cloud integration.

---
*Developed for FRC Bionics. "Scanner for the pits, truth for the sheets."*
