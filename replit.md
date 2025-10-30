# Battery Monitor - Android App Project

## Overview

This is a native Android application for battery monitoring with real-time data display and a resizable home screen widget. The project was created on October 30, 2025, as a best-effort implementation for export to Android Studio.

**Important**: This is a native Android app that cannot run in the Replit environment. You must export this code and build it in Android Studio on your local machine.

## Project Purpose

Create a comprehensive battery monitoring app for Android devices that provides:
- Real-time voltage, current, and power measurements
- Estimated time to full charge
- Estimated time to full discharge
- A resizable home screen widget with live data

## Project Status

**Current State**: Complete Android app structure ready for export to Android Studio

**Completion Date**: October 30, 2025

## Project Architecture

### Technology Stack
- **Language**: Kotlin (100%)
- **Build System**: Gradle 8.2
- **Android SDK**: Min API 21, Target API 34
- **UI Framework**: Material Design Components + View Binding
- **Architecture Pattern**: MVVM with reactive updates

### Project Structure

```
BatteryMonitor/
├── app/
│   ├── build.gradle                 # App-level Gradle configuration
│   ├── proguard-rules.pro          # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml      # App manifest with permissions
│       ├── java/com/batterymonitor/
│       │   ├── BatteryData.kt       # Data model for battery info
│       │   ├── BatteryMonitor.kt    # Core monitoring logic
│       │   ├── MainActivity.kt      # Main UI screen
│       │   └── BatteryWidget.kt     # Home screen widget provider
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml    # Main screen layout
│           │   └── battery_widget.xml   # Widget layout
│           ├── values/
│           │   ├── strings.xml          # App strings
│           │   ├── colors.xml           # Color definitions
│           │   └── themes.xml           # Material themes
│           ├── drawable/
│           │   └── widget_background.xml # Widget background
│           └── xml/
│               └── battery_widget_info.xml # Widget configuration
├── build.gradle                     # Project-level Gradle
├── settings.gradle                  # Gradle settings
├── gradle.properties               # Gradle properties
└── README.md                        # Build and setup instructions
```

## Key Features Implemented

### 1. Real-Time Battery Monitoring (`BatteryMonitor.kt`)
- Uses Android BatteryManager API
- Reads voltage in microvolts (converted to volts)
- Reads current in microamps (converted to milliamps)
- Calculates power consumption (voltage × current)
- Monitors battery level, temperature, health, status
- Updates every second in main app

### 2. Time Estimation Algorithm
- Adaptive learning algorithm that improves over time
- Tracks battery level changes with timestamps
- Maintains rolling average of last 10 rate measurements
- Calculates charge time: (100 - current%) / charge_rate
- Calculates discharge time: current% / discharge_rate
- Filters outliers for more accurate predictions

### 3. Main Activity UI (`MainActivity.kt`)
- Material Design card-based layout
- Color-coded status card (green when charging, orange/red by level)
- Real-time data display with 1-second updates
- Shows voltage, current, power, temperature
- Displays time to full charge OR time to discharge
- Battery health and technology information

### 4. Resizable Widget (`BatteryWidget.kt`)
- Home screen widget with configurable size
- Shows battery level, voltage, current, power
- Displays charge/discharge time estimate
- Updates every 2 seconds via periodic broadcasts
- Tap widget to open main app
- Transparent blue background with rounded corners

## How to Use This Project

### Export from Replit

1. Click the three-dot menu in Replit
2. Select "Download as ZIP"
3. Extract the ZIP file on your computer

### Import to Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the extracted folder
4. Wait for Gradle sync to complete

### Build and Run

1. Connect an Android device via USB (with USB debugging enabled)
2. Or set up an Android emulator in Android Studio
3. Click the "Run" button (green play icon)
4. Select your device/emulator
5. App will build and install automatically

### Alternative: Command Line Build

```bash
# From project root
./gradlew assembleDebug
./gradlew installDebug
```

## Technical Details

### Permissions Required
- `BATTERY_STATS` - Access detailed battery information
- `WAKE_LOCK` - Keep widget updates running

### API Usage
- `BatteryManager.BATTERY_PROPERTY_CURRENT_NOW` - Current measurement
- `BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER` - Charge capacity
- `Intent.ACTION_BATTERY_CHANGED` - Battery status broadcasts

### Update Mechanisms
- Main app: Handler with 1-second postDelayed runnable
- Widget: Handler with 2-second postDelayed runnable
- Widget lifecycle: Updates on enable/disable/update events

## Limitations & Notes

### Environment Limitations
- This is a native Android app that **cannot run in Replit**
- No web preview or console output available
- Must be built and tested in Android Studio or on a real Android device

### Device Limitations
- Current measurements require Android 5.0+ (API 21)
- Some devices may not expose all battery metrics
- Manufacturer restrictions may limit data availability
- Time estimates need usage data to become accurate

## Future Enhancement Ideas

If you want to extend this app:
- Add historical data tracking with charts (MPAndroidChart library)
- Implement battery health degradation analysis
- Create multiple widget themes and sizes
- Add customizable battery level alerts
- Show per-app battery usage statistics
- Export battery data to CSV files
- Add battery optimization tips based on usage patterns

## Development Notes

### Code Conventions
- Kotlin coding standards followed
- Material Design 3 components used
- View binding enabled (no findViewById)
- AndroidX libraries used throughout
- Target SDK set to latest stable (API 34)

### Testing Recommendations
- Test on multiple Android versions
- Test on different manufacturers (Samsung, Pixel, etc.)
- Test widget resize functionality
- Test charge/discharge time estimates over time
- Verify widget updates continue after device restart

## Recent Changes

**October 30, 2025**
- Initial project creation
- Implemented all core features
- Created complete Android project structure
- Added comprehensive README and documentation

## User Preferences

None specified - default Android development conventions followed.

## Contact & Support

This project is provided as-is. For Android development questions:
- Official Android Documentation: https://developer.android.com
- Android Studio Guide: https://developer.android.com/studio
- Kotlin Documentation: https://kotlinlang.org/docs

## Export Checklist

Before exporting to Android Studio, verify:
- [x] All source files created
- [x] Build configuration files present
- [x] AndroidManifest.xml configured
- [x] Layout files created
- [x] Widget configuration added
- [x] README with build instructions
- [x] Project documentation complete

**Ready to export and build in Android Studio!**
