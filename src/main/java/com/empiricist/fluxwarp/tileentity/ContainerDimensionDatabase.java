package com.empiricist.fluxwarp.tileentity;

import com.empiricist.fluxwarp.utility.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;


public class ContainerDimensionDatabase extends Container{
    private IInventory contents;

    public ContainerDimensionDatabase(IInventory playerItems, TileEntityDimensionDatabase te) {
        contents = te;
        addSlotToContainer(new Slot(contents, 0, 1, 5));//num,x,y
//        for (int r = 0; r < 9; r++) {
//            for (int c = 0; c < 3; c++) {
//                addSlotToContainer(new Slot(contents, c + r * 9, 12 + c * 18, 8 + r * 18));
//            }
//        }
//
//        int leftCol = (69 - 162) / 2 + 1;
//
//        for (int playerRow = 0; playerRow < 9; playerRow++){
//            for (int playerCol = 0; playerCol < 9; playerCol++) {
//                addSlotToContainer(new Slot(playerItems, playerCol + playerRow * 9 + 9, leftCol + playerCol * 18, 69 - (4 - playerRow) * 18 - 10));
//            }
//            for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++){
//                addSlotToContainer(new Slot(playerItems, hotbarSlot, leftCol + hotbarSlot * 18, 69 - 24));
//            }
//        }

    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public ItemStack slotClick(int slot, int button, int modifier, EntityPlayer player) {
        LogHelper.info("Clicked slot " + slot + " with button " + button + " modifier " + modifier + " side " + player.worldObj.isRemote);
        return super.slotClick(slot, button, modifier, player);
    }

    //this is a little bit ridiculous
    //but if it will save me from dealing with packets, so be it
    @Override
    public boolean enchantItem(EntityPlayer player, int e){
        LogHelper.info("Clicked coords " + e);
        return true;
    }
}
