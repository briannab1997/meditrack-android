package com.brianna.meditrack.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Reschedules medication reminders after the device reboots.
 * WorkManager persists scheduled work across reboots automatically,
 * but this receiver provides a hook for any additional setup if needed.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // WorkManager automatically re-enqueues persisted work after boot.
            // No manual rescheduling required.
        }
    }
}
