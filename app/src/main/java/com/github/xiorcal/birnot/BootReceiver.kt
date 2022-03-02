package com.github.xiorcal.birnot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctxt: Context, i: Intent) {
        Log.i("BIRNO", "Boot received, re-scheduling notification")
        i.action?.let { Log.i("BIRNO", it) }
        if (i.action == Intent.ACTION_BOOT_COMPLETED) {
            SchedulerHelper.unScheduleNotification(ctxt)
            SchedulerHelper.scheduleNotification(ctxt)
        }
    }
}