package com.empiricist.fluxwarp.item;

import com.empiricist.fluxwarp.creativetab.CreativeTabTestProject;
import com.empiricist.fluxwarp.utility.ChatHelper;
import com.empiricist.fluxwarp.utility.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemDebugger extends ItemBase{

    public ItemDebugger(){
        super();
        this.setUnlocalizedName("debugger");
        //this.setCreativeTab(CreativeTabTestProject.TEST_PROJECT_TAB);
    }

    //give data of block right clicked on
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ){
        if( !world.isRemote ){
            ChatHelper.sendText(player, "-----Block-----");
            Block block = world.getBlock(x, y, z);
            ChatHelper.sendText(player, "X:" + x + ", Y:" + y + ", Z:" + z + "; Name: " + block.getLocalizedName() + ", ID: " + block.getUnlocalizedName() + ", Meta: " + world.getBlockMetadata(x, y, z));
            ChatHelper.sendText(player, "Hardness: " + block.getBlockHardness(world,x,y,z) + ", Resistance: " + block.getExplosionResistance(player)*5.0f + ", Mining Level: " + block.getHarvestLevel(world.getBlockMetadata(x,y,z)));
            TileEntity te = world.getTileEntity(x,y,z);
            if( te != null ){
                NBTTagCompound tag = new NBTTagCompound();
                te.writeToNBT(tag);
                ChatHelper.sendText(player, "NBTData:" + tag.toString());
            }
        }
        return true;
    }

    //If right clicked on no block, give data of item in slot 1
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
        if( !world.isRemote ){
            ItemStack slot1 = player.inventory.getStackInSlot(0);
            if (slot1 != null){
                ChatHelper.sendText(player, "-----Item-----");
                ChatHelper.sendText(player, "Item in slot 1 has Display Name: " + slot1.getDisplayName() + ", Unlocalized Name: " + slot1.getItem().getUnlocalizedName());
                if( slot1.hasTagCompound() ){
                    ChatHelper.sendText(player, "NBT Data is :"  + slot1.getTagCompound().toString());
                }
            }
        }
        return stack;
    }

    //give data of entity left clicked on
    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity){
        if( !entity.worldObj.isRemote ){
            ChatHelper.sendText(player, "-----Entity-----");
            ChatHelper.sendText(player, "Name: " + entity.getClass()  + ", ID: " + entity.getEntityId() + ", Data: " + entity.toString());
        }
        return true;//do no damage
    }
}
