package com.empiricist.fluxwarp.tileentity;

import net.minecraft.block.BlockDropper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDropper;

public class TileEntityDimensionDatabase extends TileEntityDropper{//} implements IInventory{
    public static int numSlots;
    public ItemStack[] contents;

    public TileEntityDimensionDatabase(){
        super();

        //numSlots = 27;
        //contents = new ItemStack[getSizeInventory()];
    }

//    @Override
//    public int getSizeInventory() {
//        return numSlots;
//    }

//    @Override
//    public ItemStack getStackInSlot(int slot) {
//        return contents[slot];
//    }
//
//    @Override
//    public ItemStack decrStackSize(int slot, int amount) {
//        return null;
//    }
//
//    @Override
//    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
//        return null;
//    }
//
//    @Override
//    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {
//
//    }

    @Override
    public String getInventoryName() {
        return "Dimension Database";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

//    @Override
//    public int getInventoryStackLimit() {
//        return 0;
//    }
//
//    @Override
//    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
//        return false;
//    }
//
//    @Override
//    public void openInventory() {
//
//    }
//
//    @Override
//    public void closeInventory() {
//
//    }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return false;
    }


}
