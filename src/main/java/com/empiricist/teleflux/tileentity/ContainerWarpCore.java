package com.empiricist.teleflux.tileentity;

import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;


public class ContainerWarpCore extends Container {
    private TileEntityWarpCore contents;
    private int lastRF;

    public ContainerWarpCore(IInventory playerItems, TileEntityWarpCore te) {
        contents = te;
        //addSlotToContainer(new Slot(contents, 0, 1, 5));//num,x,y
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
    public ItemStack slotClick(int slot, int drag, ClickType type, EntityPlayer player) {
        //LogHelper.info("Clicked slot " + slot + " with button " + button + " modifier " + modifier + " side " + player.worldObj.isRemote);
        return super.slotClick(slot, drag, type, player);
    }

    //this is a little bit ridiculous
    //but if it will save me from dealing with packets, so be it
    @Override
    public boolean enchantItem(EntityPlayer player, int e){
        int entry = e & 15;
        int value = e >> 4;
        //LogHelper.info("Code " + e);
        //LogHelper.info("Entry " + entry + ", value " + value);
        switch(entry) {
            case (0):
                contents.setDx(value);
                break;
            case (1):
                contents.setDy(value);
                break;
            case (2):
                contents.setDz(value);
                break;
            case (3):
                contents.setDestDim(value);
                break;
            case (4): //warp button
                contents.setDoWarp(true);
                contents.tryWarp();
                //contents.getWorld().scheduleUpdate(contents.getPos(), contents.getBlockType(), 1);
                break;
            case (5):
                contents.setXPlus(value);
                break;
            case (6):
                contents.setXMinus(value);
                break;
            case (7):
                contents.setYPlus(value);
                break;
            case (8):
                contents.setYMinus(value);
                break;
            case (9):
                contents.setZPlus(value);
                break;
            case (10):
                contents.setZMinus(value);
                break;
        }
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        //contents.markDirty();
        //contents.onDataPacket(Minecraft.getMinecraft().getNetHandler().getNetworkManager(), (S35PacketUpdateTileEntity)contents.getDescriptionPacket());
        contents.getWorld().notifyBlockUpdate(contents.getPos(), contents.getWorld().getBlockState(contents.getPos()), contents.getWorld().getBlockState(contents.getPos()), 3);//markBlockForUpdate(contents.getPos());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        int rf = this.contents.getEnergyStored(EnumFacing.DOWN);

        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener iListener = (IContainerListener)this.listeners.get(i);

            if (this.lastRF != rf){
                iListener.sendProgressBarUpdate(this, 0, rf);
            }
        }

        this.lastRF = rf;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value){
        if (id == 0){
            this.contents.energyStorage.setEnergyStored(value);
        }
    }
}
