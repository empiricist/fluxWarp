package com.empiricist.fluxwarp.item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import java.util.List;

public class ItemCreativeAddress extends ItemBase implements IDimensionPermissionItem{

    public ItemCreativeAddress(){
        super();
        this.setUnlocalizedName("creativeAddress");
    }

    @Override
    public boolean canTravelTo(ItemStack stack, int dimensionID) {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
        list.add("DIM: All of them");
        list.add("ID : All of them");
    }
}
