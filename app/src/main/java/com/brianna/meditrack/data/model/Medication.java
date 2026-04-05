package com.brianna.meditrack.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medications")
public class Medication {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String dosage;
    private String frequency;       // ONCE_DAILY, TWICE_DAILY, THREE_TIMES, FOUR_TIMES, AS_NEEDED, WEEKLY
    private String scheduleTimes;   // comma-separated HH:mm strings, e.g. "08:00,20:00"
    private int colorHex;           // stored as ARGB int
    private String category;        // Morning, Afternoon, Evening, As Needed
    private String prescriber;
    private long refillDate;        // epoch millis, 0 if not set
    private int pillsRemaining;
    private int pillsTotal;
    private String notes;
    private boolean active;
    private long createdAt;

    public Medication() {
        this.active = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getScheduleTimes() { return scheduleTimes; }
    public void setScheduleTimes(String scheduleTimes) { this.scheduleTimes = scheduleTimes; }

    public int getColorHex() { return colorHex; }
    public void setColorHex(int colorHex) { this.colorHex = colorHex; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPrescriber() { return prescriber; }
    public void setPrescriber(String prescriber) { this.prescriber = prescriber; }

    public long getRefillDate() { return refillDate; }
    public void setRefillDate(long refillDate) { this.refillDate = refillDate; }

    public int getPillsRemaining() { return pillsRemaining; }
    public void setPillsRemaining(int pillsRemaining) { this.pillsRemaining = pillsRemaining; }

    public int getPillsTotal() { return pillsTotal; }
    public void setPillsTotal(int pillsTotal) { this.pillsTotal = pillsTotal; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getDisplayFrequency() {
        if (frequency == null) return "";
        switch (frequency) {
            case "ONCE_DAILY":    return "Once daily";
            case "TWICE_DAILY":   return "Twice daily";
            case "THREE_TIMES":   return "Three times daily";
            case "FOUR_TIMES":    return "Four times daily";
            case "AS_NEEDED":     return "As needed";
            case "WEEKLY":        return "Weekly";
            default:              return frequency;
        }
    }

    public String[] getTimeArray() {
        if (scheduleTimes == null || scheduleTimes.isEmpty()) return new String[0];
        return scheduleTimes.split(",");
    }
}
