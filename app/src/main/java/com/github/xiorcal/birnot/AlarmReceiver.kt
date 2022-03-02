package com.github.xiorcal.birnot

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.github.xiorcal.birnot.model.EventInfo
import com.github.xiorcal.birnot.model.EventType
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_ID = 748513695
        const val NOTIFICATION_CHANNEL_ID = "birno_notification_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alwaysNotify = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("notification_empty", false)

        val contacts = findContactForToday(context)
        if (contacts.isNotEmpty() || alwaysNotify) {
            createNotificationChannel(context)
            sendNotification(context, contacts)
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun findContactForToday(context: Context): List<EventInfo> {
        val monthF: DateFormat = SimpleDateFormat("MM")
        val dayF: DateFormat = SimpleDateFormat("dd")
        val now = Date()
        val todayMonth = monthF.format(now)
        val todayDay = dayF.format(now)

        val allContactsWithEvent = mutableListOf<EventInfo>()
        // Get the ContentResolver
        val cr = context.contentResolver
        // Get the Cursor of all the contacts
        val uri: Uri = ContactsContract.Data.CONTENT_URI

        val projection = arrayOf(
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts._ID,
            ContactsContract.CommonDataKinds.Event.START_DATE,
            ContactsContract.CommonDataKinds.Event.TYPE,
            ContactsContract.CommonDataKinds.Event.LABEL
        )
        val where = ContactsContract.Data.MIMETYPE + "= ?"
        val selectionArgs = arrayOf(
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
        )
        val cursor = cr.query(
            uri,
            projection,
            where,
            selectionArgs,
            ContactsContract.Contacts.SORT_KEY_PRIMARY + " ASC"
        )
        if (cursor != null) {
            val index = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val dateColumn =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
            val typeCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE)
            val labelCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.LABEL)

            while (cursor.moveToNext()) {
                val name = cursor.getString(index)
                val date = cursor.getString(dateColumn)
                val type = cursor.getString(typeCol)
                val label = cursor.getString(labelCol)
                allContactsWithEvent.add(EventInfo.createFromAndroidInfos(name, type, label, date))
            }
            // Close the cursor
            cursor.close()
        }
        return allContactsWithEvent.filter { e -> e.eventDateMonth == todayMonth && e.eventDateDay == todayDay }
    }


    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.app_name), importance).apply {
                description = context.getString(R.string.notification_channel_description)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


    }

    private fun sendNotification(context: Context, events: List<EventInfo>) {
        val title: String
        var body = ""
        when (events.size) {
            0 -> {
                title = context.getString(R.string.today_no_birthday); body = ""
            }
            1 -> {
                val event = events[0]
                if (event.eventType == EventType.BIRTHDAY) {
                    title = context.getString(R.string.today_one_birthday, event.contactName) ; body = ""
                } else {
                    title = context.getString(R.string.today_one_date, event.contactName); body =
                        context.getString(R.string.today_one_date_details, event.eventLabel)
                }
            }
            else -> {
                title = context.getString(R.string.today_multiple_birthdays)
                for (e in events) {
                    if (e.eventType == EventType.BIRTHDAY) {
                        body += context.getString(R.string.today_multiple_birthdays_detail, e.contactName)
                    } else {
                        body += context.getString(R.string.today_multiple_birthdays_date, e.contactName, e.eventLabel)
                    }
                }
            }
        }
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            notify(NOTIFICATION_ID, build.build())
        }

    }


}
