package com.brianna.meditrack.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.brianna.meditrack.data.model.Medication;

import java.util.List;

@Dao
public interface MedicationDao {

    @Insert
    long insert(Medication medication);

    @Update
    void update(Medication medication);

    @Delete
    void delete(Medication medication);

    @Query("SELECT * FROM medications WHERE active = 1 ORDER BY name ASC")
    LiveData<List<Medication>> getAllActive();

    @Query("SELECT * FROM medications WHERE active = 1 AND category = :category ORDER BY name ASC")
    LiveData<List<Medication>> getByCategory(String category);

    @Query("SELECT * FROM medications WHERE id = :id")
    LiveData<Medication> getById(long id);

    @Query("SELECT * FROM medications WHERE id = :id")
    Medication getByIdSync(long id);

    @Query("SELECT * FROM medications WHERE active = 1")
    List<Medication> getAllActiveSync();

    @Query("SELECT COUNT(*) FROM medications WHERE active = 1")
    LiveData<Integer> getActiveMedicationCount();

    @Query("UPDATE medications SET pillsRemaining = pillsRemaining - 1 WHERE id = :id AND pillsRemaining > 0")
    void decrementPills(long id);
}
