package com.example.simpleenergyhmi.di

import com.example.simpleenergyhmi.data.repository.VehicleRepository
import com.example.simpleenergyhmi.data.repository.VehicleRepositoryImpl
import com.example.simpleenergyhmi.data.provider.VehicleDataProvider
import com.example.simpleenergyhmi.data.viewmodel.VehicleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vehicleModule = module {

    // VehicleDataProvider
    single {
        VehicleDataProvider()
    }

    // VehicleRepository
    single<VehicleRepository> {
        VehicleRepositoryImpl(get())
    }

    // VehicleViewModel
    viewModel {
        VehicleViewModel(get())
    }
}
