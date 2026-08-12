# BionicsSCAN: FRC Inventory Management System

BionicsSCAN is a high-performance, mobile inventory management solution purpose-built for **FIRST Robotics Competition (FRC)** teams. It optimizes the tracking of mechanical power transmission components—specifically 9mm/15mm timing belts, gears, and sprockets—combining the speed of local hardware with the reliability of cloud-based persistence.

---

## 🚀 Introduction

In the high-pressure environment of an FRC competition, knowing exactly what parts are available in the pits is critical. BionicsSCAN eliminates manual inventory logs by providing a "Scan-to-Action" workflow. By using the Bionic Inventory API as a backend, the entire team can view real-time inventory levels from any device, while pit crew members use the Android app for lightning-fast updates, even when offline.

## 🏗️ Core Architecture

The app follows modern Android development patterns to ensure stability and maintainability:

- **MVVM Pattern**: Strictly decouples the UI (Compose) from business logic (ViewModel) and data (Repository).
- **Offline-First**: Uses **Room Database** as the local source of truth and **WorkManager** to queue and replay transactions when connectivity is available.
- **Bionic Inventory API**: Communicates with the authoritative backend via **Retrofit** and **OkHttp**.
- **Reactive Data Streams**: Uses Kotlin `Flow` and `StateFlow` to push real-time updates from the database directly to the UI.

## ✨ Key Features in Detail

### 📦 Comprehensive Component Support
BionicsSCAN manages a wide range of mechanical standards:
- **HTD 5mm Belts**: Full libraries for both 9mm and 15mm widths.
- **Gears**: Supports various tooth counts Typically used in FRC drivetrains and mechanisms.
- **Sprockets**: Dedicated tracking for chain-driven systems.

### 📷 Advanced Scanning & ML
Utilizing **Google ML Kit**, the scanning engine is optimized for low-light conditions and damaged labels. It supports **Code 128** barcodes (Manufacturer Part Numbers).

### 📄 Batch Label Generation
The app includes a built-in PDF rendering engine. You can:
- Generate standard-sized labels for storage bins.
- Batch print entire categories (e.g., "Print all 15mm Belts").
- View barcodes directly in-app for manual entry if the camera is unavailable.

## 🔄 Synchronization & Offline Support

The app operates on an **Offline-First** model:

1. **Local Authoritative Source**: The Room database is the source of truth while the app is running.
2. **Transaction Queueing**: Every check-in (+1) or check-out (-1) is recorded as a pending transaction in the local database.
3. **Atomic Updates**: Local quantities are updated immediately and atomically alongside the transaction recording.
4. **Reliable Sync**: WorkManager replays pending transactions in creation order as soon as network is available.
5. **Backend Reconciliation**: Once all pending changes are sent, the app refreshes the full inventory from the Bionic Inventory API.
6. **Visual Feedback**:
    - 🟢 **Synced**: All local changes have been uploaded.
    - 🔵 **Syncing**: Currently communicating with the backend.
    - 🟠 **X pending**: Changes are queued and waiting for connection.
    - 🔴 **Sync Error**: A problem occurred during the last sync attempt.

## 🏷️ Barcode System

The app uses Manufacturer Part Numbers as barcodes:

| Category | Prefix | Example |
| :--- | :--- | :--- |
| **9mm Belts** | `B9-` | `B9-180` |
| **15mm Belts** | `B15-` | `B15-250` |
| **Gears** | `GR-` | `GR-84` |
| **Sprockets** | `SP-` | `SP-16` |

## 🛠️ Technical Stack

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material 3
- **Local Database**: Room
- **Networking**: Retrofit 2 + OkHttp 4
- **Background Sync**: WorkManager
- **Scanning**: CameraX + ML Kit Barcode Scanning
- **Barcode Engine**: ZXing (Zebra Crossing)

## ⚙️ Installation & Setup

### 1. API Configuration
Add the following to your `local.properties` file:
```properties
BIONIC_INVENTORY_API_URL=https://inventory-backend.team4909.org/api/
BIONIC_INVENTORY_API_KEY=your_api_key_here
```
The build system will expose these via `BuildConfig`.

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

---
*Developed for FRC Bionics. "Scanner for the pits, truth for the backend."*
