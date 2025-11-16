package cl.duoc.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class WateringReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val plantId = intent.getIntExtra(EXTRA_PLANT_ID, -1)
        val plantName = intent.getStringExtra(EXTRA_PLANT_NAME) ?: "tu planta"
        
        if (plantId != -1) {
            NotificationHelper.showWateringReminder(context, plantId, plantName)
        }
    }
    
    companion object {
        private const val EXTRA_PLANT_ID = "plant_id"
        private const val EXTRA_PLANT_NAME = "plant_name"
        
        fun scheduleWateringReminder(
            context: Context,
            plantId: Int,
            plantName: String,
            daysUntilWatering: Int
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WateringReminderReceiver::class.java).apply {
                putExtra(EXTRA_PLANT_ID, plantId)
                putExtra(EXTRA_PLANT_NAME, plantName)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                plantId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            // Calcular el tiempo para la notificación (9 AM del día correspondiente)
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, daysUntilWatering)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // Usar setExactAndAllowWhileIdle para que funcione incluso en modo Doze
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
        
        fun cancelWateringReminder(context: Context, plantId: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WateringReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                plantId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }
}
