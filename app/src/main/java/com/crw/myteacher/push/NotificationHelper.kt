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

    private const val CHANNEL_NAME_MESSAGES = "Wiadomości"

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

        notificationManager.createNotificationChannel(messagesChannel)
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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(senderName)
            .setContentText(messageContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageContent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(chatRoomId.hashCode(), notification)
    }

    const val EXTRA_CHAT_ROOM_ID = "extra_chat_room_id"
}

