package net.craftish37.commonboat.mixin;

import net.minecraft.entity.vehicle.AbstractBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBoatEntity.class)
public interface AbstractBoatEntityAccessor {
    @Accessor("location")
    AbstractBoatEntity.Location getLocationField();
}