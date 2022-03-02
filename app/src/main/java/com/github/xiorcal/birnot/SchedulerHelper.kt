package com.github.xiorcal.birnot

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import androidx.preference.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class SchedulerHelper {
    companion object {


        fun scheduleNotification(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, SettingsActivity.ALARM_REQUEST_CODE,
                intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val hours = sharedPrefs.getString("notification_hour", "UNPROVIDED")?.toInt()
            val minutes = sharedPrefs.getString("notification_minute", "UNPROVIDED")?.toInt()

            if (hours != null && minutes != null) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hours)
                    set(Calendar.MINUTE, minutes)
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DATE, 1)
                    }
                }
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
                val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.FRANCE)
                val date = dateFormat.format(calendar.time)
                Log.i("BIRNO", "alarm scheduled for $date")
            } else {
                Log.e("BIRNO", "null hours or minutes provided")
            }
        }

        fun unScheduleNotification(context: Context) {
            Log.i("BIRNO", "un-scheduling notification")
            val intent = Intent()
            val pendingIntent = PendingIntent.getBroadcast(
                context, SettingsActivity.ALARM_REQUEST_CODE,
                intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            Log.i("BIRNO", "unscheduled notification")
        }



    }
}