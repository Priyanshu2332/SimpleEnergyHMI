
package com.example.simpleenergyhmi

import androidx.lifecycle.ViewModelStore
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    private class FakeVehicleRepository(
        private val vehicleData: VehicleData
    ) : VehicleRepository {

        override fun observeVehicleData(): Flow<VehicleData> {
            return flowOf(vehicleData)
        }
    }

    private class FakeErrorVehicleRepository(
        private val errorMessage: String
    ) : VehicleRepository {

        override fun observeVehicleData(): Flow<VehicleData> {
            return flow {
                throw RuntimeException(errorMessage)
            }
        }
    }

    private fun createViewModelStore(
        viewModel: VehicleViewModel
    ): ViewModelStore {
        return ViewModelStore().apply {
            put("VehicleViewModel", viewModel)
        }
    }

    @Test
    fun `vehicle data received successfully updates ui state`() =
        runTest(testDispatcher) {

            val expectedData = VehicleData(
                speedKmh = 50f,
                motorStatus = MotorStatus.ON,
                killSwitchStatus = KillSwitchStatus.INACTIVE
            )

            val repository = FakeVehicleRepository(expectedData)
            val viewModel = VehicleViewModel(repository)
            val viewModelStore = createViewModelStore(viewModel)

            try {
                runCurrent()

                assertEquals(
                    expectedData,
                    viewModel.uiState.value.vehicleData
                )
                assertFalse(viewModel.uiState.value.isLoading)
                assertNull(viewModel.uiState.value.errorMessage)
            } finally {
                viewModelStore.clear()
            }
        }

    @Test
    fun `negative speed is converted to null`() =
        runTest(testDispatcher) {

            val invalidData = VehicleData(
                speedKmh = -10f,
                motorStatus = MotorStatus.ON,
                killSwitchStatus = KillSwitchStatus.INACTIVE
            )

            val repository = FakeVehicleRepository(invalidData)
            val viewModel = VehicleViewModel(repository)
            val viewModelStore = createViewModelStore(viewModel)

            try {
                runCurrent()

                assertNull(
                    viewModel.uiState.value.vehicleData?.speedKmh
                )
                assertFalse(viewModel.uiState.value.isLoading)
            } finally {
                viewModelStore.clear()
            }
        }

    @Test
    fun `nan and infinite speed are converted to null`() =
        runTest(testDispatcher) {

            val nanData = VehicleData(
                speedKmh = Float.NaN,
                motorStatus = MotorStatus.ON,
                killSwitchStatus = KillSwitchStatus.INACTIVE
            )

            val nanViewModel = VehicleViewModel(
                FakeVehicleRepository(nanData)
            )
            val nanStore = createViewModelStore(nanViewModel)

            val infiniteData = VehicleData(
                speedKmh = Float.POSITIVE_INFINITY,
                motorStatus = MotorStatus.ON,
                killSwitchStatus = KillSwitchStatus.INACTIVE
            )

            val infiniteViewModel = VehicleViewModel(
                FakeVehicleRepository(infiniteData)
            )
            val infiniteStore = createViewModelStore(infiniteViewModel)

            try {
                runCurrent()

                assertNull(
                    nanViewModel.uiState.value.vehicleData?.speedKmh
                )

                assertNull(
                    infiniteViewModel.uiState.value.vehicleData?.speedKmh
                )
            } finally {
                nanStore.clear()
                infiniteStore.clear()
            }
        }

    @Test
    fun `missing speed remains null`() =
        runTest(testDispatcher) {

            val vehicleData = VehicleData(
                speedKmh = null,
                motorStatus = MotorStatus.ON,
                killSwitchStatus = KillSwitchStatus.INACTIVE
            )

            val repository = FakeVehicleRepository(vehicleData)
            val viewModel = VehicleViewModel(repository)
            val viewModelStore = createViewModelStore(viewModel)

            try {
                runCurrent()

                assertNull(
                    viewModel.uiState.value.vehicleData?.speedKmh
                )
                assertFalse(viewModel.uiState.value.isLoading)
                assertNull(viewModel.uiState.value.errorMessage)
            } finally {
                viewModelStore.clear()
            }
        }

    @Test
    fun `vehicle data becomes stale after five seconds`() =
        runTest(testDispatcher) {

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

            val viewModelStore = createViewModelStore(viewModel)

            try {
                runCurrent()

                assertFalse(viewModel.uiState.value.isStale)

                fakeTime += 6000L

                advanceTimeBy(6000L)
                runCurrent()

                assertTrue(viewModel.uiState.value.isStale)
            } finally {
                viewModelStore.clear()
            }
        }

    @Test
    fun `flow error updates error message in ui state`() =
        runTest(testDispatcher) {

            val errorMessage = "Sensor communication failure"

            val repository = FakeErrorVehicleRepository(errorMessage)
            val viewModel = VehicleViewModel(repository)
            val viewModelStore = createViewModelStore(viewModel)

            try {
                runCurrent()

                assertFalse(viewModel.uiState.value.isLoading)
                assertEquals(
                    errorMessage,
                    viewModel.uiState.value.errorMessage
                )
                assertNull(viewModel.uiState.value.vehicleData)
            } finally {
                viewModelStore.clear()
            }
        }
}
