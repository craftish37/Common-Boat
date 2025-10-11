package net.craftish37.commonboat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CommonBoatConfig {
    public boolean enabled = true;
    public double slipperiness = 0.989;
    public double velocityMultiplier = 1.1;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/commonboat.json");

    public static CommonBoatConfig load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                return GSON.fromJson(reader, CommonBoatConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new CommonBoatConfig();
    }

    public void save() {
        FILE.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}