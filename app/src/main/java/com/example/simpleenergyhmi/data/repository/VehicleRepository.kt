package com.example.simpleenergyhmi.data.repository

import com.example.simpleenergyhmi.data.models.VehicleData
import com.example.simpleenergyhmi.data.provider.VehicleDataProvider
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicleData(): Flow<VehicleData>
}

class VehicleRepositoryImpl(
    private val vehicleDataProvider: VehicleDataProvider
) : VehicleRepository {

    override fun observeVehicleData(): Flow<VehicleData> {
        return vehicleDataProvider.getVehicleData()
    }
}
