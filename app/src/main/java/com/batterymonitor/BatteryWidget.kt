package com.batterymonitor

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews

class BatteryWidget : AppWidgetProvider() {
    
    companion object {
        private const val ACTION_UPDATE = "com.batterymonitor.UPDATE_WIDGET"
        private val handler = Handler(Looper.getMainLooper())
        private var updateRunnable: Runnable? = null
        
        fun startPeriodicUpdate(context: Context) {
            stopPeriodicUpdate()
            updateRunnable = object : Runnable {
                override fun run() {
                    updateAllWidgets(context)
                    handler.postDelayed(this, 2000)
                }
            }
            handler.post(updateRunnable!!)
        }
        
        fun stopPeriodicUpdate() {
            updateRunnable?.let {
                handler.removeCallbacks(it)
                updateRunnable = null
            }
        }
        
        private fun updateAllWidgets(context: Context) {
            val intent = Intent(context, BatteryWidget::class.java).apply {
                action = ACTION_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, BatteryWidget::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        startPeriodicUpdate(context)
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        stopPeriodicUpdate()
    }
    
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val batteryMonitor = BatteryMonitor(context)
        val data = batteryMonitor.getBatteryData()
        
        val views = RemoteViews(context.packageName, R.layout.battery_widget)
        
        views.setTextViewText(R.id.widget_voltage, data.getVoltageString())
        views.setTextViewText(R.id.widget_current, data.getCurrentString())
        views.setTextViewText(R.id.widget_power, data.getPowerString())
        views.setTextViewText(R.id.widget_level, data.getLevelString())
        
        if (data.isCharging) {
            views.setTextViewText(R.id.widget_time, "Charge: ${data.getTimeToFullChargeString()}")
        } else {
            views.setTextViewText(R.id.widget_time, "Discharge: ${data.getTimeToFullDischargeString()}")
        }
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
