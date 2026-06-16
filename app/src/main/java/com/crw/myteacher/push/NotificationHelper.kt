package com.crw.myteacher.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.crw.myteacher.MainActivity
import com.crw.myteacher.R

object NotificationHelper {

    const val CHANNEL_ID_MESSAGES = "messages_channel"
    const val CHANNEL_ID_SERVICE = "service_channel"
    const val EXTRA_CHAT_ROOM_ID = "extra_chat_room_id"

    private const val CHANNEL_NAME_MESSAGES = "Wiadomości"
    private const val CHANNEL_NAME_SERVICE = "Serwis wiadomości"

    fun createNotificationChannels(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val messagesChannel = NotificationChannel(
            CHANNEL_ID_MESSAGES,
            CHANNEL_NAME_MESSAGES,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Powiadomienia o nowych wiadomościach"
            enableVibration(true)
        }

        val serviceChannel = NotificationChannel(
            CHANNEL_ID_SERVICE,
            CHANNEL_NAME_SERVICE,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Utrzymywanie połączenia z serwerem wiadomości"
            setShowBadge(false)
        }

        notificationManager.createNotificationChannel(messagesChannel)
        notificationManager.createNotificationChannel(serviceChannel)
    }

    fun showMessageNotification(
        context: Context,
        chatRoomId: String,
        senderName: String,
        messageContent: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CHAT_ROOM_ID, chatRoomId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            chatRoomId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MESSAGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF0D4CCC.toInt())
            .setContentTitle(senderName)
            .setContentText(messageContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageContent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(chatRoomId.hashCode(), notification)
    }

    fun buildServiceNotification(context: Context): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF0D4CCC.toInt())
            .setContentTitle("MyTeacher")
            .setContentText("Nasłuchiwanie nowych wiadomości…")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
