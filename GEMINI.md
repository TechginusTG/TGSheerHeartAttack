# Project: TGSheerHeartAttack

## Directory Overview

This directory contains an Android application and an Arduino project that work together to create a functional "Sheer Heart Attack" replica from the anime/manga "JoJo's Bizarre Adventure".

The Android application acts as a remote controller, sending commands via Bluetooth to the Arduino device, which controls the physical hardware.

## Key Files

*   `App/`: The Android application (the remote controller).
    *   `App/app/src/main/java/com/github/TechGinus/TGSheerHeartAttack/MainActivity.kt`: The main activity of the Android app. It handles requesting Bluetooth permissions, displays the connection status, and hosts the controller buttons.
    *   `App/app/src/main/java/com/github/TechGinus/TGSheerHeartAttack/SerialBluetooth.kt`: Manages the Bluetooth connection to the Arduino device. It sends single-character commands (`F`, `B`, `L`, `R`, `S`) corresponding to user actions on the controller buttons (forward, backward, left, right, stop).
    *   `App/app/src/main/res/layout/activity_main.xml`: The layout file defining the user interface, including the control buttons and status text.
    *   `App/app/src/main/res/raw/kocchi_wo_miro.mp3`: The sound file played by the app when the central skull button is pressed.

*   `Arduino/SheerHeartAttack/SheerHeartAttack.ino`: The Arduino sketch for the ESP32. It receives commands from the Android app to control motors for movement.
    *   **Manual Mode:** Responds to commands from the app (`F`, `B`, `L`, `R`, `S`).
    *   **Automatic Mode:** A planned feature where it will use a thermal sensor to seek out heat sources. The basic structure for this mode is in place.
    *   It defines placeholder pins for a motor driver, a servo, a thermal sensor, and a sound module.

## Building and Running

### Android App

1.  Open the `App/` directory in Android Studio.
2.  **Crucially, you must edit `App/app/src/main/java/com/github/TechGinus/TGSheerHeartAttack/SerialBluetooth.kt` and replace the placeholder `ESP32_MAC_ADDRESS` with the actual MAC address of your ESP32 board.**
3.  Build and run the app on an emulator or a physical Android device.

### Arduino

1.  Open `Arduino/SheerHeartAttack/SheerHeartAttack.ino` in the Arduino IDE.
2.  **Crucially, you must update the placeholder pin numbers (`#define`) at the top of the file to match the actual wiring of your components (motor driver, servo, etc.).**
3.  Select your ESP32 board and port.
4.  Upload the sketch.

## Development Conventions

The project is in a functional state but still under development. The Android app is written in Kotlin. The Arduino sketch is C++/Arduino language.