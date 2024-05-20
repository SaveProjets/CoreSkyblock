package fr.farmeurimmo.coreskyblock.purpur.agriculture;

public enum AgricultureCycleCrops {

    MANA(1, "Plante de mana"),
    SOMETHING_1(2, "Plante 1"),
    SOMETHING_2(3, "Plante 2"),
    SOMETHING_3(4, "Plante 3");

    private final int week;
    private final String name;

    AgricultureCycleCrops(int week, String name) {
        this.week = week;
        this.name = name;
    }

    public int getWeek() {
        return week;
    }

    public String getName() {
        return name;
    }
}
