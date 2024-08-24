package fr.farmeurimmo.coreskyblock.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

    public static String getFormattedDate(long date) {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    public static String getFormattedTime(long time) {
        return new SimpleDateFormat("HH:mm:ss").format(time);
    }

    public static String getFormattedTimeLeft(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String getFormattedTimeLeft2(long millis) {
        return getFormattedTimeLeft((int) (millis / 1000));
    }

    public static String expireAt00() {
        Calendar now = Calendar.getInstance();
        Calendar nextMidnight = (Calendar) now.clone();
        nextMidnight.add(Calendar.DAY_OF_YEAR, 1);
        nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextMidnight.set(Calendar.MINUTE, 0);
        nextMidnight.set(Calendar.SECOND, 0);
        nextMidnight.set(Calendar.MILLISECOND, 0);

        long millisUntilMidnight = nextMidnight.getTimeInMillis() - now.getTimeInMillis();
        int seconds = (int) (millisUntilMidnight / 1000);

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
