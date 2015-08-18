package com.empiricist.fluxwarp.item;


import net.minecraft.item.ItemStack;

public interface IDimensionPermissionItem {

    //does this ItemStack give permission to go to this dimension?
    public abstract boolean canTravelTo( ItemStack stack, int dimensionID);
}
