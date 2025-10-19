package net.craftish37.commonboat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CommonBoat implements ClientModInitializer {
    private static KeyBinding toggleKey;
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> EasterEggFishHighlighter.onWorldRender(context.matrixStack()));
        Sounds.registerSounds();
        ConfigAccess.get();
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.commonboat.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.commonboat"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                CommonBoatConfig cfg = ConfigAccess.get();
                boolean wasEnabled = cfg.enabled;
                cfg.enabled = !cfg.enabled;
                cfg.save();
                if (client.player != null) {
                    if (wasEnabled && cfg.easterEggsEnabled && cfg.handbrakeEnabled) {
                        client.player.playSound(Sounds.EASTER_EGG_DISABLE_SOUND);
                    }
                    String messageKey = cfg.enabled ? "text.commonboat.status.enabled" : "text.commonboat.status.disabled";
                    client.player.sendMessage(Text.translatable(messageKey), true);
                }
            }
        });
    }
}