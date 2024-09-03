package fr.farmeurimmo.coreskyblock.purpur.pvp;

import java.time.DayOfWeek;

public class PvpEvent {

    private final PvpEventType type;
    private final long duration;
    private final DayOfWeek dayOfWeek;
    private final int hour;
    private final int minute;
    private long startTime = -1;

    public PvpEvent(PvpEventType type, long duration, DayOfWeek dayOfWeek, int hour, int minute) {
        this.type = type;
        this.duration = duration;
        this.dayOfWeek = dayOfWeek;
        this.hour = hour;
        this.minute = minute;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public PvpEventType getType() {
        return type;
    }

    public long getDuration() {
        return duration;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public int getAtHour() {
        return hour;
    }

    public int getAtMinute() {
        return minute;
    }
}
