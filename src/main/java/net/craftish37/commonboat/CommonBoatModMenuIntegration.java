package net.craftish37.commonboat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
public class CommonBoatModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> {
            CommonBoatConfig cfg = ConfigAccess.get();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("text.commonboat.config.title"))
                    .setSavingRunnable(ConfigAccess::save);

            ConfigCategory general = builder.getOrCreateCategory(Text.translatable("text.commonboat.config.category.general"));
            ConfigCategory values = builder.getOrCreateCategory(Text.translatable("text.commonboat.config.category.values"));
            ConfigCategory blockSettings = builder.getOrCreateCategory(Text.translatable("text.commonboat.config.enable_slipperiness"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder
                    .startBooleanToggle(Text.translatable("text.commonboat.config.enable_mod"), cfg.enabled)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("text.commonboat.config.enable_mod.tooltip"))
                    .setSaveConsumer(v -> cfg.enabled = v)
                    .build());

            general.addEntry(entryBuilder
                    .startBooleanToggle(Text.translatable("text.commonboat.config.enable_slipperiness"), cfg.slipperinessEnabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(v -> cfg.slipperinessEnabled = v)
                    .build());
            values.addEntry(entryBuilder
                    .startDoubleField(Text.translatable("text.commonboat.config.slipperiness_value"), cfg.slipperiness)
                    .setDefaultValue(0.989)
                    .setMin(0.1)
                    .setMax(1.0)
                    .setTooltip(Text.translatable("text.commonboat.config.slipperiness_value.tooltip"))
                    .setSaveConsumer(v -> cfg.slipperiness = v)
                    .build());
            List<String> initialStringList = new ArrayList<>();
            for (Entry<String, Double> entry : cfg.customBlockSlipperiness.entrySet()) {
                initialStringList.add(entry.getKey() + "=" + entry.getValue());
            }
            if (initialStringList.isEmpty()) {
                initialStringList.add("minecraft:blue_ice=0.989");
            }
            blockSettings.addEntry(entryBuilder.startStrList(
                            Text.translatable("text.commonboat.config.enable_slipperiness"),
                            initialStringList
                    )
                    .setExpanded(true)
                    .setInsertInFront(true)
                    .setSaveConsumer(strings -> {
                        Map<String, Double> newMap = new HashMap<>();
                        for (String entry : strings) {
                            String trimmedEntry = entry.trim();
                            if (trimmedEntry.isEmpty()) continue;
                            String[] parts = trimmedEntry.split("=", 2);
                            if (parts.length != 2) {
                                throw new RuntimeException("Invalid format: " + trimmedEntry + ". Must be 'block_id=slipperiness'.");
                            }
                            String blockIdString = parts[0].trim();
                            double slipperinessValue;
                            try {
                                slipperinessValue = Double.parseDouble(parts[1].trim());
                            } catch (NumberFormatException e) {
                                throw new RuntimeException("Invalid slipperiness value for block " + blockIdString + ".");
                            }
                            Identifier id = Identifier.tryParse(blockIdString);
                            if (id == null || !Registries.BLOCK.containsId(id)) {
                                throw new RuntimeException("Invalid or unknown block ID: " + blockIdString);
                            }
                            if (slipperinessValue < 0.0 || slipperinessValue > 1.0) {
                                throw new RuntimeException("Slipperiness value for block " + blockIdString + " must be between 0.0 and 1.0");
                            }
                            newMap.put(blockIdString, slipperinessValue);
                        }
                        cfg.customBlockSlipperiness = newMap;
                    })
                    .build());

            general.addEntry(entryBuilder
                    .startBooleanToggle(Text.translatable("text.commonboat.config.enable_velocity"), cfg.velocityMultiplierEnabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(v -> cfg.velocityMultiplierEnabled = v)
                    .build());
            values.addEntry(entryBuilder
                    .startDoubleField(Text.translatable("text.commonboat.config.velocity_value"), cfg.velocityMultiplier)
                    .setDefaultValue(1.1)
                    .setMin(0.1)
                    .setMax(2.0)
                    .setTooltip(Text.translatable("text.commonboat.config.velocity_value.tooltip"))
                    .setSaveConsumer(v -> cfg.velocityMultiplier = v)
                    .build());

            general.addEntry(entryBuilder
                    .startBooleanToggle(Text.translatable("text.commonboat.config.enable_step_height"), cfg.boatStepHeightEnabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(v -> cfg.boatStepHeightEnabled = v)
                    .build());
            values.addEntry(entryBuilder
                    .startDoubleField(Text.translatable("text.commonboat.config.step_height_value"), cfg.boatStepHeight)
                    .setDefaultValue(0.5)
                    .setMin(0.0)
                    .setMax(5.0)
                    .setTooltip(Text.translatable("text.commonboat.config.step_height_value.tooltip"))
                    .setSaveConsumer(v -> cfg.boatStepHeight = v)
                    .build());

            general.addEntry(entryBuilder
                    .startBooleanToggle(Text.translatable("text.commonboat.config.remove_air_drag"), cfg.removeAirDrag)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("text.commonboat.config.remove_air_drag.tooltip"))
                    .setSaveConsumer(v -> cfg.removeAirDrag = v)
                    .build());

            general.addEntry(entryBuilder
                    .startBooleanToggle(Text.translatable("text.commonboat.config.enable_easter_eggs"), cfg.easterEggsEnabled)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("text.commonboat.config.enable_easter_eggs.tooltip"))
                    .setSaveConsumer(v -> cfg.easterEggsEnabled = v)
                    .build());

            values.addEntry(entryBuilder
                    .startDoubleField(Text.translatable("text.commonboat.config.max_speed"), cfg.maxSpeed)
                    .setDefaultValue(-1.0)
                    .setMin(-1.0)
                    .setMax(150.0)
                    .setTooltip(Text.translatable("text.commonboat.config.max_speed.tooltip"))
                    .setSaveConsumer(v -> cfg.maxSpeed = v)
                    .build());
            if (cfg.easterEggsEnabled) {
                ConfigCategory eastereggs = builder.getOrCreateCategory(Text.translatable("text.commonboat.config.category.eastereggs"));
                eastereggs.addEntry(entryBuilder
                        .startBooleanToggle(Text.translatable("text.commonboat.config.enable_handbrake"), cfg.handbrakeEnabled)
                        .setDefaultValue(false)
                        .setSaveConsumer(v -> cfg.handbrakeEnabled = v)
                        .build());
                eastereggs.addEntry(entryBuilder
                        .startBooleanToggle(Text.translatable("text.commonboat.config.enable_flappybird"), cfg.flappyBirdEnabled)
                        .setDefaultValue(false)
                        .setSaveConsumer(v -> cfg.flappyBirdEnabled = v)
                        .build());
                eastereggs.addEntry(entryBuilder
                        .startBooleanToggle(Text.translatable("text.commonboat.config.enable_lefischeauchocolat"), cfg.leFischeAuChocolatEnabled)
                        .setDefaultValue(false)
                        .setSaveConsumer(v -> cfg.leFischeAuChocolatEnabled = v)
                        .build());
                eastereggs.addEntry(entryBuilder
                        .startDoubleField(Text.translatable("text.commonboat.config.max_jump_height"), cfg.maxJumpHeight)
                        .setDefaultValue(-1.0)
                        .setMin(-1.0)
                        .setMax(2048.0)
                        .setTooltip(Text.translatable("text.commonboat.config.max_jump_height.tooltip"))
                        .setSaveConsumer(v -> cfg.maxJumpHeight = v)
                        .build());
            }

            return builder.build();
        };
    }
}