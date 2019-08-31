package com.androiddeveloper3005.mychat.cloudMessagingService

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.androiddeveloper3005.mychat.R
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {
   override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val notification_title = remoteMessage.getNotification()?.getTitle()
        val notification_message = remoteMessage.getNotification()?.getBody()
        val click_action = remoteMessage.getNotification()?.getClickAction()
        val from_user_id = remoteMessage.getData().get("from_user_id")
        val mBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification_title)
            .setContentText(notification_message)
        val resultIntent = Intent(click_action)
        resultIntent.putExtra("user_id", from_user_id)
        val resultPendingIntent = PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationId = System.currentTimeMillis().toInt()
        val mNotifyMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.notify(mNotificationId, mBuilder.build())
    }
}