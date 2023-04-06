package com.example.yama

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.provider.Settings
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.PreferenceManager

class MyBroadcastReceiver:BroadcastReceiver() {
    private val NOTIFICATION_CHANNEL_ID ="my_channel_01"
    private val NOTIFICATION_ID = 10
    private val NOTIFICATION_REQUEST = 1
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in smsMessages) {
                // Log.e("MyBroadcastReceiver", message.toString())
                Toast.makeText(
                    context,
                    "message from ${message.displayOriginatingAddress}:body ${message.messageBody}",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
        //notifications turned on
        if (prefs.getBoolean("notifications", true)) {
            val builder = NotificationCompat.Builder(context!!, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_local_florist_24)
                .setContentTitle("Message incoming")
                .setContentText("You have received a message")
            //set up channels
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Message Channel",
                    NotificationManager.IMPORTANCE_HIGH
                )

                val notificationManager: NotificationManager =
                    getSystemService(
                        context,
                        NotificationManager::class.java
                    ) as NotificationManager
                notificationManager.createNotificationChannel(channel)//register with the system
            }else {
                //everything else!
                builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                builder.setVibrate(longArrayOf(0, 500, 500, 5000))
                builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            }
           // val intnt = Intent(this, ReadMessage::class.java)
            val pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT)//FLAG_UPDATE_CURRENT)

            builder.setContentIntent(pendingIntent)
            //show the notification!

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())

        }
        else{
            //notifications turned off!
            Toast.makeText(context, "This notice has been generated 1 times", Toast.LENGTH_LONG).show()
        }
    }
}