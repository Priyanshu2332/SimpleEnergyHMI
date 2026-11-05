# Electric Vehicle HMI Dashboard

## Overview

This project is an Android-based Electric Vehicle (EV) Human-Machine Interface (HMI) dashboard developed using Kotlin and Jetpack Compose.

It simulates vehicle information such as speed, motor status, and kill-switch status. The dashboard automatically updates vehicle states approximately every second to demonstrate a real-time vehicle monitoring experience.

## Features

- Displays vehicle speed in km/h.
- Shows motor ON/OFF status.
- Shows kill-switch ACTIVE/INACTIVE status.
- Displays the current ride mode.
- Simulates five vehicle scenarios:

| Scenario | Speed | Motor | Kill Switch |
|---|---:|---|---|
| Parked | 0 km/h | OFF | INACTIVE |
| Ready | 0 km/h | ON | INACTIVE |
| Riding | 25 km/h | ON | INACTIVE |
| Accelerating | 50 km/h | ON | INACTIVE |
| Motor Disabled | 0 km/h | OFF | ACTIVE |

- Automatically updates vehicle states approximately every second.
- Handles invalid and missing speed values.
- Detects stale vehicle data.
- Handles data stream errors.
- Supports landscape orientation.

## Tech Stack

- Kotlin
- Jetpack Compose
- Android SDK
- Kotlin Coroutines
- Kotlin Flow and StateFlow
- MVVM Architecture
- Koin Dependency Injection
- JUnit
- Kotlin Coroutines Test

## Architecture

The project follows the MVVM architecture with separation between the data layer, ViewModel, and UI.

### Data Layer

- **VehicleData:** Represents vehicle information, including speed, motor status, kill-switch status, and timestamp.
- **VehicleDataProvider:** Generates simulated vehicle data using Kotlin Flow.
- **VehicleRepository:** Provides an abstraction for accessing vehicle data.

### ViewModel Layer

- **VehicleViewModel:** Collects vehicle data from the repository.
- Exposes UI state through StateFlow.
- Validates incoming speed values.
- Handles loading and error states.
- Monitors stale vehicle data.

### UI Layer

- **HmiDashboardScreen:** Displays vehicle information using Jetpack Compose.
- Observes ViewModel state and automatically updates when new data arrives.

### Dependency Injection

Koin is used to provide dependencies such as the repository and ViewModel.

### Data Flow

VehicleDataProvider → VehicleRepository → VehicleViewModel → StateFlow → Compose UI

## Project Structure

```text
com.example.simpleenergyhmi
│
├── data
│   ├── models
│   │   └── VehicleData.kt
│   │
│   ├── provider
│   │   └── VehicleDataProvider.kt
│   │
│   ├── repository
│   │   └── VehicleRepository.kt
│   │
│   └── viewmodel
│       └── VehicleViewModel.kt
│
├── di
│   └── AppModule.kt
│
├── compose
│   └── HmiDashboardScreen.kt
│
├── ui
│   └── theme
│
├── MainActivity.kt
└── MyApplication.kt
```

## Edge Cases Handled

1. **Negative speed:** Invalid negative speed values are converted to null.
2. **NaN and infinite speed:** Non-finite speed values are rejected.
3. **Missing speed:** Null speed values are handled safely and displayed as unavailable.
4. **Stale data:** Vehicle data is marked stale if no fresh update is received for more than 5 seconds.
5. **Repository/Flow failure:** Exceptions from the data stream are caught and exposed through the UI state.

## Unit Tests

Unit tests are implemented using JUnit and `kotlinx-coroutines-test`.

The six test cases cover:

- Successful vehicle data reception and UI state update.
- Negative speed validation.
- NaN and infinite speed validation.
- Missing speed handling.
- Stale vehicle data detection.
- Flow error handling.

### Run Unit Tests

On Windows, open the project terminal and execute:

```powershell
.\gradlew.bat testDebugUnitTest
```

Test reports are generated at:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

Open the HTML report to view individual test results.

## Build and Run

1. Clone or download the repository.
2. Open the project in Android Studio.
3. Allow Gradle synchronization to finish.
4. Connect an Android device or start an emulator.
5. Run the application using the Android Studio Run button.

The application is configured for landscape orientation.

## Assumptions and Limitations

- Vehicle data is simulated locally; no physical vehicle or CAN hardware is connected.
- The five predefined vehicle scenarios change automatically approximately every second.
- Stale-data detection uses a 5-second threshold.
- Speed validation rejects negative and non-finite values.
- This project demonstrates the Android HMI application layer and does not communicate with actual vehicle hardware.

## Author

Android Developer Assignment – Simple Energy
