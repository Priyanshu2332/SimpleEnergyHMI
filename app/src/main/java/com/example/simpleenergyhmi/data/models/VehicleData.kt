package com.example.simpleenergyhmi.data.models

import kotlinx.serialization.Serializable

@Serializable
data class VehicleData(
    val speedKmh: Float?,
    val motorStatus: MotorStatus,
    val killSwitchStatus: KillSwitchStatus,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class MotorStatus {
    ON,
    OFF,
    UNKNOWN
}

@Serializable
enum class KillSwitchStatus {
    ACTIVE,
    INACTIVE,
    UNKNOWN
}
