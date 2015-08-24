package com.empiricist.fluxwarp.item;

import com.empiricist.fluxwarp.creativetab.CreativeTabTestProject;
import com.empiricist.fluxwarp.utility.LogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
            player.addChatMessage(new ChatComponentText("X:" + x + ", Y:" + y + ", Z:" + z + "; Name:" + world.getBlock(x, y, z).getLocalizedName() + ", ID:" + world.getBlock(x, y, z).getUnlocalizedName() + ", Meta:" + world.getBlockMetadata(x, y, z)));
            TileEntity te = world.getTileEntity(x,y,z);
            if( te != null ){
                NBTTagCompound tag = new NBTTagCompound();
                te.writeToNBT(tag);
                player.addChatMessage( new ChatComponentText("NBTData:" + tag.toString()) );
            }
        }
        return true;
    }

    //give data of entity left clicked on
    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity){
        if( !entity.worldObj.isRemote ){
            player.addChatMessage(new ChatComponentText("Name:" + entity.getClass()  + ", ID:" + entity.getEntityId() + ", Data:" + entity.toString()));
        }
        return true;//do no damage
    }
}
