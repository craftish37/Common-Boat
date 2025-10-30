package net.craftish37.commonboat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommonBoatConfig {
    public boolean enabled = true;

    public boolean slipperinessEnabled = true;
    public boolean velocityMultiplierEnabled = true;
    public boolean boatStepHeightEnabled = true;
    public boolean removeAirDrag = true;

    public double slipperiness = 0.989;
    public double velocityMultiplier = 1.1;
    public double boatStepHeight = 1.0;
    public double maxSpeed = -1.0;
    public double maxJumpHeight = -1.0;

    public Map<String, Double> customBlockSlipperiness = new HashMap<>();

    public boolean easterEggsEnabled = true;
    public boolean handbrakeEnabled = true;
    public boolean flappyBirdEnabled = true;
    public boolean leFischeAuChocolatEnabled = true;

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