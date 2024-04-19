package fr.farmeurimmo.coreskyblock.storage.agriculture;

import fr.farmeurimmo.coreskyblock.purpur.agriculture.AgricultureCycleCrops;

public record AgricultureCycleSeason(int id, long startTime, long endTime) {

    // a season is 5 cycle (0,1,2,3,4) of 5 weeks (0,1,2,3,4) of 7 days

    public int getDay() {
        return (int) ((System.currentTimeMillis() - startTime) / (1000 * 60 * 60 * 24));
    }

    public int getWeek() {
        return getDay() / 7;
    }

    public long getTimeUntilNextWeek() {
        return ((long) (getWeek() + 1) * 7 * 1000 * 60 * 60 * 24) - (System.currentTimeMillis() - startTime);
    }

    public String getCycleWithWeek() {
        return "§7C" + (getWeek() % 5 + 1) + " Semaine " + (getWeek() / 5 + 1);
    }

    public AgricultureCycleCrops getCrop() {
        // for each first week of each cycle, there isn't any crop
        // the getWeek() get the current week of the season between 0 and 4
        if (getWeek() % 5 == 0) return null;
        return AgricultureCycleCrops.values()[getWeek() % 5 - 1];
    }

}
