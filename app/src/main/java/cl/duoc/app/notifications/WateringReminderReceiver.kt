package cl.duoc.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class WateringReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val plantName = intent.getStringExtra(EXTRA_PLANT_NAME) ?: "tu planta"
        val plantId = intent.getIntExtra(EXTRA_PLANT_ID, 0)
        
        NotificationHelper.showWateringReminder(context, plantName, plantId)
    }
    
    companion object {
        private const val EXTRA_PLANT_NAME = "plant_name"
        private const val EXTRA_PLANT_ID = "plant_id"
        
        fun scheduleWateringReminder(
            context: Context,
            plantId: Int,
            plantName: String,
            daysUntilWatering: Int
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WateringReminderReceiver::class.java).apply {
                putExtra(EXTRA_PLANT_NAME, plantName)
                putExtra(EXTRA_PLANT_ID, plantId)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                plantId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Calcular el tiempo para la alarma
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, daysUntilWatering)
                set(Calendar.HOUR_OF_DAY, 9) // 9 AM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            
            // Programar alarma
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        
        fun cancelWateringReminder(context: Context, plantId: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WateringReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                plantId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
