package com.example.reservation_system.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.reservation_system.R
import com.example.reservation_system.ui.act_hizmet_alan.navigation_menu.HizmetAlanNavigationMenuPage
import com.example.reservation_system.ui.act_hizmet_veren.navigation_menu.HizmetVerenNavigationMenuPage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var NOTIFICATION_CHANNEL_ID = "com.example.reservation_system.notification"
    private val NOTIFICATION_ID = 0
    private var uid : String = ""
    private lateinit var db: FirebaseFirestore

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        db = FirebaseFirestore.getInstance()

        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            showNotification(applicationContext, title, body)
        } else {
            val title = remoteMessage.notification!!.title
            val body = remoteMessage.notification!!.body
            showNotification(applicationContext, title, body)
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.e("token", "New Token")
    }

    fun showNotification(
            context: Context,
            title: String?,
            message: String?
    ) {
        val ii: Intent = Intent(context, HizmetVerenNavigationMenuPage::class.java)
        ii.data = Uri.parse("custom://" + System.currentTimeMillis())
        ii.action = "actionstring" + System.currentTimeMillis()
        ii.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        ii.putExtra("data", "fromoutside")
        val pi =
                PendingIntent.getActivity(context, 0, ii, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setOngoing(true)
                    .setSmallIcon(getNotificationIcon())
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setWhen(System.currentTimeMillis())
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentTitle(title).build()
            val notificationManager = context.getSystemService(
                    Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    title,
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } else {
            notification = NotificationCompat.Builder(context)
                    .setSmallIcon(getNotificationIcon())
                    .setAutoCancel(true)
                    .setContentText(message)
                    .setContentIntent(pi)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentTitle(title).build()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)

        }

    }



    private fun getNotificationIcon(): Int {
        val useWhiteIcon =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        return if (useWhiteIcon) R.mipmap.ic_launcher else R.mipmap.ic_launcher
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification(from:String, to:String, text:String, randevuId:String){
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val date = formatter.format(now).toString()
        val notification = hashMapOf<String, Any>(
                "from" to from,
                "to" to to,
                "text" to text,
                "randevuId" to randevuId,
                "date" to date,
                "gone" to false)
        FirebaseFirestore.getInstance().collection("notifications").add(notification)
                .addOnSuccessListener { documentReference ->
                    documentReference.update("notificationId",documentReference.id)
                }
    }
}