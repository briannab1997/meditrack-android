package com.brianna.meditrack.util;

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.brianna.meditrack.data.model.Medication;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    // WorkManager tag prefix used to cancel all reminders for a given medication
    private static final String TAG_PREFIX = "reminder_med_";

    public static void schedule(Context context, Medication medication) {
        if (medication.getScheduleTimes() == null || medication.getScheduleTimes().isEmpty()) return;

        WorkManager wm = WorkManager.getInstance(context.getApplicationContext());
        String tag = TAG_PREFIX + medication.getId();

        // Cancel any existing reminders for this medication before scheduling fresh ones
        wm.cancelAllWorkByTag(tag);

        String[] times = medication.getTimeArray();
        for (String time : times) {
            long delayMillis = millisUntilNextOccurrence(time);
            if (delayMillis < 0) continue;

            Data inputData = new Data.Builder()
                    .putLong(ReminderWorker.KEY_MED_ID, medication.getId())
                    .putString(ReminderWorker.KEY_MED_NAME, medication.getName())
                    .putString(ReminderWorker.KEY_DOSAGE, medication.getDosage())
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag(tag)
                    .build();

            wm.enqueue(request);
        }
    }

    public static void cancel(Context context, long medicationId) {
        WorkManager.getInstance(context.getApplicationContext())
                .cancelAllWorkByTag(TAG_PREFIX + medicationId);
    }

    private static long millisUntilNextOccurrence(String hhmm) {
        try {
            String[] parts = hhmm.split(":");
            int hour   = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar next = Calendar.getInstance();
            next.set(Calendar.HOUR_OF_DAY, hour);
            next.set(Calendar.MINUTE, minute);
            next.set(Calendar.SECOND, 0);
            next.set(Calendar.MILLISECOND, 0);

            // If this time has already passed today, schedule for tomorrow
            if (next.getTimeInMillis() <= System.currentTimeMillis()) {
                next.add(Calendar.DAY_OF_YEAR, 1);
            }

            return next.getTimeInMillis() - System.currentTimeMillis();
        } catch (Exception e) {
            return -1;
        }
    }
}
