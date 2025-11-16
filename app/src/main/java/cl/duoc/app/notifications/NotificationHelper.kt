package cl.duoc.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cl.duoc.app.MainActivity
import cl.duoc.app.R

object NotificationHelper {
    
    private const val CHANNEL_ID = "plant_watering_channel"
    private const val CHANNEL_NAME = "Recordatorios de Riego"
    private const val CHANNEL_DESCRIPTION = "Notificaciones para recordarte regar tus plantas"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showNotification(context: Context, title: String, message: String, notificationId: Int = 1) {
        if (!NotificationPermissionHelper.hasNotificationPermission(context)) {
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    fun showWateringReminder(context: Context, plantId: Int, plantName: String) {
        val title = "🌱 Hora de regar"
        val message = "Es momento de regar tu $plantName"
        showNotification(context, title, message, plantId)
    }
    
    fun showNotificationsEnabled(context: Context, plantName: String, daysUntilWatering: Int) {
        val title = "✅ Notificaciones activadas"
        val message = "Te recordaremos regar tu $plantName en $daysUntilWatering días"
        showNotification(context, title, message, System.currentTimeMillis().toInt())
    }
}
