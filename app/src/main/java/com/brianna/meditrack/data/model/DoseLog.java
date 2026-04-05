package com.brianna.meditrack.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "dose_logs",
    foreignKeys = @ForeignKey(
        entity = Medication.class,
        parentColumns = "id",
        childColumns = "medicationId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = { @Index("medicationId") }
)
public class DoseLog {

    public static final String STATUS_TAKEN   = "TAKEN";
    public static final String STATUS_MISSED  = "MISSED";
    public static final String STATUS_SKIPPED = "SKIPPED";

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long medicationId;
    private String medicationName;   // denormalized for display convenience
    private long scheduledTime;      // epoch millis — when it was supposed to be taken
    private long takenTime;          // epoch millis — when it was actually taken (0 = not taken)
    private String status;           // TAKEN, MISSED, SKIPPED
    private String dateKey;          // yyyy-MM-dd string for fast date queries

    public DoseLog() {}

    // Getters and setters

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMedicationId() { return medicationId; }
    public void setMedicationId(long medicationId) { this.medicationId = medicationId; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public long getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(long scheduledTime) { this.scheduledTime = scheduledTime; }

    public long getTakenTime() { return takenTime; }
    public void setTakenTime(long takenTime) { this.takenTime = takenTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDateKey() { return dateKey; }
    public void setDateKey(String dateKey) { this.dateKey = dateKey; }

    public boolean isTaken() {
        return STATUS_TAKEN.equals(status);
    }
}
