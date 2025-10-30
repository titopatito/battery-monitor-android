# Battery Monitor - Android App

A native Android application that monitors battery metrics in real-time, including voltage, current, power consumption, and estimated charge/discharge times. Features a resizable home screen widget for quick access to battery information.

## Features

- **Real-Time Battery Monitoring**
  - Voltage (in Volts)
  - Current (in milliamps)
  - Power consumption (in Watts)
  - Battery level percentage
  - Temperature
  - Battery health status
  - Charging status

- **Time Estimates**
  - Estimated time to full charge (when charging)
  - Estimated time to full discharge (when on battery)
  - Adaptive algorithms that learn from usage patterns

- **Resizable Home Screen Widget**
  - Shows voltage, current, power, and battery level
  - Displays charge/discharge time estimates
  - Updates every 2 seconds
  - Customizable size (horizontal and vertical resize)
  - Tap widget to open main app

## Requirements

- Android device running Android 5.0 (API 21) or higher
- Android Studio Hedgehog (2023.1.1) or later
- Gradle 8.2 or later
- Kotlin 1.9.20 or later

## Building the App

### Option 1: Using Android Studio (Recommended)

1. **Download or Clone this project**
   - Download the project as a ZIP file and extract it
   - Or clone using git: `git clone <repository-url>`

2. **Open in Android Studio**
   - Launch Android Studio
   - Click "Open" and select the project folder
   - Wait for Gradle sync to complete

3. **Connect your Android device**
   - Enable USB debugging on your Android device:
     - Go to Settings → About Phone
     - Tap "Build Number" 7 times to enable Developer Options
     - Go to Settings → Developer Options
     - Enable "USB Debugging"
   - Connect your device via USB
   - Accept the debugging authorization prompt on your device

4. **Build and Run**
   - Click the "Run" button (green play icon) in Android Studio
   - Select your connected device
   - The app will build and install on your device

### Option 2: Building APK from Command Line

```bash
# Navigate to project directory
cd BatteryMonitor

# Build debug APK
./gradlew assembleDebug

# The APK will be located at:
# app/build/outputs/apk/debug/app-debug.apk

# Install to connected device
./gradlew installDebug
```

### Option 3: Building Release APK

```bash
# Build release APK
./gradlew assembleRelease

# Sign the APK (you'll need to create a keystore first)
# Follow Android's documentation for signing APKs
```

## Using the App

### Main App

1. Launch "Battery Monitor" from your app drawer
2. View real-time battery metrics updated every second
3. Monitor voltage, current, and power consumption
4. Check estimated time to full charge or discharge
5. View battery health and technology information

### Adding the Widget

1. Long-press on your home screen
2. Select "Widgets" from the menu
3. Find "Battery Monitor" widget
4. Drag it to your home screen
5. Resize the widget to your preferred size
6. The widget will start showing real-time battery data
7. Tap the widget to open the main app

## How It Works

### Battery Data Collection

The app uses Android's `BatteryManager` API to access battery information:
- `BATTERY_PROPERTY_CURRENT_NOW` - Current flowing (in/out of battery)
- `EXTRA_VOLTAGE` - Battery voltage
- Temperature, health, and status from battery broadcast
- Power calculated as: Power = Voltage × Current

### Time Estimation Algorithm

The app uses an adaptive learning algorithm:
1. Monitors battery level changes over time
2. Calculates charge/discharge rates
3. Maintains a rolling average of the last 10 rate measurements
4. Estimates time based on current battery level and average rate
5. Adapts to your usage patterns for more accurate predictions

### Widget Updates

The widget updates every 2 seconds using:
- `AppWidgetProvider` for widget lifecycle
- Periodic broadcast updates via Handler
- Click action to launch main app

## Technical Details

- **Language**: Kotlin
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: MVVM pattern with view binding
- **UI Framework**: Material Design Components
- **Update Frequency**: 1 second (main app), 2 seconds (widget)

## Permissions

The app requires these permissions:
- `BATTERY_STATS` - To access detailed battery information
- `WAKE_LOCK` - To keep widget updates running

These are normal permissions granted automatically by the system.

## Limitations

- Current measurements may not be available on all devices (requires API 21+)
- Some manufacturers may restrict access to certain battery metrics
- Time estimates become more accurate after the app learns your usage patterns
- Widget updates may be throttled by the system to save battery

## Troubleshooting

### App doesn't show current/power data
- Some devices don't expose current measurements through the API
- Try updating your device's firmware
- Check if your device manufacturer provides this data

### Widget not updating
- Check that battery optimization is disabled for the app
- Restart your device
- Remove and re-add the widget

### Time estimates show "N/A"
- Wait a few minutes for the app to collect data
- Charge/discharge your device normally to help the algorithm learn

## Future Enhancements

Possible improvements for future versions:
- Historical battery data with charts
- Battery health analysis and degradation tracking
- Export battery statistics to CSV
- Dark/light widget themes
- Battery optimization recommendations
- Per-app battery usage statistics
- Customizable alerts for battery levels

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Verify your device meets the minimum requirements
3. Ensure USB debugging is enabled when testing

## License

This project is provided as-is for educational and personal use.
