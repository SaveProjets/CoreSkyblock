package fr.farmeurimmo.coreskyblock.purpur.pvp;

import fr.farmeurimmo.coreskyblock.utils.DateUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PvpPlanningManager {

    public static PvpPlanningManager INSTANCE;
    private final ArrayList<PvpEvent> scheduledEvents = new ArrayList<>();
    public PvpEvent currentEvent;

    public PvpPlanningManager() {
        INSTANCE = this;

        scheduledEvents.add(new PvpEvent(PvpEventType.KOTH, 60 * 1_000 * 10, DayOfWeek.MONDAY, 21, 30));
        scheduledEvents.add(new PvpEvent(PvpEventType.TOTEM, 60 * 1_000 * 10, DayOfWeek.TUESDAY, 21, 30));
        scheduledEvents.add(new PvpEvent(PvpEventType.KOTH, 60 * 1_000 * 10, DayOfWeek.WEDNESDAY, 21, 30));
        scheduledEvents.add(new PvpEvent(PvpEventType.NEXUS, 60 * 1_000 * 30, DayOfWeek.THURSDAY, 21, 30));
        scheduledEvents.add(new PvpEvent(PvpEventType.TOTEM, 60 * 1_000 * 10, DayOfWeek.FRIDAY, 21, 30));
        scheduledEvents.add(new PvpEvent(PvpEventType.KOTH, 60 * 1_000 * 10, DayOfWeek.SATURDAY, 21, 30));
        scheduledEvents.add(new PvpEvent(PvpEventType.BOSS, 60 * 1_000 * 30, DayOfWeek.SUNDAY, 21, 30));
    }

    public PvpEvent getNextEvent() {
        LocalDateTime now = LocalDateTime.now();
        for (PvpEvent event : scheduledEvents) {
            if (event.getDayOfWeek() == now.getDayOfWeek() && event.getAtHour() > now.getHour()) {
                return event;
            }
        }
        return null;
    }

    public long getTimeUntilNextEvent() {
        PvpEvent nextEvent = getNextEvent();
        if (nextEvent == null) {
            return -1;
        }
        LocalDateTime now = LocalDateTime.now();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, nextEvent.getAtHour());
        calendar.set(Calendar.MINUTE, nextEvent.getAtMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date eventDate = calendar.getTime();
        return eventDate.getTime() - now.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public String getNextInString() {
        long time = getTimeUntilNextEvent();
        if (time == -1) {
            return "Aucun événement prévu";
        }
        return DateUtils.getFormattedTimeLeft2(time);
    }

    public String getTimeLeftForCurrentEvent() {
        if (currentEvent == null) {
            return "Aucun événement en cours";
        }
        if (currentEvent.getStartTime() == -1) {
            return "Erreur";
        }
        long time = currentEvent.getDuration() - (System.currentTimeMillis() - currentEvent.getStartTime());
        return (time / 3_600_000) + ":" + ((time % 3_600_000) / 60_000) + ":" + ((time % 60_000) / 1_000);
    }
}
