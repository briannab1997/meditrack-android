package com.brianna.meditrack.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.brianna.meditrack.data.model.DoseLog;

import java.util.List;

@Dao
public interface DoseLogDao {

    @Insert
    long insert(DoseLog log);

    @Update
    void update(DoseLog log);

    @Query("SELECT * FROM dose_logs WHERE dateKey = :dateKey ORDER BY scheduledTime ASC")
    LiveData<List<DoseLog>> getLogsForDate(String dateKey);

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medId ORDER BY scheduledTime DESC LIMIT 50")
    LiveData<List<DoseLog>> getRecentLogsForMedication(long medId);

    @Query("SELECT COUNT(*) FROM dose_logs WHERE status = 'TAKEN' AND dateKey = :dateKey")
    LiveData<Integer> getTakenCountForDate(String dateKey);

    @Query("SELECT COUNT(*) FROM dose_logs WHERE dateKey = :dateKey")
    LiveData<Integer> getTotalScheduledForDate(String dateKey);

    @Query("SELECT * FROM dose_logs WHERE dateKey >= :startKey AND dateKey <= :endKey ORDER BY scheduledTime ASC")
    LiveData<List<DoseLog>> getLogsForDateRange(String startKey, String endKey);

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medId AND dateKey = :dateKey")
    List<DoseLog> getLogsForMedAndDateSync(long medId, String dateKey);

    @Query("SELECT DISTINCT dateKey FROM dose_logs WHERE status = 'TAKEN' ORDER BY dateKey DESC LIMIT 60")
    List<String> getRecentTakenDateKeysSync();

    @Query("SELECT COUNT(*) FROM dose_logs WHERE medicationId = :medId AND status = 'TAKEN'")
    int getTotalTakenCountSync(long medId);

    @Query("SELECT COUNT(*) FROM dose_logs WHERE medicationId = :medId")
    int getTotalLogsCountSync(long medId);

    @Query("DELETE FROM dose_logs WHERE medicationId = :medId")
    void deleteAllForMedication(long medId);
}
