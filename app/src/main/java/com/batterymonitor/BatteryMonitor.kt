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
    
    companion object {
        private const val DEFAULT_BATTERY_CAPACITY_MAH = 4000.0
        private const val MINIMUM_UPDATE_INTERVAL_MS = 30000L
    }
    
    private var lastLevel: Int = -1
    private var lastLevelChangeTime: Long = 0
    private var estimatedFullCapacityMah: Double = DEFAULT_BATTERY_CAPACITY_MAH
    private var levelChangeRates = mutableListOf<Double>()
    private val maxRateHistory = 5
    
    // Low pass filter for current
    private var filteredCurrent: Double? = null
    private val filterAlpha: Double = 0.1 // Alpha = dt / (RC + dt). dt=1s, RC=9s -> Alpha approx 0.1
    
    fun getBatteryData(): BatteryData {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        
        if (batteryStatus == null) {
            return BatteryData()
        }
        
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (scale > 0) level * 100 / scale.toFloat() else 0f
        
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
        
        // Update low pass filter for current
        filteredCurrent = if (filteredCurrent == null) {
            current.toDouble()
        } else {
            filteredCurrent!! + filterAlpha * (current.toDouble() - filteredCurrent!!)
        }
        val smoothedCurrent = filteredCurrent!!.toInt()
        
        updateCapacityEstimate(capacity, batteryPct)
        trackLevelChanges(level, isCharging)
        
        val timeToFullCharge = calculateTimeToFullCharge(batteryPct, smoothedCurrent, isCharging)
        val timeToFullDischarge = calculateTimeToFullDischarge(batteryPct, smoothedCurrent, isCharging)
        
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
    
    private fun updateCapacityEstimate(currentCapacityMicroAh: Int, batteryPct: Float) {
        if (currentCapacityMicroAh > 0 && batteryPct > 10) {
            val capacityMah = currentCapacityMicroAh / 1000.0
            val estimatedFull = capacityMah / (batteryPct / 100.0)
            if (estimatedFull > 500 && estimatedFull < 10000) {
                estimatedFullCapacityMah = estimatedFull
            }
        }
    }
    
    private fun trackLevelChanges(currentLevel: Int, isCharging: Boolean) {
        val currentTime = System.currentTimeMillis()
        
        if (lastLevel != -1 && lastLevel != currentLevel && lastLevelChangeTime > 0) {
            val timeDiffHours = (currentTime - lastLevelChangeTime) / 3600000.0
            if (timeDiffHours > 0.001) {
                val levelDiff = abs(currentLevel - lastLevel)
                val rate = levelDiff / timeDiffHours
                
                if (rate > 0.1 && rate < 200) {
                    levelChangeRates.add(rate)
                    if (levelChangeRates.size > maxRateHistory) {
                        levelChangeRates.removeAt(0)
                    }
                }
            }
        }
        
        if (lastLevel != currentLevel) {
            lastLevel = currentLevel
            lastLevelChangeTime = currentTime
        }
    }
    
    private fun calculateTimeToFullCharge(batteryPct: Float, currentMicroA: Int, isCharging: Boolean): Long {
        if (!isCharging || batteryPct >= 100) {
            return -1L
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val systemEstimate = batteryManager?.computeChargeTimeRemaining() ?: -1L
            if (systemEstimate > 0) {
                return systemEstimate
            }
        }
        
        val currentMa = abs(currentMicroA / 1000.0)
        if (currentMa > 10) {
            val remainingPercent = 100 - batteryPct
            val remainingCapacityMah = (remainingPercent / 100.0) * estimatedFullCapacityMah
            val hoursToFull = remainingCapacityMah / currentMa
            val millisToFull = (hoursToFull * 3600000).toLong()
            
            if (millisToFull > 0 && millisToFull < 24 * 3600000) {
                return millisToFull
            }
        }
        
        if (levelChangeRates.isNotEmpty()) {
            val averageRate = levelChangeRates.average()
            val remainingPercent = 100 - batteryPct
            val hoursToFull = remainingPercent / averageRate
            return (hoursToFull * 3600000).toLong()
        }
        
        return -1L
    }
    
    private fun calculateTimeToFullDischarge(batteryPct: Float, currentMicroA: Int, isCharging: Boolean): Long {
        if (isCharging || batteryPct <= 0) {
            return -1L
        }
        
        val currentMa = abs(currentMicroA / 1000.0)
        if (currentMa > 10) {
            val currentCapacityMah = (batteryPct / 100.0) * estimatedFullCapacityMah
            val hoursToEmpty = currentCapacityMah / currentMa
            val millisToEmpty = (hoursToEmpty * 3600000).toLong()
            
            if (millisToEmpty > 0 && millisToEmpty < 72 * 3600000) {
                return millisToEmpty
            }
        }
        
        if (levelChangeRates.isNotEmpty()) {
            val averageRate = levelChangeRates.average()
            val hoursToEmpty = batteryPct / averageRate
            return (hoursToEmpty * 3600000).toLong()
        }
        
        val assumedDrainRateMa = 200.0
        val currentCapacityMah = (batteryPct / 100.0) * estimatedFullCapacityMah
        val hoursToEmpty = currentCapacityMah / assumedDrainRateMa
        return (hoursToEmpty * 3600000).toLong()
    }
}
