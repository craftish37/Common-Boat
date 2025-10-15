package net.craftish37.commonboat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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
            general.addEntry(entryBuilder
                    .startDoubleField(Text.translatable("text.commonboat.config.slipperiness_value"), cfg.slipperiness)
                    .setDefaultValue(0.989)
                    .setMin(0.1)
                    .setMax(1.0)
                    .setTooltip(Text.translatable("text.commonboat.config.slipperiness_value.tooltip"))
                    .setSaveConsumer(v -> cfg.slipperiness = v)
                    .build());

            general.addEntry(entryBuilder
                    .startBooleanToggle(Text.translatable("text.commonboat.config.enable_velocity"), cfg.velocityMultiplierEnabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(v -> cfg.velocityMultiplierEnabled = v)
                    .build());
            general.addEntry(entryBuilder
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
            general.addEntry(entryBuilder
                    .startDoubleField(Text.translatable("text.commonboat.config.step_height_value"), cfg.boatStepHeight)
                    .setDefaultValue(0.5)
                    .setMin(0.0)
                    .setMax(5.0)
                    .setTooltip(Text.translatable("text.commonboat.config.step_height_value.tooltip"))
                    .setSaveConsumer(v -> cfg.boatStepHeight = v)
                    .build());

            general.addEntry(entryBuilder
                    .startBooleanToggle(Text.translatable("text.commonboat.config.enable_easter_eggs"), cfg.easterEggsEnabled)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("text.commonboat.config.enable_easter_eggs.tooltip"))
                    .setSaveConsumer(v -> cfg.easterEggsEnabled = v)
                    .build());

            return builder.build();
        };
    }
}