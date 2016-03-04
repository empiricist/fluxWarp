package com.empiricist.teleflux.init;

import com.empiricist.teleflux.block.BlockBase;
import com.empiricist.teleflux.block.BlockDimensionDatabase;
import com.empiricist.teleflux.block.BlockTest;
import com.empiricist.teleflux.block.BlockWarpCore;
import com.empiricist.teleflux.reference.Reference;
import com.empiricist.teleflux.tileentity.TileEntityWarpCore;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

//this annotation tells forge to preserve this as reference w/o modification (unnecessary, good practice)
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModBlocks {
    public static final BlockBase test = new BlockTest();
    public static final BlockWarpCore warpcore = new BlockWarpCore();
    public static final Block dimensionDatabase = new BlockDimensionDatabase();

    public static void init(){
        GameRegistry.registerBlock( test, test.getName());

        GameRegistry.registerBlock( warpcore, warpcore.getName());
        GameRegistry.registerTileEntity(TileEntityWarpCore.class, warpcore.getName());

//        GameRegistry.registerBlock( dimensionDatabase, "dimensionDatabase");
//        GameRegistry.registerTileEntity(TileEntityDimensionDatabase.class, "dimensionDatabase");
    }
}
