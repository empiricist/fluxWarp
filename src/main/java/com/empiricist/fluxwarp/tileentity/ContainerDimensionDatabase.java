package com.empiricist.fluxwarp.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;


public class ContainerDimensionDatabase {//extends ContainerChest{
    private IInventory contents;

//    public ContainerDimensionDatabase(IInventory playerItems, IInventory containerItems) {
//        contents = containerItems;
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
//
//    }



//    @Override
//    public boolean canInteractWith(EntityPlayer player) {
//        return true;
//    }
}
