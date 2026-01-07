package net.craftish37.commonboat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.gui.screen.Screen;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommonBoatModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> {
            CommonBoatMalilibConfig.getInstance().load();
            return new CommonBoatConfigScreen(parent);
        };
    }
    public static class CommonBoatConfigScreen extends GuiConfigsBase {
        private enum Tab {
            GENERAL("text.commonboat.config.category.general"),
            VALUES("text.commonboat.config.category.values"),
            EASTER_EGGS("text.commonboat.config.category.eastereggs"),
            HOTKEYS("text.commonboat.config.category.hotkeys");
            public final String translationKey;
            Tab(String key) { this.translationKey = key; }
        }
        private Tab activeTab = Tab.GENERAL;
        private final Map<String, ConfigColor> dynamicColorWidgets = new LinkedHashMap<>();

        public CommonBoatConfigScreen(Screen parent) {
            super(10, 50, StringUtils.translate("text.commonboat.config.tab"), parent, StringUtils.translate("text.commonboat.config.title"));
        }
        @Override
        public void initGui() {
            saveDynamicColors();
            dynamicColorWidgets.clear();
            super.initGui();
            int x = 20;
            int y = 20;
            int buttonWidth = 80;
            for (Tab tab : Tab.values()) {
                ButtonGeneric button = new ButtonGeneric(x, y, buttonWidth, 20, StringUtils.translate(tab.translationKey));
                button.setEnabled(this.activeTab != tab);
                this.addButton(button, (b, mouseButton) -> {
                    this.activeTab = tab;
                    this.initGui();
                });
                x += buttonWidth + 4;
            }
        }
        private void saveDynamicColors() {
            for (Map.Entry<String, ConfigColor> entry : dynamicColorWidgets.entrySet()) {
                CommonBoatMalilibConfig.capturedFishSheetColors.put(entry.getKey(), entry.getValue().getStringValue());
            }
        }
        @Override
        public List<ConfigOptionWrapper> getConfigs() {
            List<IConfigBase> configs = new ArrayList<>();
            switch (this.activeTab) {
                case GENERAL:
                    configs.add(CommonBoatMalilibConfig.enabled);
                    configs.add(CommonBoatMalilibConfig.disableOnNameMatch);
                    configs.add(CommonBoatMalilibConfig.nameMatchMode);
                    configs.add(CommonBoatMalilibConfig.slipperinessEnabled);
                    configs.add(CommonBoatMalilibConfig.velocityMultiplierEnabled);
                    configs.add(CommonBoatMalilibConfig.boatStepHeightEnabled);
                    configs.add(CommonBoatMalilibConfig.removeAirDrag);
                    configs.add(CommonBoatMalilibConfig.easterEggsEnabled);
                    break;
                case VALUES:
                    configs.add(CommonBoatMalilibConfig.nameMatchList);
                    configs.add(CommonBoatMalilibConfig.slipperiness);
                    configs.add(CommonBoatMalilibConfig.velocityMultiplier);
                    configs.add(CommonBoatMalilibConfig.boatStepHeight);
                    configs.add(CommonBoatMalilibConfig.maxSpeed);
                    configs.add(CommonBoatMalilibConfig.customBlockSlipperiness);
                    break;
                case EASTER_EGGS:
                    if (CommonBoatMalilibConfig.easterEggsEnabled.getBooleanValue()) {
                        configs.add(CommonBoatMalilibConfig.handbrakeEnabled);
                        configs.add(CommonBoatMalilibConfig.flappyBirdEnabled);
                        configs.add(CommonBoatMalilibConfig.flappyBirdPitchControl);
                        configs.add(CommonBoatMalilibConfig.leFischeAuChocolatEnabled);
                        configs.add(CommonBoatMalilibConfig.elytraBoatEnabled);
                        configs.add(CommonBoatMalilibConfig.disableBlockBreakingPenalty);
                        configs.add(CommonBoatMalilibConfig.fishDetectionDistance);
                        configs.add(CommonBoatMalilibConfig.capturedFishSheetUrls);

                        List<String> urls = CommonBoatMalilibConfig.capturedFishSheetUrls.getStrings();
                        for (int i = 0; i < urls.size(); i++) {
                            String url = urls.get(i);
                            if (url == null || url.trim().isEmpty()) continue;

                            String label = "Captured Fish Google Sheet URL " + (i + 1);
                            String colorVal = CommonBoatMalilibConfig.capturedFishSheetColors.getOrDefault(url, "#FFFFFF");

                            ConfigColor colorWidget = new ConfigColor(label, colorVal, "");
                            colorWidget.setValueFromString(colorVal);

                            dynamicColorWidgets.put(url, colorWidget);
                            configs.add(colorWidget);
                        }

                        configs.add(CommonBoatMalilibConfig.maxJumpHeight);
                    }
                    break;
                case HOTKEYS:
                    configs.add(CommonBoatMalilibConfig.masterToggleKey);
                    configs.add(CommonBoatMalilibConfig.slipperinessToggleKey);
                    configs.add(CommonBoatMalilibConfig.velocityToggleKey);
                    configs.add(CommonBoatMalilibConfig.stepHeightToggleKey);
                    configs.add(CommonBoatMalilibConfig.airDragToggleKey);
                    configs.add(CommonBoatMalilibConfig.easterEggsToggleKey);
                    if (CommonBoatMalilibConfig.easterEggsEnabled.getBooleanValue()) {
                        configs.add(CommonBoatMalilibConfig.handbrakeToggleKey);
                        configs.add(CommonBoatMalilibConfig.flappyBirdToggleKey);
                        configs.add(CommonBoatMalilibConfig.flappyBirdPitchToggleKey);
                        configs.add(CommonBoatMalilibConfig.leFischeAuChocolatToggleKey);
                        configs.add(CommonBoatMalilibConfig.elytraBoatToggleKey);
                        configs.add(CommonBoatMalilibConfig.blockBreakingPenaltyToggleKey);
                    }
                    break;
            }
            return ConfigOptionWrapper.createFor(configs);
        }
        @Override
        public void removed() {
            saveDynamicColors();
            super.removed();
            CommonBoatMalilibConfig.getInstance().save();
        }
    }
}