package com.example.simpleenergyhmi

import android.app.Application
import com.example.simpleenergyhmi.di.vehicleModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(vehicleModule)
        }
    }
}
