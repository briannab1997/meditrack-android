package com.brianna.meditrack.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {

    public static final String KEY_MED_ID   = "med_id";
    public static final String KEY_MED_NAME = "med_name";
    public static final String KEY_DOSAGE   = "dosage";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long medId   = getInputData().getLong(KEY_MED_ID, -1);
        String name  = getInputData().getString(KEY_MED_NAME);
        String dose  = getInputData().getString(KEY_DOSAGE);

        if (medId < 0 || name == null) return Result.failure();

        NotificationHelper.showDoseReminder(getApplicationContext(), medId, name, dose != null ? dose : "");
        return Result.success();
    }
}
