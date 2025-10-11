package net.craftish37.commonboat;

public class ConfigAccess {
    private static CommonBoatConfig INSTANCE = CommonBoatConfig.load();

    public static CommonBoatConfig get() {
        return INSTANCE;
    }

    public static void save() {
        INSTANCE.save();
    }

    public static void reload() {
        INSTANCE = CommonBoatConfig.load();
    }
}