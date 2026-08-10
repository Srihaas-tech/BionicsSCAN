# BionicsSCAN: FRC Inventory Management System

BionicsSCAN is a high-performance, mobile inventory management solution purpose-built for **FIRST Robotics Competition (FRC)** teams. It optimizes the tracking of mechanical power transmission components—specifically 9mm/15mm timing belts, gears, and sprockets—combining the speed of local hardware with the reliability of cloud-based persistence.

---

## 📖 Table of Contents
1. [Introduction](#-introduction)
2. [Core Architecture](#-core-architecture)
3. [Key Features in Detail](#-key-features-in-detail)
4. [The Synchronization Engine](#-the-synchronization-engine)
5. [Barcode Label System](#-barcode-label-system)
6. [Technical Stack](#-technical-stack)
7. [Installation & Setup](#-installation--setup)
8. [Usage Guide](#-usage-guide)
9. [Development & Troubleshooting](#-development--troubleshooting)

---

## 🚀 Introduction

In the high-pressure environment of an FRC competition, knowing exactly what parts are available in the pits is critical. BionicsSCAN eliminates manual inventory logs by providing a "Scan-to-Action" workflow. By using Google Sheets as a backend, the entire team can view real-time inventory levels from any device, while pit crew members use the Android app for lightning-fast updates.

## 🏗️ Core Architecture

The app follows modern Android development patterns to ensure stability and maintainability:

- **MVVM Pattern**: Strictly decouples the UI (Compose) from business logic (ViewModel) and data (Repository).
- **Repository Pattern**: Acts as a mediator between the `LocalBeltRepository` (offline cache) and `SheetsService` (cloud provider).
- **Reactive Data Streams**: Uses Kotlin `Flow` and `StateFlow` to push real-time updates from the database/network directly to the UI without manual refreshes.

## ✨ Key Features in Detail

### 📦 Comprehensive Component Support
BionicsSCAN isn't just for belts. It manages a wide range of mechanical standards:
- **HTD 5mm Belts**: Full libraries for both 9mm and 15mm widths.
- **Gears**: Supports various tooth counts (22T - 84T) typically used in FRC drivetrains and mechanisms.
- **Sprockets**: Dedicated tracking for chain-driven systems.

### 📷 Advanced Scanning & ML
Utilizing **Google ML Kit**, the scanning engine is optimized for low-light conditions and damaged labels. It supports **Code 128** barcodes, which allow for high data density in small physical footprints.

### 📄 Batch Label Generation
The app includes a built-in PDF rendering engine. You can:
- Generate standard-sized labels (2.5" x 1") for storage bins.
- Batch print entire categories (e.g., "Print all 15mm Belts").
- View barcodes directly in-app for manual entry if the camera is unavailable.

## 🔄 The Synchronization Engine

The sync engine is the heart of BionicsSCAN. It operates on a **Priority-Fetch, Offline-Fallback** model:

1. **Priority Sync**: On app launch or tab switch, the app attempts to pull the latest values from Google Sheets.
2. **Local Source of Truth**: The `LocalBeltRepository` stores a copy of all inventory data. This ensures the app is always functional, even with zero bars of signal.
3. **Automatic Conflict Resolution**: When checking out an item offline, the app marks the local change. Once internet is restored, the next 2-second sync interval pushes the local quantity update to the spreadsheet.
4. **Visual Feedback**:
    - 🟢 **Online**: Data matches the spreadsheet exactly.
    - 🔵 **Syncing**: Currently communicating with Google APIs.
    - 🔴 **Offline**: Connection failed; operating on local cache.

## 🏷️ Barcode Label System

To maintain consistency, the app uses a standardized prefix system. If a barcode is missing in your spreadsheet, BionicsSCAN generates one using this logic:

| Category | Prefix | Example |
| :--- | :--- | :--- |
| **9mm Belts** | `B9-` | `B9-180` |
| **15mm Belts** | `b15-` | `b15-250` |
| **Gears** | `GR-` | `GR-84` |
| **Sprockets** | `SP-` | `SP-16` |

*Note: Barcodes are case-sensitive to ensure perfect matching with the ZXing generation engine.*

## 🛠️ Technical Stack

- **Language**: Kotlin 1.9+
- **UI Framework**: Jetpack Compose with Material 3
- **Async**: Coroutines (Dispatchers.IO for networking)
- **Scanning**: CameraX + ML Kit Barcode Scanning
- **Persistence**: Google Sheets API v4
- **PDF Engine**: Android Graphics PDF Document
- **Barcode Engine**: ZXing (Zebra Crossing)

## ⚙️ Installation & Setup

### 1. Google Cloud Configuration
1. Create a project in the [Google Cloud Console](https://console.cloud.google.com/).
2. Enable the **Google Sheets API**.
3. Create a **Service Account** (not an OAuth ID).
4. Download the **JSON Key File** and rename it to `credentials.json`.
5. Place it in: `app/src/main/assets/credentials.json`.

### 2. Spreadsheet Preparation
Create a Google Sheet and add four tabs:
- `Belt Inventory 9mm`
- `Belt Inventory 15mm`
- `Gear Inventory`
- `Sprocket Inventory`

**Structure (Columns A, B, C):**
`Size` | `Quantity` | `Barcode`

### 3. Permissions
Share your spreadsheet with the **Service Account Email** (found inside your `credentials.json`) and grant it **Editor** access.

## 🏗️ Building & Deployment

Execute from the root directory:

**Windows (PowerShell):**
```powershell
./gradlew :app:assembleDebug
```

**macOS/Linux:**
```bash
./gradlew :app:assembleDebug
```

The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

## 📋 Usage Guide

1. **Home Screen**: Select your category using the top tabs.
2. **Checkout**: Tap a component in the list or scan its barcode to decrease quantity by 1.
3. **Check-in**: Tap an item to enter the detail view, then tap the "+" button.
4. **Labels**: Tap the "Labels" button to enter the print preview. Switch tabs to print different categories.
5. **Sync**: Tap the refresh icon at the top right to force an immediate cloud update.

## 🛠️ Development & Troubleshooting

- **Sync Status is always Red**: Check that your `credentials.json` is in the assets folder and that the device has internet access.
- **Scanning not working**: Ensure camera permissions are granted in Android Settings.
- **Build Fails**: Ensure you are using Java 17+ and the latest Android Studio (Ladybug or newer).

---
*Developed for FRC Bionics. "Scanner for the pits, truth for the sheets."*
