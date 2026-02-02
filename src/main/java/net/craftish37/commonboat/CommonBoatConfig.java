package net.craftish37.commonboat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonBoatConfig {
    public enum BlackWhiteList implements IConfigOptionListEntry {
        BLACKLIST("blacklist", "text.commonboat.config.namematch.blacklist"),
        WHITELIST("whitelist", "text.commonboat.config.namematch.whitelist");
        private final String configString;
        private final String translationKey;
        BlackWhiteList(String configString, String translationKey) {
            this.configString = configString;
            this.translationKey = translationKey;
        }
        @Override
        public String getStringValue() { return this.configString; }
        @Override
        public String getDisplayName() { return StringUtils.translate(this.translationKey); }
        @Override
        public IConfigOptionListEntry cycle(boolean forward) { return this == BLACKLIST ? WHITELIST : BLACKLIST; }
        @Override
        public IConfigOptionListEntry fromString(String value) {
            for (BlackWhiteList entry : values()) {
                if (entry.configString.equalsIgnoreCase(value)) return entry;
            }
            return BLACKLIST;
        }
    }

    public enum FishSortingMode implements IConfigOptionListEntry {
        SHAPE_FIRST("shape_first", "text.commonboat.config.fish_sorting.shape_first"),
        COLOR_FIRST("color_first", "text.commonboat.config.fish_sorting.color_first");
        private final String configString;
        private final String translationKey;
        FishSortingMode(String configString, String translationKey) {
            this.configString = configString;
            this.translationKey = translationKey;
        }
        @Override
        public String getStringValue() { return this.configString; }
        @Override
        public String getDisplayName() { return StringUtils.translate(this.translationKey); }
        @Override
        public IConfigOptionListEntry cycle(boolean forward) { return this == SHAPE_FIRST ? COLOR_FIRST : SHAPE_FIRST; }
        @Override
        public IConfigOptionListEntry fromString(String value) {
            return "color_first".equalsIgnoreCase(value) ? COLOR_FIRST : SHAPE_FIRST;
        }
    }

    public boolean enabled = false;
    public boolean disableOnNameMatch = false;
    public BlackWhiteList nameMatchMode = BlackWhiteList.BLACKLIST;
    public List<String> nameMatchList = new ArrayList<>();

    public boolean slipperinessEnabled = false;
    public boolean velocityMultiplierEnabled = false;
    public boolean boatStepHeightEnabled = false;
    public boolean removeAirDrag = false;
    public boolean boatCameraRotationEnabled = false;
    public boolean easterEggsEnabled = false;

    public double slipperiness = 0.989;
    public double velocityMultiplier = 1.1;
    public double boatStepHeight = 1.0;
    public double maxSpeed = -1.0;
    public Map<String, Double> customBlockSlipperiness = new HashMap<>();

    public boolean handbrakeEnabled = false;
    public boolean flappyBirdEnabled = false;
    public boolean flappyBirdPitchControl = false;
    public boolean leFischeAuChocolatEnabled = false;
    public boolean elytraBoatEnabled = false;
    public boolean disableBlockBreakingPenalty = false;
    public boolean customItemScrolling = false;
    public FishSortingMode fishSortingMode = FishSortingMode.COLOR_FIRST;
    public double fishDetectionDistance = 48.0;

    public List<String> capturedFishSheetUrls = new ArrayList<>();
    public Map<String, String> capturedFishSheetColors = new HashMap<>();
    public double maxJumpHeight = -1.0;

    public JsonElement masterToggleKey = new JsonPrimitive("G");
    public JsonElement slipperinessToggleKey = new JsonPrimitive("");
    public JsonElement velocityToggleKey = new JsonPrimitive("");
    public JsonElement stepHeightToggleKey = new JsonPrimitive("");
    public JsonElement airDragToggleKey = new JsonPrimitive("");
    public JsonElement boatCameraRotationToggleKey = new JsonPrimitive("");
    public JsonElement easterEggsToggleKey = new JsonPrimitive("");
    public JsonElement handbrakeToggleKey = new JsonPrimitive("");
    public JsonElement flappyBirdToggleKey = new JsonPrimitive("");
    public JsonElement flappyBirdPitchToggleKey = new JsonPrimitive("");
    public JsonElement leFischeAuChocolatToggleKey = new JsonPrimitive("");
    public JsonElement elytraBoatToggleKey = new JsonPrimitive("");
    public JsonElement blockBreakingPenaltyToggleKey = new JsonPrimitive("");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final File FILE = new File("config/commonboat.json");
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonBoatConfig.class);

    public static CommonBoatConfig load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                return GSON.fromJson(reader, CommonBoatConfig.class);
            } catch (IOException e) {
                LOGGER.error("Failed to load CommonBoat config", e);
            }
        }
        return new CommonBoatConfig();
    }
    public void save() {
        File parent = FILE.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            LOGGER.warn("Failed to create config directory: {}", parent.getAbsolutePath());
        }
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save CommonBoat config", e);
        }
    }
}