package com.crw.myteacher.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.crw.myteacher.MainActivity
import com.crw.myteacher.R

/**
 * Helper do tworzenia kanałów i wyświetlania powiadomień o nowych wiadomościach.
 */
object NotificationHelper {

    const val CHANNEL_ID_MESSAGES = "messages_channel"
    const val CHANNEL_ID_SERVICE = "service_channel"

    private const val CHANNEL_NAME_MESSAGES = "Wiadomości"
    private const val CHANNEL_NAME_SERVICE = "Serwis wiadomości"

    /**
     * Tworzy kanały powiadomień (wymagane od Android 8.0+).
     * Bezpieczne do wielokrotnego wywołania — system ignoruje duplikaty.
     */
    fun createNotificationChannels(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Kanał dla wiadomości czatowych — wysoki priorytet z dźwiękiem
        val messagesChannel = NotificationChannel(
            CHANNEL_ID_MESSAGES,
            CHANNEL_NAME_MESSAGES,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Powiadomienia o nowych wiadomościach"
            enableVibration(true)
        }

        // Kanał dla foreground service — niski priorytet, cichy
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

    /**
     * Wyświetla powiadomienie o nowej wiadomości.
     *
     * @param context Kontekst aplikacji
     * @param chatRoomId ID pokoju czatowego (do deep-linku)
     * @param senderName Nazwa nadawcy
     * @param messageContent Treść wiadomości
     */
    fun showMessageNotification(
        context: Context,
        chatRoomId: String,
        senderName: String,
        messageContent: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent otwierający aplikację po kliknięciu powiadomienia
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

        // Używamy hashCode chatRoomId jako ID powiadomienia,
        // dzięki czemu nowe wiadomości z tego samego pokoju nadpisują stare
        notificationManager.notify(chatRoomId.hashCode(), notification)
    }

    /**
     * Tworzy powiadomienie foreground service (wymagane do działania w tle).
     */
    fun buildServiceNotification(context: Context): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("MyTeacher")
            .setContentText("Nasłuchiwanie nowych wiadomości…")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    const val EXTRA_CHAT_ROOM_ID = "extra_chat_room_id"
}

