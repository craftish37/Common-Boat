package net.craftish37.commonboat;

import net.fabricmc.api.ModInitializer;

public class CommonBoat implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigAccess.reload();
    }
}