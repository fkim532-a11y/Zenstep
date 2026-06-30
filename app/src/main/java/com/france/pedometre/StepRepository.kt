package com.france.pedometre

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StepRepository {
    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps.asStateFlow()

    private val _sensorAvailable = MutableStateFlow(true)
    val sensorAvailable: StateFlow<Boolean> = _sensorAvailable.asStateFlow()

    fun updateSteps(steps: Int) {
        _currentSteps.value = steps
    }

    fun setSensorAvailable(available: Boolean) {
        _sensorAvailable.value = available
    }
}