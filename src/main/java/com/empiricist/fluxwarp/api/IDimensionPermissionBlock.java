package com.empiricist.fluxwarp.api;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IDimensionPermissionBlock {
    public abstract boolean canTravelTo( World world, int x, int y, int z, int dimensionID);
}
