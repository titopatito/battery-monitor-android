package com.batterymonitor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import kotlin.math.abs

class BatteryMonitor(private val context: Context) {
    
    private val batteryManager: BatteryManager? = 
        context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    
    private var lastLevel: Int = -1
    private var lastTimestamp: Long = 0
    private var averageDischargeRate: Double = 0.0
    private var averageChargeRate: Double = 0.0
    private val rateHistory = mutableListOf<Double>()
    private val maxHistorySize = 10
    
    fun getBatteryData(): BatteryData {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        
        if (batteryStatus == null) {
            return BatteryData()
        }
        
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level * 100 / scale.toFloat()
        
        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
        
        val voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        
        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
        } else {
            0
        }
        
        val power = (voltage.toDouble() / 1000.0) * (abs(current.toDouble()) / 1000000.0)
        
        val capacity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: 0
        } else {
            0
        }
        
        val temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        
        val health = when (batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
        
        val statusString = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
        
        val technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        
        updateRates(level, isCharging)
        
        val timeToFullCharge = if (isCharging && averageChargeRate > 0) {
            ((100 - batteryPct) / averageChargeRate * 3600000).toLong()
        } else {
            -1L
        }
        
        val timeToFullDischarge = if (!isCharging && averageDischargeRate > 0) {
            (batteryPct / averageDischargeRate * 3600000).toLong()
        } else {
            -1L
        }
        
        return BatteryData(
            voltage = voltage.toDouble(),
            current = current.toDouble(),
            power = power,
            level = batteryPct.toInt(),
            capacity = capacity,
            temperature = temperature.toDouble(),
            isCharging = isCharging,
            status = statusString,
            health = health,
            technology = technology,
            timeToFullCharge = timeToFullCharge,
            timeToFullDischarge = timeToFullDischarge
        )
    }
    
    private fun updateRates(currentLevel: Int, isCharging: Boolean) {
        val currentTime = System.currentTimeMillis()
        
        if (lastLevel != -1 && lastTimestamp != 0L) {
            val timeDiff = (currentTime - lastTimestamp) / 1000.0 / 3600.0
            
            if (timeDiff > 0) {
                val levelDiff = currentLevel - lastLevel
                val rate = abs(levelDiff / timeDiff)
                
                if (rate > 0 && rate < 100) {
                    rateHistory.add(rate)
                    if (rateHistory.size > maxHistorySize) {
                        rateHistory.removeAt(0)
                    }
                    
                    val averageRate = rateHistory.average()
                    if (isCharging) {
                        averageChargeRate = averageRate
                    } else {
                        averageDischargeRate = averageRate
                    }
                }
            }
        }
        
        lastLevel = currentLevel
        lastTimestamp = currentTime
    }
}
