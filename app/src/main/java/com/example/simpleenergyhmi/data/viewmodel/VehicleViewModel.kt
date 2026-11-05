package com.example.simpleenergyhmi.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleenergyhmi.data.models.VehicleData
import com.example.simpleenergyhmi.data.repository.VehicleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class VehicleViewModel(
    private val repository: VehicleRepository,
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    data class VehicleUiState(
        val vehicleData: VehicleData? = null,
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
        val isStale: Boolean = false
    )

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    init {
        observeVehicleData()
        monitorStaleData()
    }

    private fun observeVehicleData() {
        viewModelScope.launch {
            repository.observeVehicleData()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                            ?: "Unable to receive vehicle data"
                    )
                }
                .collect { data ->
                    // Validate vehicle speed: reject negative, NaN, and infinite values
                    val validSpeed = data.speedKmh?.takeIf {
                        it.isFinite() && it >= 0f
                    }

                    val safeData = data.copy(
                        speedKmh = validSpeed
                    )

                    _uiState.value = VehicleUiState(
                        vehicleData = safeData,
                        isLoading = false,
                        errorMessage = null,
                        isStale = currentTimeMillis() - safeData.timestamp > 5000L
                    )
                }
        }
    }

    private fun monitorStaleData() {
        viewModelScope.launch {
            while (true) {
                delay(1000.milliseconds)

                val currentState = _uiState.value
                val vehicleData = currentState.vehicleData

                if (vehicleData != null) {
                    val isStale = currentTimeMillis() - vehicleData.timestamp > 5000L
                    if (currentState.isStale != isStale) {
                        _uiState.value = currentState.copy(
                            isStale = isStale
                        )
                    }
                }
            }
        }
    }
}