package com.empiricist.teleflux.api;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IDimensionPermissionBlock {
    public abstract boolean canTravelTo(World world, BlockPos pos, int dimensionID);
}
