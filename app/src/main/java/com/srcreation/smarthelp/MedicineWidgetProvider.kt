package com.srcreation.smarthelp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import com.srcreation.smarthelp.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MedicineWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TAKE_MEDICINE -> {
                val medId = intent.getStringExtra(EXTRA_MED_ID) ?: ""
                if (medId.isNotBlank()) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            MedicineRepository(context).markDoseTaken(medId)
                            val db = AppDatabase.getDatabase(context)
                            val medList = db.medicineDao().getFullMedicineList()
                            val med = medList.find { it.id == medId }
                            med?.let {
                                // 🌐 1-Tap Cloud Sync to Caregiver
                                CaregiverSyncRepository.syncMedicineTaken(
                                    context = context,
                                    medicineName = it.name,
                                    dosage = "${it.dosageValue} ${it.dosageUnit}".trim(),
                                    timeCategory = it.selectedTimes,
                                    status = "TAKEN"
                                )

                                // ⚠️ Refill Alert Check
                                if (it.currentStock <= 3) {
                                    val title = context.getString(R.string.med_refill_alert_title)
                                    val body = context.getString(R.string.med_refill_alert_body, it.currentStock, it.dosageUnit, it.name)
                                    NotificationHelper.showNotification(context, title, body)
                                }

                                withContext(Dispatchers.Main) {
                                    val toastText = "✓ ${it.name} - ${context.getString(R.string.med_taken_btn)}! 💊"
                                    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                                }
                            }
                            updateAllWidgets(context)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
            ACTION_REFRESH_WIDGET -> {
                updateAllWidgets(context)
            }
        }
    }

    companion object {
        const val ACTION_TAKE_MEDICINE = "com.srcreation.smarthelp.ACTION_TAKE_MEDICINE"
        const val ACTION_REFRESH_WIDGET = "com.srcreation.smarthelp.ACTION_REFRESH_WIDGET"
        const val EXTRA_MED_ID = "EXTRA_MED_ID"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MedicineWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val medicines = db.medicineDao().getFullMedicineList()
                val nextMed = medicines.filter { it.isActive }.find { !isTakenToday(it.lastTakenTimestamp) } 
                    ?: medicines.find { it.isActive }

                val views = RemoteViews(context.packageName, R.layout.widget_medicine)

                if (nextMed != null) {
                    views.setTextViewText(R.id.widget_med_name, nextMed.name)
                    views.setTextViewText(
                        R.id.widget_med_details, 
                        "${nextMed.dosageValue} ${nextMed.dosageUnit} • ${nextMed.foodRelation}"
                    )
                    views.setTextViewText(R.id.widget_stock_badge, "Stock: ${nextMed.currentStock}")
                    views.setTextViewText(
                        R.id.btn_widget_take,
                        "✓ " + context.getString(R.string.med_taken_btn) + " 💊"
                    )

                    // Take Now PendingIntent
                    val takeIntent = Intent(context, MedicineWidgetProvider::class.java).apply {
                        action = ACTION_TAKE_MEDICINE
                        putExtra(EXTRA_MED_ID, nextMed.id)
                    }
                    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
                    val takePendingIntent = PendingIntent.getBroadcast(
                        context, nextMed.id.hashCode(), takeIntent, flags
                    )
                    views.setOnClickPendingIntent(R.id.btn_widget_take, takePendingIntent)
                } else {
                    views.setTextViewText(R.id.widget_med_name, "All Done! 🎉")
                    views.setTextViewText(R.id.widget_med_details, "No upcoming medicine for today")
                    views.setTextViewText(R.id.widget_stock_badge, "Stock: --")
                }

                // Header Intent -> Open HomeActivity
                val homeIntent = Intent(context, HomeActivity::class.java)
                val homeFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val homePendingIntent = PendingIntent.getActivity(context, 0, homeIntent, homeFlags)
                views.setOnClickPendingIntent(R.id.widget_container, homePendingIntent)

                // Emergency QR PendingIntent -> Open MedicalQrActivity
                val qrIntent = Intent(context, MedicalQrActivity::class.java)
                val qrPendingIntent = PendingIntent.getActivity(context, 1, qrIntent, homeFlags)
                views.setOnClickPendingIntent(R.id.btn_widget_qr, qrPendingIntent)

                withContext(Dispatchers.Main) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        private fun isTakenToday(timestamp: Long): Boolean {
            if (timestamp == 0L) return false
            val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
            val cal2 = Calendar.getInstance()
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }
    }
}
