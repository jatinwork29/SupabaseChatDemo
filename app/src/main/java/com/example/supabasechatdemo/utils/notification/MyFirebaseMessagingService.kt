package com.example.supabasechatdemo.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.example.supabasechatdemo.utils.Utils.Companion.isOnline
import com.example.supabasechatdemo.R
import com.example.supabasechatdemo.activity.LoginActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            val message = remoteMessage.getNotification()!!.body
            val title = remoteMessage.getNotification()!!.title
            if (!isOnline) {
                sendNotification(message,title)
            }
        }
    }

    private fun sendNotification(messageBody: String?, title: String?) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel for Android 8.0 and higher.
        val channel = NotificationChannel(
            channelId,
            getString(R.string.default_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0, notificationBuilder.build())
    }
}