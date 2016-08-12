package com.empiricist.teleflux.init;

import com.empiricist.teleflux.block.BlockBase;
import com.empiricist.teleflux.block.BlockTest;
import com.empiricist.teleflux.block.BlockWarpCore;
import com.empiricist.teleflux.reference.Reference;
import com.empiricist.teleflux.tileentity.TileEntityWarpCore;
import com.empiricist.teleflux.utility.ParseHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

//this annotation tells forge to preserve this as reference w/o modification (unnecessary, good practice)
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModBlocks {
    public static final BlockBase test = new BlockTest();
    public static final BlockWarpCore warpcore = new BlockWarpCore();
    //public static final Block dimensionDatabase = new BlockDimensionDatabase();

    public static void init(){
        test.setRegistryName(Reference.MOD_ID, test.getName());
        GameRegistry.register( test );
        GameRegistry.register( new ItemBlock(test), new ResourceLocation(Reference.MOD_ID + ":" + test.getName()));

        warpcore.setRegistryName(Reference.MOD_ID, warpcore.getName());
        GameRegistry.register( warpcore );
        GameRegistry.register( new ItemBlock(warpcore), new ResourceLocation(Reference.MOD_ID + ":" + warpcore.getName()));
        GameRegistry.registerTileEntity(TileEntityWarpCore.class, warpcore.getName());

//        GameRegistry.registerBlock( dimensionDatabase, "dimensionDatabase");
//        GameRegistry.registerTileEntity(TileEntityDimensionDatabase.class, "dimensionDatabase");
    }
}
