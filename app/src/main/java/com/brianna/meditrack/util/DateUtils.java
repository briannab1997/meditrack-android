package com.brianna.meditrack.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat DATE_KEY_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final SimpleDateFormat DISPLAY_DATE_FORMAT =
            new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

    private static final SimpleDateFormat DISPLAY_TIME_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.getDefault());

    private static final SimpleDateFormat DAY_LABEL_FORMAT =
            new SimpleDateFormat("EEE", Locale.getDefault());

    public static String todayKey() {
        return DATE_KEY_FORMAT.format(new Date());
    }

    public static String offsetDayKey(int offsetDays) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, offsetDays);
        return DATE_KEY_FORMAT.format(cal.getTime());
    }

    public static String formatDisplayDate(long millis) {
        if (millis <= 0) return "Not set";
        return DISPLAY_DATE_FORMAT.format(new Date(millis));
    }

    public static String formatTime(long millis) {
        if (millis <= 0) return "";
        return DISPLAY_TIME_FORMAT.format(new Date(millis));
    }

    public static String formatDayLabel(String dateKey) {
        try {
            Date date = DATE_KEY_FORMAT.parse(dateKey);
            return date != null ? DAY_LABEL_FORMAT.format(date) : dateKey;
        } catch (Exception e) {
            return dateKey;
        }
    }

    public static long daysUntil(long futureMillis) {
        long now = System.currentTimeMillis();
        if (futureMillis <= now) return 0;
        return (futureMillis - now) / (1000 * 60 * 60 * 24);
    }

    public static long todayAtTime(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }

    public static String formatTimeFromString(String hhmm) {
        try {
            String[] parts = hhmm.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, m);
            return DISPLAY_TIME_FORMAT.format(cal.getTime());
        } catch (Exception e) {
            return hhmm;
        }
    }
}
