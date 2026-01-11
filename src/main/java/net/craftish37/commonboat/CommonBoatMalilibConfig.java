package net.craftish37.commonboat;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.config.options.ConfigColor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class CommonBoatMalilibConfig implements IConfigHandler {
    private static final CommonBoatMalilibConfig INSTANCE = new CommonBoatMalilibConfig();
    public static CommonBoatMalilibConfig getInstance() { return INSTANCE; }

    public static class ConfigColorNoComment extends ConfigColor {
        public ConfigColorNoComment(String name, String defaultValue) {
            super(name, defaultValue, "NO_COMMENT_PLACEHOLDER");
        }
        @Override
        public String getComment() { return null; }
    }
    public static class ConfigBooleanNoComment extends ConfigBoolean {
        public ConfigBooleanNoComment(String name, boolean defaultValue) {
            super(name, defaultValue, "NO_COMMENT_PLACEHOLDER");
        }
        @Override
        public String getComment() {
            return null;
        }
    }
    public static class ConfigHotkeyNoComment extends ConfigHotkey {
        public ConfigHotkeyNoComment(String name, String defaultStorageString) {
            super(name, defaultStorageString, "NO_COMMENT_PLACEHOLDER");
        }
        @Override
        public String getComment() {
            return null;
        }
    }
    public static final Map<String, ConfigColor> dynamicFishColorWidgets = new LinkedHashMap<>();
    public static final ConfigBoolean enabled = new ConfigBoolean("text.commonboat.config.enable_mod", false, "text.commonboat.config.enable_mod.tooltip");
    public static final ConfigBoolean disableOnNameMatch = new ConfigBoolean("text.commonboat.config.disable_on_name_match", false, "text.commonboat.config.disable_on_name_match.tooltip");
    public static final ConfigOptionList nameMatchMode = new ConfigOptionList("text.commonboat.config.name_match_mode", CommonBoatConfig.BlackWhiteList.BLACKLIST, "text.commonboat.config.name_match_mode.tooltip");
    public static final ConfigStringList nameMatchList = new ConfigStringList("text.commonboat.config.name_match_string", ImmutableList.of(), "text.commonboat.config.name_match_string.tooltip");

    public static final ConfigBoolean slipperinessEnabled = new ConfigBooleanNoComment("text.commonboat.config.enable_slipperiness", false);
    public static final ConfigBoolean velocityMultiplierEnabled = new ConfigBooleanNoComment("text.commonboat.config.enable_velocity", false);
    public static final ConfigBoolean boatStepHeightEnabled = new ConfigBooleanNoComment("text.commonboat.config.enable_step_height", false);
    public static final ConfigBoolean removeAirDrag = new ConfigBoolean("text.commonboat.config.remove_air_drag", false, "text.commonboat.config.remove_air_drag.tooltip");
    public static final ConfigBoolean boatCameraRotationEnabled = new ConfigBoolean("text.commonboat.config.enable_camera_rotation", false, "text.commonboat.config.enable_camera_rotation.tooltip");
    public static final ConfigBoolean easterEggsEnabled = new ConfigBoolean("text.commonboat.config.enable_easter_eggs", false, "text.commonboat.config.enable_easter_eggs.tooltip");

    public static final ConfigDouble slipperiness = new ConfigDouble("text.commonboat.config.slipperiness_value", 0.989, 0.1, 1.0, "text.commonboat.config.slipperiness_value.tooltip");
    public static final ConfigDouble velocityMultiplier = new ConfigDouble("text.commonboat.config.velocity_value", 1.1, 0.1, 2.0, "text.commonboat.config.velocity_value.tooltip");
    public static final ConfigDouble boatStepHeight = new ConfigDouble("text.commonboat.config.step_height_value", 0.5, 0.0, 5.0, "text.commonboat.config.step_height_value.tooltip");
    public static final ConfigDouble maxSpeed = new ConfigDouble("text.commonboat.config.max_speed", -1.0, -1.0, 220.0, "text.commonboat.config.max_speed.tooltip");
    public static final ConfigStringList customBlockSlipperiness = new ConfigStringList("text.commonboat.config.slipperiness_value", ImmutableList.of(), "text.commonboat.config.slipperiness_value.tooltip");

    public static final ConfigBoolean handbrakeEnabled = new ConfigBooleanNoComment("text.commonboat.config.enable_handbrake", false);
    public static final ConfigBoolean flappyBirdEnabled = new ConfigBooleanNoComment("text.commonboat.config.enable_flappybird", false);
    public static final ConfigBoolean flappyBirdPitchControl = new ConfigBoolean("text.commonboat.config.enable_flappybird_pitch_control", false, "text.commonboat.config.enable_flappybird_pitch_control.tooltip");
    public static final ConfigBoolean leFischeAuChocolatEnabled = new ConfigBooleanNoComment("text.commonboat.config.enable_lefischeauchocolat", false);
    public static final ConfigBoolean elytraBoatEnabled = new ConfigBooleanNoComment("text.commonboat.config.enable_elytraboat", false);
    public static final ConfigBoolean disableBlockBreakingPenalty = new ConfigBooleanNoComment("text.commonboat.config.disable_block_breaking_penalty", false);
    public static final ConfigDouble fishDetectionDistance = new ConfigDouble("text.commonboat.config.fish_detection_distance", 48.0, 0.0, 256.0, "text.commonboat.config.fish_detection_distance.tooltip");

    public static final ConfigStringList capturedFishSheetUrls = new ConfigStringList("text.commonboat.config.captured_fish_sheet_url", ImmutableList.of(), "text.commonboat.config.captured_fish_sheet_url.tooltip");
    public static Map<String, String> capturedFishSheetColors = new HashMap<>();
    public static final ConfigDouble maxJumpHeight = new ConfigDouble("text.commonboat.config.max_jump_height", -1.0, -1.0, 2048.0, "text.commonboat.config.max_jump_height.tooltip");

    public static final ConfigHotkey masterToggleKey = new ConfigHotkey("text.commonboat.config.enable_mod", "G", "text.commonboat.config.enable_mod.tooltip");

    public static final ConfigHotkey slipperinessToggleKey = new ConfigHotkeyNoComment("text.commonboat.config.enable_slipperiness", "");
    public static final ConfigHotkey velocityToggleKey = new ConfigHotkeyNoComment("text.commonboat.config.enable_velocity", "");
    public static final ConfigHotkey stepHeightToggleKey = new ConfigHotkeyNoComment("text.commonboat.config.enable_step_height", "");
    public static final ConfigHotkey airDragToggleKey = new ConfigHotkey("text.commonboat.config.remove_air_drag", "", "text.commonboat.config.remove_air_drag.tooltip");
    public static final ConfigHotkey boatCameraRotationToggleKey = new ConfigHotkeyNoComment("text.commonboat.config.enable_camera_rotation", "");
    public static final ConfigHotkey easterEggsToggleKey = new ConfigHotkey("text.commonboat.config.enable_easter_eggs", "", "text.commonboat.config.enable_easter_eggs.tooltip");

    public static final ConfigHotkey handbrakeToggleKey = new ConfigHotkeyNoComment("text.commonboat.config.enable_handbrake", "");
    public static final ConfigHotkey flappyBirdToggleKey = new ConfigHotkeyNoComment("text.commonboat.config.enable_flappybird", "");
    public static final ConfigHotkey flappyBirdPitchToggleKey = new ConfigHotkey("text.commonboat.config.enable_flappybird_pitch_control", "", "text.commonboat.config.enable_flappybird_pitch_control.tooltip");
    public static final ConfigHotkey leFischeAuChocolatToggleKey = new ConfigHotkeyNoComment("text.commonboat.config.enable_lefischeauchocolat", "");
    public static final ConfigHotkey elytraBoatToggleKey = new ConfigHotkeyNoComment("text.commonboat.config.enable_elytraboat", "");
    public static final ConfigHotkey blockBreakingPenaltyToggleKey = new ConfigHotkeyNoComment("text.commonboat.config.disable_block_breaking_penalty", "");

    public static final List<ConfigHotkey> HOTKEYS = ImmutableList.of(
            masterToggleKey, slipperinessToggleKey, velocityToggleKey, stepHeightToggleKey,
            airDragToggleKey, boatCameraRotationToggleKey, easterEggsToggleKey, handbrakeToggleKey, flappyBirdToggleKey,
            flappyBirdPitchToggleKey, leFischeAuChocolatToggleKey, elytraBoatToggleKey,
            blockBreakingPenaltyToggleKey
    );

    @Override
    public void load() {
        CommonBoatConfig cfg = ConfigAccess.get();
        loadBasicSettings(cfg);
        loadCustomBlockSlipperiness(cfg);
        loadEasterEggSettings(cfg);
        loadFishSheetSettings(cfg);
        loadKeybinds(cfg);
    }
    private void loadBasicSettings(CommonBoatConfig cfg) {
        enabled.setBooleanValue(cfg.enabled);
        disableOnNameMatch.setBooleanValue(cfg.disableOnNameMatch);
        nameMatchMode.setOptionListValue(cfg.nameMatchMode);
        nameMatchList.setStrings(cfg.nameMatchList);
        slipperinessEnabled.setBooleanValue(cfg.slipperinessEnabled);
        velocityMultiplierEnabled.setBooleanValue(cfg.velocityMultiplierEnabled);
        boatStepHeightEnabled.setBooleanValue(cfg.boatStepHeightEnabled);
        removeAirDrag.setBooleanValue(cfg.removeAirDrag);
        boatCameraRotationEnabled.setBooleanValue(cfg.boatCameraRotationEnabled);
        slipperiness.setDoubleValue(cfg.slipperiness);
        velocityMultiplier.setDoubleValue(cfg.velocityMultiplier);
        boatStepHeight.setDoubleValue(cfg.boatStepHeight);
        maxSpeed.setDoubleValue(cfg.maxSpeed);
        maxJumpHeight.setDoubleValue(cfg.maxJumpHeight);
    }
    private void loadCustomBlockSlipperiness(CommonBoatConfig cfg) {
        List<String> blockList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : cfg.customBlockSlipperiness.entrySet()) {
            blockList.add(entry.getKey() + "=" + entry.getValue());
        }
        if (blockList.isEmpty()) blockList.add("minecraft:blue_ice=0.989");
        customBlockSlipperiness.setStrings(blockList);
    }
    private void loadEasterEggSettings(CommonBoatConfig cfg) {
        easterEggsEnabled.setBooleanValue(cfg.easterEggsEnabled);
        handbrakeEnabled.setBooleanValue(cfg.handbrakeEnabled);
        flappyBirdEnabled.setBooleanValue(cfg.flappyBirdEnabled);
        flappyBirdPitchControl.setBooleanValue(cfg.flappyBirdPitchControl);
        leFischeAuChocolatEnabled.setBooleanValue(cfg.leFischeAuChocolatEnabled);
        elytraBoatEnabled.setBooleanValue(cfg.elytraBoatEnabled);
        disableBlockBreakingPenalty.setBooleanValue(cfg.disableBlockBreakingPenalty);
        fishDetectionDistance.setDoubleValue(cfg.fishDetectionDistance);
    }
    public void refreshFishWidgets() {
        List<String> urls = capturedFishSheetUrls.getStrings();
        Map<String, ConfigColor> newWidgets = new LinkedHashMap<>();
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            if (url == null || url.trim().isEmpty()) continue;
            String label = StringUtils.translate("text.commonboat.config.captured_fish_sheet_url_color") + " " + (i + 1);
            String currentValue;
            if (dynamicFishColorWidgets.containsKey(url)) {
                currentValue = dynamicFishColorWidgets.get(url).getStringValue();
            } else {
                currentValue = capturedFishSheetColors.getOrDefault(url, "#FFFFFF");
            }
            ConfigColorNoComment widget = new ConfigColorNoComment(label, currentValue);
            widget.setValueFromString(currentValue);
            newWidgets.put(url, widget);
        }
        dynamicFishColorWidgets.clear();
        dynamicFishColorWidgets.putAll(newWidgets);
    }
    private void loadFishSheetSettings(CommonBoatConfig cfg) {
        capturedFishSheetUrls.setStrings(cfg.capturedFishSheetUrls);
        capturedFishSheetColors = new HashMap<>(cfg.capturedFishSheetColors);
        refreshFishWidgets();
    }
    private void loadKeybinds(CommonBoatConfig cfg) {
        if (cfg.masterToggleKey != null) masterToggleKey.setValueFromJsonElement(cfg.masterToggleKey);
        if (cfg.slipperinessToggleKey != null) slipperinessToggleKey.setValueFromJsonElement(cfg.slipperinessToggleKey);
        if (cfg.velocityToggleKey != null) velocityToggleKey.setValueFromJsonElement(cfg.velocityToggleKey);
        if (cfg.stepHeightToggleKey != null) stepHeightToggleKey.setValueFromJsonElement(cfg.stepHeightToggleKey);
        if (cfg.airDragToggleKey != null) airDragToggleKey.setValueFromJsonElement(cfg.airDragToggleKey);
        if (cfg.boatCameraRotationToggleKey != null) boatCameraRotationToggleKey.setValueFromJsonElement(cfg.boatCameraRotationToggleKey);
        if (cfg.easterEggsToggleKey != null) easterEggsToggleKey.setValueFromJsonElement(cfg.easterEggsToggleKey);
        if (cfg.handbrakeToggleKey != null) handbrakeToggleKey.setValueFromJsonElement(cfg.handbrakeToggleKey);
        if (cfg.flappyBirdToggleKey != null) flappyBirdToggleKey.setValueFromJsonElement(cfg.flappyBirdToggleKey);
        if (cfg.flappyBirdPitchToggleKey != null) flappyBirdPitchToggleKey.setValueFromJsonElement(cfg.flappyBirdPitchToggleKey);
        if (cfg.leFischeAuChocolatToggleKey != null) leFischeAuChocolatToggleKey.setValueFromJsonElement(cfg.leFischeAuChocolatToggleKey);
        if (cfg.elytraBoatToggleKey != null) elytraBoatToggleKey.setValueFromJsonElement(cfg.elytraBoatToggleKey);
        if (cfg.blockBreakingPenaltyToggleKey != null) blockBreakingPenaltyToggleKey.setValueFromJsonElement(cfg.blockBreakingPenaltyToggleKey);
    }

    @Override
    public void save() {
        CommonBoatConfig cfg = ConfigAccess.get();
        cfg.enabled = enabled.getBooleanValue();
        cfg.disableOnNameMatch = disableOnNameMatch.getBooleanValue();
        cfg.nameMatchMode = (CommonBoatConfig.BlackWhiteList) nameMatchMode.getOptionListValue();
        cfg.nameMatchList = new ArrayList<>(nameMatchList.getStrings());
        cfg.slipperinessEnabled = slipperinessEnabled.getBooleanValue();
        cfg.velocityMultiplierEnabled = velocityMultiplierEnabled.getBooleanValue();
        cfg.boatStepHeightEnabled = boatStepHeightEnabled.getBooleanValue();
        cfg.removeAirDrag = removeAirDrag.getBooleanValue();
        cfg.boatCameraRotationEnabled = boatCameraRotationEnabled.getBooleanValue();
        cfg.easterEggsEnabled = easterEggsEnabled.getBooleanValue();
        cfg.slipperiness = slipperiness.getDoubleValue();
        cfg.velocityMultiplier = velocityMultiplier.getDoubleValue();
        cfg.boatStepHeight = boatStepHeight.getDoubleValue();
        cfg.maxSpeed = maxSpeed.getDoubleValue();

        Map<String, Double> newMap = new HashMap<>();
        for (String entry : customBlockSlipperiness.getStrings()) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) continue;
            String[] parts = trimmedEntry.split("=", 2);
            if (parts.length == 2) {
                try {
                    String blockIdString = parts[0].trim();
                    double val = Double.parseDouble(parts[1].trim());
                    if (Identifier.tryParse(blockIdString) != null && Registries.BLOCK.containsId(Identifier.of(blockIdString))) {
                        newMap.put(blockIdString, val);
                    }
                } catch (Exception ignored) {}
            }
        }
        cfg.customBlockSlipperiness = newMap;
        cfg.handbrakeEnabled = handbrakeEnabled.getBooleanValue();
        cfg.flappyBirdEnabled = flappyBirdEnabled.getBooleanValue();
        cfg.flappyBirdPitchControl = flappyBirdPitchControl.getBooleanValue();
        cfg.leFischeAuChocolatEnabled = leFischeAuChocolatEnabled.getBooleanValue();
        cfg.elytraBoatEnabled = elytraBoatEnabled.getBooleanValue();
        cfg.disableBlockBreakingPenalty = disableBlockBreakingPenalty.getBooleanValue();
        cfg.fishDetectionDistance = fishDetectionDistance.getDoubleValue();
        cfg.capturedFishSheetUrls = new ArrayList<>(capturedFishSheetUrls.getStrings());
        for (Map.Entry<String, ConfigColor> entry : dynamicFishColorWidgets.entrySet()) {
            capturedFishSheetColors.put(entry.getKey(), entry.getValue().getStringValue());
        }
        Map<String, String> cleanColorMap = new HashMap<>();
        for (String url : cfg.capturedFishSheetUrls) {
            if (url != null && !url.trim().isEmpty()) {
                cleanColorMap.put(url, capturedFishSheetColors.getOrDefault(url, "#FFFFFF"));
            }
        }
        capturedFishSheetColors = cleanColorMap;
        cfg.capturedFishSheetColors = new HashMap<>(cleanColorMap);

        cfg.maxJumpHeight = maxJumpHeight.getDoubleValue();

        cfg.masterToggleKey = masterToggleKey.getAsJsonElement();
        cfg.slipperinessToggleKey = slipperinessToggleKey.getAsJsonElement();
        cfg.velocityToggleKey = velocityToggleKey.getAsJsonElement();
        cfg.stepHeightToggleKey = stepHeightToggleKey.getAsJsonElement();
        cfg.airDragToggleKey = airDragToggleKey.getAsJsonElement();
        cfg.boatCameraRotationToggleKey = boatCameraRotationToggleKey.getAsJsonElement();
        cfg.easterEggsToggleKey = easterEggsToggleKey.getAsJsonElement();
        cfg.handbrakeToggleKey = handbrakeToggleKey.getAsJsonElement();
        cfg.flappyBirdToggleKey = flappyBirdToggleKey.getAsJsonElement();
        cfg.flappyBirdPitchToggleKey = flappyBirdPitchToggleKey.getAsJsonElement();
        cfg.leFischeAuChocolatToggleKey = leFischeAuChocolatToggleKey.getAsJsonElement();
        cfg.elytraBoatToggleKey = elytraBoatToggleKey.getAsJsonElement();
        cfg.blockBreakingPenaltyToggleKey = blockBreakingPenaltyToggleKey.getAsJsonElement();

        ConfigAccess.save();
    }
}