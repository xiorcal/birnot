package com.github.xiorcal.birnot

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import java.util.*


@RequiresApi(Build.VERSION_CODES.N)
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
                    val permissionGranted = ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (currentValue && permissionGranted) {
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
                    SchedulerHelper.scheduleNotification(this)
                }

            }
        }

    

    


    private fun requestContactAccess() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)) {
            PackageManager.PERMISSION_GRANTED -> {
                Log.i("BIRNO", "permission granted")
            }
            PackageManager.PERMISSION_DENIED -> {
                Log.i("BIRNO", "permission not granted")
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
                disableAll()
            }

        }

        // Permission is not granted

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

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }


}