package net.craftish37.commonboat;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class Sounds {
    public static final Identifier EASTER_EGG_DISABLE_ID = Identifier.of("common-boat:easteregg.disable");
    public static SoundEvent EASTER_EGG_DISABLE_SOUND = SoundEvent.of(EASTER_EGG_DISABLE_ID);
    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, EASTER_EGG_DISABLE_ID, EASTER_EGG_DISABLE_SOUND);
    }
}