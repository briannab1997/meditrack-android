package com.brianna.meditrack.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.brianna.meditrack.data.dao.DoseLogDao;
import com.brianna.meditrack.data.dao.MedicationDao;
import com.brianna.meditrack.data.model.DoseLog;
import com.brianna.meditrack.data.model.Medication;

@Database(
    entities = { Medication.class, DoseLog.class },
    version = 1,
    exportSchema = false
)
public abstract class MediTrackDatabase extends RoomDatabase {

    private static volatile MediTrackDatabase INSTANCE;

    public abstract MedicationDao medicationDao();
    public abstract DoseLogDao doseLogDao();

    public static MediTrackDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MediTrackDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MediTrackDatabase.class,
                            "meditrack_database"
                        )
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }
}
