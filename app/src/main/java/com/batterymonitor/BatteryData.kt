package com.batterymonitor

data class BatteryData(
    val voltage: Double = 0.0,
    val current: Double = 0.0,
    val power: Double = 0.0,
    val level: Int = 0,
    val capacity: Int = 0,
    val temperature: Double = 0.0,
    val isCharging: Boolean = false,
    val status: String = "Unknown",
    val health: String = "Unknown",
    val technology: String = "Unknown",
    val timeToFullCharge: Long = -1,
    val timeToFullDischarge: Long = -1
) {
    fun getVoltageString(): String = String.format("%.3f V", voltage / 1000.0)
    
    fun getCurrentString(): String = String.format("%.0f mA", current / 1000.0)
    
    fun getPowerString(): String = String.format("%.2f W", power)
    
    fun getTemperatureString(): String = String.format("%.1f °C", temperature / 10.0)
    
    fun getLevelString(): String = "$level%"
    
    fun getTimeToFullChargeString(): String {
        return if (timeToFullCharge > 0) {
            formatTime(timeToFullCharge)
        } else {
            "N/A"
        }
    }
    
    fun getTimeToFullDischargeString(): String {
        return if (timeToFullDischarge > 0) {
            formatTime(timeToFullDischarge)
        } else {
            "N/A"
        }
    }
    
    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return if (hours > 0) {
            String.format("%dh %dm", hours, minutes)
        } else {
            String.format("%dm", minutes)
        }
    }
}
