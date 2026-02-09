package com.example.foxos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WeatherInfo(
    val temperature: String,
    val condition: String,
    val city: String
)

class WeatherViewModel : ViewModel() {
    private val _weatherInfo = MutableStateFlow(WeatherInfo("24°C", "Partly Cloudy", "New York"))
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()
}