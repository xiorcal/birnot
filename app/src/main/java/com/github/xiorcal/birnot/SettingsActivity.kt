package com.github.xiorcal.birnot

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import java.util.*


class SettingsActivity : AppCompatActivity() {

    companion object {
        const val ALARM_REQUEST_CODE = 847263

    }

    private val mPrefsListener =
        OnSharedPreferenceChangeListener { sharedPrefs, key ->
            when (key) {
                "sync" -> {
                    //do stuff
                    val newValue = sharedPrefs.getBoolean("sync", false)
                    Log.i("BIRNO", "sync value : $newValue")
                    if (newValue) {
                        requestContactAccess()
                    } else {
                        disableAll()
                    }
                }
                "notification" -> {
                    //do stuff
                    val currentValue = sharedPrefs.getBoolean("notification", false)
                    Log.i("BIRNO", "notification value : $currentValue")
                    val contactPermissionGranted = ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                    val notificationPermissionGranted = ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if(!notificationPermissionGranted){
                        requestNotificationAccess()
                    }
                    if (currentValue && contactPermissionGranted) {
                        SchedulerHelper.scheduleNotification(this)
                    } else {
                        SchedulerHelper.unScheduleNotification(this)
                    }
                }
                "notification_empty" -> {
                    //do stuff
                    val currentValue = sharedPrefs.getBoolean("notification_empty", false)
                    Log.i("BIRNO", "notification_empty value : $currentValue")
                }
                "notification_hour", "notification_minute" -> {
                    //do stuff
                    val newHour = sharedPrefs.getString("notification_hour", "NOTPROVIDED")
                    val newMinute = sharedPrefs.getString("notification_minute", "NOTPROVIDED")
                    Log.i("BIRNO", "notification_hour value : $newHour:$newMinute")
                    if (sharedPrefs.getBoolean("notification", false)) {
                        SchedulerHelper.scheduleNotification(this)
                    }
                }

            }
        }


    private fun requestContactAccess() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)) {
            PackageManager.PERMISSION_GRANTED -> {
                Log.i("BIRNO", "contact permission granted")
            }
            PackageManager.PERMISSION_DENIED -> {
                Log.i("BIRNO", "contact permission not granted")
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
                disableAll()
            }
        }
    }

    private fun requestNotificationAccess() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
            PackageManager.PERMISSION_GRANTED -> {
                Log.i("BIRNO", "notif permission granted")
            }
            PackageManager.PERMISSION_DENIED -> {
                Log.i("BIRNO", "notif permission not granted")
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    private fun disableAll() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPrefs
            .edit()
            .putBoolean("sync", false)
            .putBoolean("notification", false)
            .apply()
        SchedulerHelper.unScheduleNotification(this)
    }


    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(mPrefsListener);
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(mPrefsListener);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        findViewById<Button>(R.id.testNotifButton)
            .setOnClickListener {
                Log.d("BUTTONS", "User tapped the Supabutton")
                val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    this, ALARM_REQUEST_CODE,
                    intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                )
                pendingIntent.send()
            }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }


}