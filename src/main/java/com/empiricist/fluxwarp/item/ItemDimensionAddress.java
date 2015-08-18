package com.empiricist.fluxwarp.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import javax.swing.*;
import java.util.List;
import java.util.Random;

public class ItemDimensionAddress extends ItemBase implements IDimensionPermissionItem{

    public IIcon savedAddressIcon;

    public ItemDimensionAddress(){
        super();
        this.setUnlocalizedName("dimensionAddress");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister){
        savedAddressIcon = iconRegister.registerIcon( getUnwrappedUnlocalizedName( this.getUnlocalizedName() )  + "Saved");


        //this assumes file name is same as name
        super.registerIcons(iconRegister);
    }

    @Override
    public boolean canTravelTo( ItemStack stack, int dimensionID ) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.hasTagCompound()) {
            tag = stack.getTagCompound();
        }
        if(tag.hasKey("DimID")){
            return dimensionID == tag.getInteger("DimID");
        }else{
            return false;
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.hasTagCompound()) {
            tag = stack.getTagCompound();
        }
        if(tag.hasKey("DimID")){//sneaking deletes ID, if it exists
            if(player.isSneaking()){
                tag.removeTag("DimID");
            }
        }else{//clicking with blank address saves ID
            tag.setInteger("DimID", world.provider.dimensionId);
            //tag.setInteger("DimID", new Random().nextInt());
        }

        stack.setTagCompound(tag);
        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
        if(stack.hasTagCompound()){
            NBTTagCompound tag = stack.getTagCompound();
            if( tag.hasKey("DimID")){
                int id = tag.getInteger("DimID");
                list.add("DIM: " + (DimensionManager.isDimensionRegistered(id) ? WorldProvider.getProviderForDimension(id).getDimensionName() : "ERROR! Dimension does not exist") );
                list.add("ID : " + id );
            }
        }
        if(GuiScreen.isShiftKeyDown()){
            list.add("USE to save dimension");
            list.add("SNEAK-USE to clear");
        }else{
            list.add("Sneak to inspect");
        }
        super.addInformation(stack, player, list, bool);
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) { //icon in players hand
        if(stack.hasTagCompound()){
            NBTTagCompound tag = stack.getTagCompound();
            if( tag.hasKey("DimID")) {
                return savedAddressIcon;
            }
        }
        return super.getIcon(stack, pass);
    }

    @Override
    public IIcon getIconIndex(ItemStack stack) { //icon in inventories
        if(stack.hasTagCompound()){
            NBTTagCompound tag = stack.getTagCompound();
            if( tag.hasKey("DimID")) {
                return savedAddressIcon;
            }
        }
        return super.getIconIndex(stack);
    }

}
