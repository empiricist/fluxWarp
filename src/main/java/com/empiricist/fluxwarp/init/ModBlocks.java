package com.empiricist.fluxwarp.init;

import com.empiricist.fluxwarp.block.BlockBase;
import com.empiricist.fluxwarp.block.BlockDimensionDatabase;
import com.empiricist.fluxwarp.block.BlockTest;
import com.empiricist.fluxwarp.block.BlockWarpCore;
import com.empiricist.fluxwarp.reference.Reference;
import com.empiricist.fluxwarp.tileentity.TileEntityDimensionDatabase;
import com.empiricist.fluxwarp.tileentity.TileEntityWarpCore;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

//this annotation tells forge to preserve this as reference w/o modification (unnecessary, good practice)
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModBlocks {
    public static final BlockBase test = new BlockTest();
    public static final Block warpcore = new BlockWarpCore();
    public static final Block dimensionDatabase = new BlockDimensionDatabase();

    public static void init(){
        GameRegistry.registerBlock( test, "test");

        GameRegistry.registerBlock( warpcore, "warpcore");
        GameRegistry.registerTileEntity(TileEntityWarpCore.class, "warpcore");

        GameRegistry.registerBlock( dimensionDatabase, "dimensionDatabase");
        GameRegistry.registerTileEntity(TileEntityDimensionDatabase.class, "dimensionDatabase");
    }
}
