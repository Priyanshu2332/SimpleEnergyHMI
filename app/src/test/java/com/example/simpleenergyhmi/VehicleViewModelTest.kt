package com.example.simpleenergyhmi

import com.example.simpleenergyhmi.data.models.KillSwitchStatus
import com.example.simpleenergyhmi.data.models.MotorStatus
import com.example.simpleenergyhmi.data.models.VehicleData
import com.example.simpleenergyhmi.data.repository.VehicleRepository
import com.example.simpleenergyhmi.data.viewmodel.VehicleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Fake Repository for testing success
    class FakeVehicleRepository(
        private val vehicleData: VehicleData
    ) : VehicleRepository {
        override fun observeVehicleData(): Flow<VehicleData> {
            return flowOf(vehicleData)
        }
    }

    // Fake Repository for testing error flow
    class FakeErrorVehicleRepository(
        private val errorMessage: String
    ) : VehicleRepository {
        override fun observeVehicleData(): Flow<VehicleData> {
            return flow {
                throw RuntimeException(errorMessage)
            }
        }
    }

    @Test
    fun `vehicle data received successfully updates ui state`() = runTest(testDispatcher) {
        val expectedData = VehicleData(
            speedKmh = 50f,
            motorStatus = MotorStatus.ON,
            killSwitchStatus = KillSwitchStatus.INACTIVE
        )

        val repository = FakeVehicleRepository(expectedData)
        val viewModel = VehicleViewModel(repository)

        // Run current pending tasks without advanceUntilIdle due to infinite monitoring loop
        runCurrent()

        assertEquals(expectedData, viewModel.uiState.value.vehicleData)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `negative speed is converted to null`() = runTest(testDispatcher) {
        val invalidData = VehicleData(
            speedKmh = -10f,
            motorStatus = MotorStatus.ON,
            killSwitchStatus = KillSwitchStatus.INACTIVE
        )

        val repository = FakeVehicleRepository(invalidData)
        val viewModel = VehicleViewModel(repository)

        runCurrent()

        assertNull(viewModel.uiState.value.vehicleData?.speedKmh)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `nan and infinite speed are converted to null`() = runTest(testDispatcher) {
        val nanData = VehicleData(
            speedKmh = Float.NaN,
            motorStatus = MotorStatus.ON,
            killSwitchStatus = KillSwitchStatus.INACTIVE
        )

        val repositoryNan = FakeVehicleRepository(nanData)
        val viewModelNan = VehicleViewModel(repositoryNan)
        runCurrent()
        assertNull(viewModelNan.uiState.value.vehicleData?.speedKmh)

        val infData = VehicleData(
            speedKmh = Float.POSITIVE_INFINITY,
            motorStatus = MotorStatus.ON,
            killSwitchStatus = KillSwitchStatus.INACTIVE
        )

        val repositoryInf = FakeVehicleRepository(infData)
        val viewModelInf = VehicleViewModel(repositoryInf)
        runCurrent()
        assertNull(viewModelInf.uiState.value.vehicleData?.speedKmh)
    }

    @Test
    fun `missing speed remains null`() = runTest(testDispatcher) {
        val vehicleData = VehicleData(
            speedKmh = null,
            motorStatus = MotorStatus.ON,
            killSwitchStatus = KillSwitchStatus.INACTIVE
        )

        val repository = FakeVehicleRepository(vehicleData)
        val viewModel = VehicleViewModel(repository)

        runCurrent()

        assertNull(viewModel.uiState.value.vehicleData?.speedKmh)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `vehicle data becomes stale after five seconds`() = runTest(testDispatcher) {
        var fakeTime = 10000L

        val vehicleData = VehicleData(
            speedKmh = 25f,
            motorStatus = MotorStatus.ON,
            killSwitchStatus = KillSwitchStatus.INACTIVE,
            timestamp = fakeTime
        )

        val repository = FakeVehicleRepository(vehicleData)
        val viewModel = VehicleViewModel(
            repository = repository,
            currentTimeMillis = { fakeTime }
        )

        // Receive initial vehicle data
        runCurrent()
        assertFalse(viewModel.uiState.value.isStale)

        // Move clock forward by 6 seconds
        fakeTime += 6000L

        // Advance monitoring coroutine
        advanceTimeBy(6000L)
        runCurrent()

        assertTrue(viewModel.uiState.value.isStale)
    }

    @Test
    fun `flow error updates error message in ui state`() = runTest(testDispatcher) {
        val errorMessage = "Sensor communication failure"
        val repository = FakeErrorVehicleRepository(errorMessage)
        val viewModel = VehicleViewModel(repository)

        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(errorMessage, viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.vehicleData)
    }
}
