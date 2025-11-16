package cl.duoc.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cl.duoc.app.R

object NotificationHelper {
    private const val CHANNEL_ID = "plant_watering_channel"
    private const val CHANNEL_NAME = "Recordatorios de Riego"
    private const val CHANNEL_DESCRIPTION = "Notificaciones para recordar regar las plantas"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            android.util.Log.d("NotificationHelper", "Canal de notificaciones creado")
        }
    }
    
    fun showNotification(context: Context, title: String, message: String, notificationId: Int = 0) {
        createNotificationChannel(context)
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 250, 250, 250))
        
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Para MIUI: Verificar si las notificaciones están habilitadas
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.areNotificationsEnabled()) {
                android.util.Log.w("NotificationHelper", "Notificaciones deshabilitadas por el usuario")
                throw SecurityException("Notificaciones deshabilitadas")
            }
            
            notificationManager.notify(notificationId, builder.build())
            android.util.Log.d("NotificationHelper", "Notificación enviada: $title")
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationHelper", "Error de seguridad al mostrar notificación", e)
            throw e
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Error al mostrar notificación", e)
            e.printStackTrace()
        }
    }
    
    fun showWateringReminder(context: Context, plantName: String, plantId: Int) {
        showNotification(
            context = context,
            title = "¡Hora de regar!",
            message = "Es momento de regar tu $plantName",
            notificationId = plantId
        )
    }
    
    fun showNotificationsEnabled(context: Context, plantName: String, daysUntilWatering: Int) {
        showNotification(
            context = context,
            title = "¡Notificaciones activadas!",
            message = "¡Te avisaré dentro de $daysUntilWatering días para regar $plantName!",
            notificationId = System.currentTimeMillis().toInt()
        )
    }
}
