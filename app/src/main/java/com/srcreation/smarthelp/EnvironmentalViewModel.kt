package com.srcreation.smarthelp

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.location.Location
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

data class EnvironmentalData(
    val aqi: Int = 0,
    val aqiText: String = "Loading...",
    val aqiAdvice: String = "Fetching data...",
    val aqiColor: Color = Color.Gray,
    val locationName: String = "Current Location"
)

class EnvironmentalViewModel(application: Application) : AndroidViewModel(application) {
    private val service = EnvironmentalHealthService.create()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    
    // Placeholder API Key (User should replace this)
    private val apiKey = "YOUR_OPENWEATHER_API_KEY"

    private val _envData = MutableStateFlow(EnvironmentalData())
    val envData: StateFlow<EnvironmentalData> = _envData

    fun fetchEnvironmentalData(context: Context) {
        viewModelScope.launch {
            try {
                val availability = GoogleApiAvailability.getInstance()
                val resultCode = availability.isGooglePlayServicesAvailable(context)
                if (resultCode != ConnectionResult.SUCCESS) {
                    _envData.value = _envData.value.copy(
                        aqiText = "Unavailable",
                        aqiAdvice = "Google Play Services not available."
                    )
                    return@launch
                }

                // Get current location (needs permission handling in UI)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            updateAirQuality(location.latitude, location.longitude)
                        } else {
                            _envData.value = _envData.value.copy(
                                aqiText = "Location Not Found",
                                aqiAdvice = "Please ensure GPS is enabled."
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        _envData.value = _envData.value.copy(
                            aqiText = "Location Error",
                            aqiAdvice = e.localizedMessage ?: "Failed to get location."
                        )
                    }
            } catch (e: SecurityException) {
                _envData.value = _envData.value.copy(aqiText = "No Permission", aqiAdvice = "Enable location in settings.")
            } catch (e: Exception) {
                _envData.value = _envData.value.copy(aqiText = "Error", aqiAdvice = "Could not fetch location.")
            }
        }
    }

    private fun updateAirQuality(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = service.getAirQuality(lat, lon, apiKey)
                val aqi = response.list.firstOrNull()?.main?.aqi ?: 0
                val (text, advice, color) = getAqiInfo(aqi)
                _envData.value = EnvironmentalData(aqi, text, advice, color)
            } catch (e: Exception) {
                _envData.value = _envData.value.copy(aqiText = "Error", aqiAdvice = "Check internet or API key.")
            }
        }
    }

    private fun getAqiInfo(aqi: Int): Triple<String, String, Color> {
        return when (aqi) {
            1 -> Triple("Good", "Air quality is great. Perfect for outdoor activities!", Color(0xFF22C55E))
            2 -> Triple("Fair", "Acceptable air quality. Enjoy your day.", Color(0xFF84CC16))
            3 -> Triple("Moderate", "Sensitive groups should limit prolonged outdoor exertion.", Color(0xFFFFAB00))
            4 -> Triple("Poor", "Avoid outdoor activities. Wear a mask if going out.", Color(0xFFFF5252))
            5 -> Triple("Very Poor", "Hazardous! Stay indoors and use air purifiers.", Color(0xFF7E22CE))
            else -> Triple("Unknown", "Keep tracking for updates.", Color.Gray)
        }
    }
}
