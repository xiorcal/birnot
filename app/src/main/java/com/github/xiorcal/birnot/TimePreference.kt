package com.github.xiorcal.birnot

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.format.DateFormat.is24HourFormat
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.github.xiorcal.birnot.databinding.TimePickerLayoutBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// A custom preference to show a time picker
@RequiresApi(Build.VERSION_CODES.O)
class TimePickerPreference(context: Context?, attrs: AttributeSet?) : Preference(context!!, attrs),
    View.OnClickListener {
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var currentHour: String
    private lateinit var currentMinute: String
    private lateinit var binding: TimePickerLayoutBinding
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        currentHour = sharedPrefs.getString("notification_hour", "8").toString()
        currentMinute = sharedPrefs.getString("notification_minute", "0").toString()
        super.onBindViewHolder(holder)
        binding = TimePickerLayoutBinding.bind(holder.itemView)

        // Format the time correctly
        val currentTime = LocalTime.of(currentHour.toInt(), currentMinute.toInt())

        binding.timePickerDescription.text =
            String.format(
                context.getString(R.string.notification_timepicker_desc),
                "~${formatter.format(currentTime)}"
            )

        binding.root.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val act = context as SettingsActivity
        currentHour = sharedPrefs.getString("notification_hour", "8").toString()
        currentMinute = sharedPrefs.getString("notification_minute", "0").toString()

        // Show the time picker
        val isSystem24Hour = is24HourFormat(context)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(currentHour.toInt())
                .setMinute(currentMinute.toInt())
                .setTitleText(context.getString(R.string.notification_timepicker_desc))
                .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                .build()

        picker.addOnPositiveButtonClickListener {
            val editor = sharedPrefs.edit()
            editor.putString("notification_hour", "${picker.hour}")
            editor.putString("notification_minute", "${picker.minute}")
            editor.apply()

            // Format the selected hour and update the text
            val currentTime = LocalTime.of(picker.hour, picker.minute)

            binding.timePickerDescription.text =
                String.format(
                    context.getString(R.string.notification_timepicker_desc),
                    "~${formatter.format(currentTime)}"
                )
        }

        picker.show(act.supportFragmentManager, "timepicker")
    }

}