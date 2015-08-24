package com.empiricist.fluxwarp.tileentity;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.minecraft.block.BlockDropper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.util.ChatComponentText;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TileEntityDimensionDatabase extends TileEntity{//TileEntityDropper{//} implements IInventory{
    public static int numSlots;
    public ItemStack[] contents;
    public ArrayList<Integer> dimensions;


    public TileEntityDimensionDatabase(){
        super();
        dimensions = new ArrayList<Integer>();
        //numSlots = 27;
        //contents = new ItemStack[getSizeInventory()];
    }

    public void addDimension(int dimension){
        if(!dimensions.contains(dimension)){
            dimensions.add(new Integer(dimension));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        dimensions = Lists.newArrayList(Arrays.asList(ArrayUtils.toObject(tag.getIntArray("DimIDs"))));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setIntArray("DimIDs", Ints.toArray(dimensions));
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

//    @Override
//    public String getInventoryName() {
//        return "Dimension Database";
//    }
//
//    @Override
//    public boolean hasCustomInventoryName() {
//        return false;
//    }

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

//    @Override
//    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
//        return false;
//    }
//

}
