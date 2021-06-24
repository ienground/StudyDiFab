package net.ienlab.study.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import net.ienlab.study.R

class AlarmReceiver: BroadcastReceiver() {
    val channelId = "studyplz"

    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(channelId, context.getString(R.string.study_remind), NotificationManager.IMPORTANCE_DEFAULT))
        }

        NotificationCompat.Builder(context, channelId).apply {
            setContentTitle(context.getString(R.string.title_study_remind))
            setContentText(context.getString(R.string.message_study_remind))
            setSmallIcon(R.drawable.ic_icon)

            nm.notify(2552, build())
        }
    }
}