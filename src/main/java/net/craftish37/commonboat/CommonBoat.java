package net.craftish37.commonboat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CommonBoat implements ClientModInitializer {
    private static KeyBinding toggleKey;
    @Override
    public void onInitializeClient() {
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
                cfg.enabled = !cfg.enabled;
                cfg.save();
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("Common Boat " + (cfg.enabled ? "Enabled" : "Disabled")),
                            true
                    );
                }
            }
        });
    }
}