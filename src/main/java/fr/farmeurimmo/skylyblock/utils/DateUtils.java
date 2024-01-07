package fr.farmeurimmo.skylyblock.utils;

import java.text.SimpleDateFormat;

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
        int seconds = (int) (millis / 1000);
        int milliseconds = (int) (millis % 1000);
        //don't display a 0 before the seconds if there is no hours
        //I want something like this: 1.34s
        return seconds + "." + milliseconds;
    }
}
