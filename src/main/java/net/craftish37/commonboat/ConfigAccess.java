package net.craftish37.commonboat;

public class ConfigAccess {
    private static final CommonBoatConfig INSTANCE = CommonBoatConfig.load();

    public static CommonBoatConfig get() {
        return INSTANCE;
    }

    public static void save() {
        INSTANCE.save();
    }
}