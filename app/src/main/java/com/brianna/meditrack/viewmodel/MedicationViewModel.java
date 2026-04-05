package com.brianna.meditrack.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.brianna.meditrack.data.model.DoseLog;
import com.brianna.meditrack.data.model.Medication;
import com.brianna.meditrack.repository.MedicationRepository;
import com.brianna.meditrack.util.DateUtils;

import java.util.List;
import java.util.function.Consumer;

public class MedicationViewModel extends AndroidViewModel {

    private final MedicationRepository repository;

    private final LiveData<List<Medication>> allMedications;
    private final MutableLiveData<Integer> streak = new MutableLiveData<>(0);

    public MedicationViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicationRepository(application);
        allMedications = repository.getAllActiveMedications();
        computeStreak();
    }

    // --- Medications ---

    public LiveData<List<Medication>> getAllMedications() {
        return allMedications;
    }

    public LiveData<List<Medication>> getMedicationsByCategory(String category) {
        return repository.getMedicationsByCategory(category);
    }

    public LiveData<Medication> getMedicationById(long id) {
        return repository.getMedicationById(id);
    }

    public void insertMedication(Medication medication, Consumer<Long> onInserted) {
        repository.insertMedication(medication, onInserted);
    }

    public void updateMedication(Medication medication) {
        repository.updateMedication(medication);
    }

    public void deleteMedication(Medication medication) {
        repository.deleteMedication(medication);
    }

    // --- Dose Logs ---

    public LiveData<List<DoseLog>> getLogsForToday() {
        return repository.getLogsForDate(DateUtils.todayKey());
    }

    public LiveData<List<DoseLog>> getLogsForDate(String dateKey) {
        return repository.getLogsForDate(dateKey);
    }

    public LiveData<List<DoseLog>> getRecentLogsForMedication(long medId) {
        return repository.getRecentLogsForMedication(medId);
    }

    public LiveData<Integer> getTodayTakenCount() {
        return repository.getTakenCountForDate(DateUtils.todayKey());
    }

    public LiveData<Integer> getTodayTotalCount() {
        return repository.getTotalScheduledForDate(DateUtils.todayKey());
    }

    public LiveData<List<DoseLog>> getLogsForDateRange(String startKey, String endKey) {
        return repository.getLogsForDateRange(startKey, endKey);
    }

    public void logDoseTaken(Medication medication, long scheduledTime) {
        DoseLog log = new DoseLog();
        log.setMedicationId(medication.getId());
        log.setMedicationName(medication.getName());
        log.setScheduledTime(scheduledTime);
        log.setTakenTime(System.currentTimeMillis());
        log.setStatus(DoseLog.STATUS_TAKEN);
        log.setDateKey(DateUtils.todayKey());
        repository.insertDoseLog(log);
        repository.decrementPills(medication.getId());
        computeStreak();
    }

    public void updateDoseLog(DoseLog log) {
        repository.updateDoseLog(log);
    }

    public void getMedicationStats(long medId, Consumer<int[]> callback) {
        repository.getMedicationStats(medId, callback);
    }

    // --- Streak ---

    public LiveData<Integer> getStreak() {
        return streak;
    }

    private void computeStreak() {
        repository.getRecentTakenDateKeys(dateKeys -> {
            if (dateKeys == null || dateKeys.isEmpty()) {
                streak.postValue(0);
                return;
            }
            int count = 0;
            String expected = DateUtils.offsetDayKey(-count);
            for (String key : dateKeys) {
                if (key.equals(expected)) {
                    count++;
                    expected = DateUtils.offsetDayKey(-count);
                } else {
                    break;
                }
            }
            streak.postValue(count);
        });
    }
}
