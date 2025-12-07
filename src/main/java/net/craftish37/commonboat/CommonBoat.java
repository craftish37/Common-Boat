package net.craftish37.commonboat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class CommonBoat implements ClientModInitializer {
    private static KeyBinding masterToggleKey;
    private static KeyBinding slipperinessToggleKey;
    private static KeyBinding velocityToggleKey;
    private static KeyBinding stepHeightToggleKey;
    private static KeyBinding airDragToggleKey;
    private static KeyBinding easterEggsToggleKey;
    private static KeyBinding handbrakeToggleKey;
    private static KeyBinding flappyBirdToggleKey;
    private static KeyBinding flappyBirdPitchToggleKey;
    private static KeyBinding leFischeAuChocolatToggleKey;
    private static KeyBinding elytraBoatToggleKey;
    private static KeyBinding registerToggleKey(String key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                key,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                new KeyBinding.Category(Identifier.of("commonboat", "general"))
        ));
    }
    private void performToggle(MinecraftClient client, CommonBoatConfig cfg, String configKey, boolean newState) {
        cfg.save();
        if (client.player != null) {
            String statusKey = newState ? "text.commonboat.status.enabled" : "text.commonboat.status.disabled";
            Text configName = Text.translatable("text.commonboat.config." + configKey);
            Text message = configName.copy().append(": ").append(Text.translatable(statusKey));

            client.player.sendMessage(message, true);
        }
    }
    @Override
    public void onInitializeClient() {
        Sounds.registerSounds();
        ConfigAccess.get();
        EasterEggFishHighlighter.startUpdater();
        masterToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.commonboat.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                new KeyBinding.Category(Identifier.of("commonboat", "general"))
        ));
        slipperinessToggleKey = registerToggleKey("text.commonboat.config.enable_slipperiness");
        velocityToggleKey = registerToggleKey("text.commonboat.config.enable_velocity");
        stepHeightToggleKey = registerToggleKey("text.commonboat.config.enable_step_height");
        airDragToggleKey = registerToggleKey("text.commonboat.config.remove_air_drag");
        easterEggsToggleKey = registerToggleKey("text.commonboat.config.enable_easter_eggs");
        handbrakeToggleKey = registerToggleKey("text.commonboat.config.enable_handbrake");
        flappyBirdToggleKey = registerToggleKey("text.commonboat.config.enable_flappybird");
        flappyBirdPitchToggleKey = registerToggleKey("text.commonboat.config.enable_flappybird_pitch_control");
        leFischeAuChocolatToggleKey = registerToggleKey("text.commonboat.config.enable_lefischeauchocolat");
        elytraBoatToggleKey = registerToggleKey("text.commonboat.config.enable_elytraboat");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CommonBoatConfig cfg = ConfigAccess.get();
            while (masterToggleKey.wasPressed()) {
                boolean wasEnabled = cfg.enabled;
                cfg.enabled = !cfg.enabled;
                performToggle(client, cfg, "enable_mod", cfg.enabled);
                if (wasEnabled && cfg.easterEggsEnabled && cfg.handbrakeEnabled) {
                    client.player.playSound(Sounds.EASTER_EGG_DISABLE_SOUND);
                }
            }
            if (slipperinessToggleKey.wasPressed()) {
                cfg.slipperinessEnabled = !cfg.slipperinessEnabled;
                performToggle(client, cfg, "enable_slipperiness", cfg.slipperinessEnabled);
            }
            if (velocityToggleKey.wasPressed()) {
                cfg.velocityMultiplierEnabled = !cfg.velocityMultiplierEnabled;
                performToggle(client, cfg, "enable_velocity", cfg.velocityMultiplierEnabled);
            }
            if (stepHeightToggleKey.wasPressed()) {
                cfg.boatStepHeightEnabled = !cfg.boatStepHeightEnabled;
                performToggle(client, cfg, "enable_step_height", cfg.boatStepHeightEnabled);
            }
            if (airDragToggleKey.wasPressed()) {
                cfg.removeAirDrag = !cfg.removeAirDrag;
                performToggle(client, cfg, "remove_air_drag", cfg.removeAirDrag);
            }
            if (easterEggsToggleKey.wasPressed()) {
                cfg.easterEggsEnabled = !cfg.easterEggsEnabled;
                performToggle(client, cfg, "enable_easter_eggs", cfg.easterEggsEnabled);
            }
            if (cfg.easterEggsEnabled) {
                if (handbrakeToggleKey.wasPressed()) {
                    cfg.handbrakeEnabled = !cfg.handbrakeEnabled;
                    performToggle(client, cfg, "enable_handbrake", cfg.handbrakeEnabled);
                }
                if (flappyBirdToggleKey.wasPressed()) {
                    cfg.flappyBirdEnabled = !cfg.flappyBirdEnabled;
                    performToggle(client, cfg, "enable_flappybird", cfg.flappyBirdEnabled);
                }
                if (flappyBirdPitchToggleKey.wasPressed()) {
                    cfg.flappyBirdPitchControl = !cfg.flappyBirdPitchControl;
                    performToggle(client, cfg, "enable_flappybird_pitch_control", cfg.flappyBirdPitchControl);
                }
                if (leFischeAuChocolatToggleKey.wasPressed()) {
                    cfg.leFischeAuChocolatEnabled = !cfg.leFischeAuChocolatEnabled;
                    performToggle(client, cfg, "enable_lefischeauchocolat", cfg.leFischeAuChocolatEnabled);
                }
                if (elytraBoatToggleKey.wasPressed()) {
                    cfg.elytraBoatEnabled = !cfg.elytraBoatEnabled;
                    performToggle(client, cfg, "enable_elytraboat", cfg.elytraBoatEnabled);
                }
            }
        });
    }
}