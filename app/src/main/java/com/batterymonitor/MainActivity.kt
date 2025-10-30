package com.batterymonitor

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.batterymonitor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var batteryMonitor: BatteryMonitor
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L
    
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateBatteryInfo()
            handler.postDelayed(this, updateInterval)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        batteryMonitor = BatteryMonitor(this)
        
        setupUI()
        startUpdates()
    }
    
    private fun setupUI() {
        supportActionBar?.title = "Battery Monitor"
    }
    
    private fun updateBatteryInfo() {
        val data = batteryMonitor.getBatteryData()
        
        binding.apply {
            textVoltage.text = data.getVoltageString()
            textCurrent.text = data.getCurrentString()
            textPower.text = data.getPowerString()
            textLevel.text = data.getLevelString()
            textTemperature.text = data.getTemperatureString()
            textStatus.text = data.status
            textHealth.text = data.health
            textTechnology.text = data.technology
            
            if (data.isCharging) {
                textTimeToFull.text = data.getTimeToFullChargeString()
                labelTimeToFull.text = "Time to Full Charge:"
                textTimeToFull.visibility = android.view.View.VISIBLE
                labelTimeToFull.visibility = android.view.View.VISIBLE
                textTimeToEmpty.visibility = android.view.View.GONE
                labelTimeToEmpty.visibility = android.view.View.GONE
            } else {
                textTimeToEmpty.text = data.getTimeToFullDischargeString()
                labelTimeToEmpty.text = "Time to Empty:"
                textTimeToEmpty.visibility = android.view.View.VISIBLE
                labelTimeToEmpty.visibility = android.view.View.VISIBLE
                textTimeToFull.visibility = android.view.View.GONE
                labelTimeToFull.visibility = android.view.View.GONE
            }
            
            progressBar.progress = data.level
            
            val statusColor = if (data.isCharging) {
                getColor(android.R.color.holo_green_dark)
            } else {
                when {
                    data.level > 50 -> getColor(android.R.color.holo_green_light)
                    data.level > 20 -> getColor(android.R.color.holo_orange_light)
                    else -> getColor(android.R.color.holo_red_light)
                }
            }
            cardStatus.setCardBackgroundColor(statusColor)
        }
    }
    
    private fun startUpdates() {
        handler.post(updateRunnable)
    }
    
    private fun stopUpdates() {
        handler.removeCallbacks(updateRunnable)
    }
    
    override fun onResume() {
        super.onResume()
        startUpdates()
    }
    
    override fun onPause() {
        super.onPause()
        stopUpdates()
    }
}
