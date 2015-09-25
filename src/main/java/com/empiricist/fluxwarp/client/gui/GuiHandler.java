package com.empiricist.fluxwarp.client.gui;

import com.empiricist.fluxwarp.FluxWarp;
import com.empiricist.fluxwarp.tileentity.ContainerDimensionDatabase;
import com.empiricist.fluxwarp.tileentity.TileEntityDimensionDatabase;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    public GuiHandler(){
        //need to register so we can use
        NetworkRegistry.INSTANCE.registerGuiHandler(FluxWarp.instance, this);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch(ID){
            case 0:
                TileEntity te = world.getTileEntity(x, y, z);
                if(te!=null && te instanceof TileEntityDimensionDatabase){
                    return new ContainerDimensionDatabase(player.inventory, (TileEntityDimensionDatabase)te);
                }
                break;
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch(ID){
            case 0://machine
                TileEntity te = world.getTileEntity(x, y, z);
                if(te!=null && te instanceof TileEntityDimensionDatabase){
                    return new GuiDimensionDatabase(player.inventory, (TileEntityDimensionDatabase)te);
                }
                break;
        }
        return null;
    }
}
