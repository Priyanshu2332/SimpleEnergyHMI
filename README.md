# CryptoTracker – Electric Vehicle HMI Dashboard

## Overview

This project is an Android-based Electric Vehicle (EV) HMI dashboard built using Kotlin and Jetpack Compose.

It simulates vehicle information such as speed, motor status, and kill-switch status. The dashboard automatically updates vehicle states every second to demonstrate a real-time vehicle monitoring experience.

## Features

- Displays vehicle speed in km/h.
- Shows motor ON/OFF status.
- Shows kill-switch ACTIVE/INACTIVE status.
- Simulates five vehicle states:
    1. Parked – Speed: 0 km/h, Motor: OFF, Kill Switch: INACTIVE.
    2. Ready – Speed: 0 km/h, Motor: ON, Kill Switch: INACTIVE.
    3. Riding – Speed: 25 km/h, Motor: ON, Kill Switch: INACTIVE.
    4. Accelerating – Speed: 50 km/h, Motor: ON, Kill Switch: INACTIVE.
    5. Motor Disabled – Speed: 0 km/h, Motor: OFF, Kill Switch: ACTIVE.
- Automatically changes states every second.
- Handles invalid and missing speed values.
- Detects stale vehicle data.
- Handles data provider errors.
- Supports landscape orientation.

## Tech Stack

- Kotlin
- Jetpack Compose
- Android SDK
- Coroutines
- StateFlow
- MVVM Architecture
- Koin Dependency Injection
- JUnit
- Kotlin Coroutines Test

## Architecture

The project follows MVVM architecture with separation between data, business logic, and UI.

### Data Layer
- VehicleData: Represents vehicle information.
- VehicleDataProvider: Generates simulated vehicle data.
- VehicleRepository: Provides an abstraction for accessing vehicle data.

### ViewModel Layer
- VehicleViewModel: Collects vehicle data and exposes UI state through StateFlow.
- Validates speed values.
- Handles loading and error states.
- Monitors stale vehicle data.

### UI Layer
- HmiDashboardScreen: Displays vehicle information using Jetpack Compose.
- Observes ViewModel state and updates the dashboard.

### Dependency Injection
Koin is used to provide dependencies such as the repository and ViewModel.

## Project Structure

```text
com.example.cryptotracker
│
├── data
│   ├── models
│   │   └── VehicleData.kt
│   ├── network
│   │   └── VehicleRepository.kt
│   ├── viewmodel
│   │   └── VehicleViewModel.kt
│   └── VehicleDataProvider.kt
│
├── di
│   └── AppModule.kt
│
├── compose
│   └── HmiDashboardScreen.kt
│
├── navigation
│   └── Navigation.kt
│
├── MainActivity.kt
└── MyApplication.kt
```

## Edge Cases Handled

1. Negative speed: Invalid negative speed values are converted to null.
2. Missing speed: Null speed values are handled safely.
3. Stale data: Vehicle data is marked stale if no new update is received for more than 5 seconds.
4. Repository failure: Errors from the data provider are handled and exposed through the UI state.

## Unit Tests

Unit tests are written using JUnit and kotlinx-coroutines-test.

The tests cover:

- Successful vehicle data reception.
- Negative speed validation.
- Missing speed handling.
- Repository failure handling.
- Stale vehicle data detection.

### Run Unit Tests

On Windows, open the project terminal and run:

```powershell
.\gradlew.bat testDebugUnitTest
```

Test reports are generated at:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

## Build and Run

1. Clone or download the project.
2. Open the project in Android Studio.
3. Allow Gradle synchronization to finish.
4. Connect an Android device or start an emulator.
5. Run the app using the Run button.

The application is configured to run in landscape orientation.

## Assumptions and Limitations

- Vehicle data is simulated locally; no physical vehicle or CAN hardware is connected.
- Vehicle states change automatically every second.
- Stale-data detection uses a 5-second threshold.
- This project demonstrates the Android HMI application layer and does not communicate with actual vehicle hardware.

## Author

Android Developer Assignment – Simple Energy