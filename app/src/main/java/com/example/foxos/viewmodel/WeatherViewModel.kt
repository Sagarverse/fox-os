package com.example.foxos.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

data class WeatherInfo(
    val temperature: String,
    val condition: String,
    val city: String,
    val humidity: Int = 0,
    val windSpeed: Double = 0.0
)

class WeatherViewModel : ViewModel() {
    private val _weatherInfo = MutableStateFlow(WeatherInfo("--°C", "Loading...", "--"))
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val client = OkHttpClient()
    
    @SuppressLint("MissingPermission")
    fun fetchWeather(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check location permission
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                
                var latitude = 40.7128 // Default: New York
                var longitude = -74.0060
                var cityName = "New York"
                
                if (hasPermission) {
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        
                        // Get city name from coordinates
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                cityName = addresses[0].locality ?: addresses[0].adminArea ?: "Unknown"
                            }
                        } catch (e: Exception) {
                            // Geocoder failed, keep default
                        }
                    }
                }
                
                // Fetch weather from Open-Meteo API (free, no API key required)
                val weather = fetchWeatherFromApi(latitude, longitude, cityName)
                _weatherInfo.value = weather
            } catch (e: Exception) {
                _weatherInfo.value = WeatherInfo("--°C", "Error", "--")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun fetchWeatherFromApi(lat: Double, lon: Double, city: String): WeatherInfo {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "{}")
                    val current = json.getJSONObject("current")
                    
                    val temp = current.getDouble("temperature_2m")
                    val humidity = current.getInt("relative_humidity_2m")
                    val weatherCode = current.getInt("weather_code")
                    val windSpeed = current.getDouble("wind_speed_10m")
                    
                    val condition = getConditionFromCode(weatherCode)
                    
                    WeatherInfo(
                        temperature = "${temp.toInt()}°C",
                        condition = condition,
                        city = city,
                        humidity = humidity,
                        windSpeed = windSpeed
                    )
                } else {
                    WeatherInfo("--°C", "Error", city)
                }
            } catch (e: Exception) {
                WeatherInfo("--°C", "Offline", city)
            }
        }
    }
    
    private fun getConditionFromCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75 -> "Snow"
            77 -> "Snow Grains"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Unknown"
        }
    }
}