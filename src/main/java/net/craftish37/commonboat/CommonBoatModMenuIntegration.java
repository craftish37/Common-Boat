package net.craftish37.commonboat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;

public class CommonBoatModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> {
            CommonBoatConfig cfg = ConfigAccess.get();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(net.minecraft.text.Text.literal("Common-Boat Config"))
                    .setSavingRunnable(ConfigAccess::save);

            ConfigCategory general = builder.getOrCreateCategory(net.minecraft.text.Text.literal("General"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder
                    .startBooleanToggle(net.minecraft.text.Text.literal("Enabled"), cfg.enabled)
                    .setDefaultValue(true)
                    .setTooltip(net.minecraft.text.Text.literal("Toggle to enable or disable Common-Boat entirely"))
                    .setSaveConsumer(v -> cfg.enabled = v)
                    .build());

            general.addEntry(entryBuilder
                    .startDoubleField(net.minecraft.text.Text.literal("Slipperiness"), cfg.slipperiness)
                    .setDefaultValue(0.989)
                    .setMin(0.8)
                    .setMax(1.0)
                    .setTooltip(net.minecraft.text.Text.literal("Adjust slipperiness for boats on ice"))
                    .setSaveConsumer(v -> cfg.slipperiness = v)
                    .build());

            general.addEntry(entryBuilder
                    .startDoubleField(net.minecraft.text.Text.literal("Velocity Multiplier"), cfg.velocityMultiplier)
                    .setDefaultValue(1.1)
                    .setMin(0.1)
                    .setMax(2.0)
                    .setTooltip(net.minecraft.text.Text.literal("Adjust velocity multiplier for boats in water"))
                    .setSaveConsumer(v -> cfg.velocityMultiplier = v)
                    .build());

            general.addEntry(entryBuilder
                    .startDoubleField(net.minecraft.text.Text.literal("Boat Step Height"), cfg.boatStepHeight)
                    .setDefaultValue(1.0)
                    .setMin(0.0)
                    .setMax(2.0)
                    .setTooltip(net.minecraft.text.Text.literal("Client-side only. May cause rubber-banding on servers. Vanilla is 0."))
                    .setSaveConsumer(v -> cfg.boatStepHeight = v)
                    .build());

            return builder.build();
        };
    }
}