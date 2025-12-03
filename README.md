## MyNoiseDetector

MyNoiseDetector is a simple Android app that measures environmental noise using the device microphone and alerts you when the sound level gets too high.

### Features
- **Live noise meter**: Displays the current sound level in dBFS with a progress bar.
- **Color feedback**: A colored circle and label show whether the noise is **Low**, **Medium**, or **High**.
- **Configurable alert threshold**: Adjust the noise alert threshold using a seek bar.
- **Vibration alert**: The device vibrates and shows a message when the noise exceeds your chosen threshold.

### Permissions
- **RECORD_AUDIO**: Required to access the microphone and measure sound levels.
- **VIBRATE**: Used to provide haptic feedback when noise exceeds the threshold.

### Getting Started
- **Requirements**:
  - Android Studio (latest stable version recommended)
  - Android device or emulator with microphone support

- **Setup**:
  1. Clone or download this project.
  2. Open the project in Android Studio.
  3. Let Gradle sync and finish building.
  4. Connect an Android device (or start an emulator).
  5. Click **Run** to install and launch the app.

### How to Use
1. Open the app; you’ll see the noise meter, color indicator, and controls.
2. Tap **Start** to begin measuring noise. Grant microphone permission when prompted.
3. Adjust the **Alert threshold** slider to choose when alerts should trigger.
4. When the noise exceeds the threshold, the app will:
   - Show a warning message
   - Vibrate the device
5. Tap **Stop** to stop measuring and release the microphone.

### Notes
- This app is intended for **relative** noise indication (quiet vs. loud) rather than for calibrated, professional sound level measurements.
- On some devices, microphone sensitivity and reported levels may vary.



