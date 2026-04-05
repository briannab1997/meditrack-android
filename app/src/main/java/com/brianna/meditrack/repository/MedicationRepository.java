package com.brianna.meditrack.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.brianna.meditrack.data.dao.DoseLogDao;
import com.brianna.meditrack.data.dao.MedicationDao;
import com.brianna.meditrack.data.db.MediTrackDatabase;
import com.brianna.meditrack.data.model.DoseLog;
import com.brianna.meditrack.data.model.Medication;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MedicationRepository {

    private final MedicationDao medicationDao;
    private final DoseLogDao doseLogDao;
    private final ExecutorService executor;

    public MedicationRepository(Application application) {
        MediTrackDatabase db = MediTrackDatabase.getInstance(application);
        medicationDao = db.medicationDao();
        doseLogDao = db.doseLogDao();
        executor = Executors.newFixedThreadPool(4);
    }

    // --- Medications ---

    public LiveData<List<Medication>> getAllActiveMedications() {
        return medicationDao.getAllActive();
    }

    public LiveData<List<Medication>> getMedicationsByCategory(String category) {
        return medicationDao.getByCategory(category);
    }

    public LiveData<Medication> getMedicationById(long id) {
        return medicationDao.getById(id);
    }

    public void insertMedication(Medication medication, Consumer<Long> onInserted) {
        executor.execute(() -> {
            long id = medicationDao.insert(medication);
            if (onInserted != null) onInserted.accept(id);
        });
    }

    public void updateMedication(Medication medication) {
        executor.execute(() -> medicationDao.update(medication));
    }

    public void deleteMedication(Medication medication) {
        executor.execute(() -> medicationDao.delete(medication));
    }

    public void decrementPills(long medicationId) {
        executor.execute(() -> medicationDao.decrementPills(medicationId));
    }

    // --- Dose Logs ---

    public LiveData<List<DoseLog>> getLogsForDate(String dateKey) {
        return doseLogDao.getLogsForDate(dateKey);
    }

    public LiveData<List<DoseLog>> getRecentLogsForMedication(long medId) {
        return doseLogDao.getRecentLogsForMedication(medId);
    }

    public LiveData<Integer> getTakenCountForDate(String dateKey) {
        return doseLogDao.getTakenCountForDate(dateKey);
    }

    public LiveData<Integer> getTotalScheduledForDate(String dateKey) {
        return doseLogDao.getTotalScheduledForDate(dateKey);
    }

    public LiveData<List<DoseLog>> getLogsForDateRange(String startKey, String endKey) {
        return doseLogDao.getLogsForDateRange(startKey, endKey);
    }

    public void insertDoseLog(DoseLog log) {
        executor.execute(() -> doseLogDao.insert(log));
    }

    public void updateDoseLog(DoseLog log) {
        executor.execute(() -> doseLogDao.update(log));
    }

    public void getRecentTakenDateKeys(Consumer<List<String>> callback) {
        executor.execute(() -> {
            List<String> keys = doseLogDao.getRecentTakenDateKeysSync();
            if (callback != null) callback.accept(keys);
        });
    }

    public void getMedicationStats(long medId, Consumer<int[]> callback) {
        executor.execute(() -> {
            int taken = doseLogDao.getTotalTakenCountSync(medId);
            int total = doseLogDao.getTotalLogsCountSync(medId);
            if (callback != null) callback.accept(new int[]{ taken, total });
        });
    }
}
