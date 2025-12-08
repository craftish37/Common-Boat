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
    public boolean enabled = false;

    public boolean slipperinessEnabled = false;
    public boolean velocityMultiplierEnabled = false;
    public boolean boatStepHeightEnabled = false;
    public boolean removeAirDrag = false;

    public double slipperiness = 0.989;
    public double velocityMultiplier = 1.1;
    public double boatStepHeight = 1.0;
    public double maxSpeed = -1.0;
    public double maxJumpHeight = -1.0;
    public double fishDetectionDistance = 48.0;

    public Map<String, Double> customBlockSlipperiness = new HashMap<>();

    public boolean easterEggsEnabled = false;
    public boolean handbrakeEnabled = false;
    public boolean flappyBirdEnabled = false;
    public boolean flappyBirdPitchControl = false;
    public boolean leFischeAuChocolatEnabled = false;
    public boolean elytraBoatEnabled = false;
    public String capturedFishSheetUrl = "";
    public String capturedFishSheetUrl2 = "";
    public String capturedFishSheetUrl2Color = "#000000";

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