package com.srcreation.smarthelp

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EnvironmentData(
    val aqi: Int,
    val aqiStatus: String,
    val uvIndex: Int,
    val uvStatus: String,
    val healthAdvice: String,
    val aqiColor: Color,
    val uvColor: Color
)

class EnvironmentViewModel(application: Application) : AndroidViewModel(application) {
    private val _envData = MutableStateFlow<EnvironmentData?>(null)
    val envData: StateFlow<EnvironmentData?> = _envData

    init {
        refreshEnvironmentData()
    }

    fun refreshEnvironmentData() {
        viewModelScope.launch {
            // Simulated High-End Intelligence (In a real app, this calls Weather/AQI API)
            delay(1000)
            
            // Example Data for Simulation
            val aqiValue = 145 // Unhealthy for Sensitive Groups
            val uvValue = 7    // High

            val aqiInfo = when {
                aqiValue <= 50 -> Triple("Good", "Air is clean. Ideal for outdoor exercise.", androidx.compose.ui.graphics.Color(0xFF22C55E))
                aqiValue <= 100 -> Triple("Moderate", "Acceptable air quality. No mask needed.", androidx.compose.ui.graphics.Color(0xFFFF9800))
                else -> Triple("Poor", "Dust alert! Wear a mask if going outside.", androidx.compose.ui.graphics.Color(0xFFEF4444))
            }

            val uvInfo = when {
                uvValue <= 2 -> Triple("Low", "Safe for sunlight.", androidx.compose.ui.graphics.Color(0xFF22C55E))
                uvValue <= 5 -> Triple("Moderate", "Use sunglasses.", androidx.compose.ui.graphics.Color(0xFFFF9800))
                else -> Triple("High", "Apply sunscreen & stay in shade.", androidx.compose.ui.graphics.Color(0xFFEF4444))
            }

            _envData.value = EnvironmentData(
                aqi = aqiValue,
                aqiStatus = aqiInfo.first,
                uvIndex = uvValue,
                uvStatus = uvInfo.first,
                healthAdvice = "${aqiInfo.second} ${uvInfo.second}",
                aqiColor = aqiInfo.third,
                uvColor = uvInfo.third
            )
        }
    }
}
