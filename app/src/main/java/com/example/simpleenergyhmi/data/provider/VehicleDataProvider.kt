package com.example.simpleenergyhmi.data.provider

import com.example.simpleenergyhmi.data.models.KillSwitchStatus
import com.example.simpleenergyhmi.data.models.MotorStatus
import com.example.simpleenergyhmi.data.models.VehicleData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

class VehicleDataProvider {

    fun getVehicleData(): Flow<VehicleData> = flow {

        val scenarios = listOf(
            // 1. Parked
            VehicleData(0f, MotorStatus.OFF, KillSwitchStatus.INACTIVE),

            // 2. Ready
            VehicleData(0f, MotorStatus.ON, KillSwitchStatus.INACTIVE),

            // 3. Riding
            VehicleData(25f, MotorStatus.ON, KillSwitchStatus.INACTIVE),

            // 4. Accelerating
            VehicleData(50f, MotorStatus.ON, KillSwitchStatus.INACTIVE),

            // 5. Motor Disabled
            VehicleData(0f, MotorStatus.OFF, KillSwitchStatus.ACTIVE)
        )

        var index = 0

        while (true) {
            val vehicleData = scenarios[index].copy(
                timestamp = System.currentTimeMillis()
            )

            emit(vehicleData)

            delay(1000.milliseconds)    // wait for scenarios(1 second)

            index = (index + 1) % scenarios.size
        }
    }
}
