package com.empiricist.fluxwarp.init;


import com.empiricist.fluxwarp.block.BlockWarpCore;
import com.empiricist.fluxwarp.utility.LogHelper;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.block.Block;

public class Integration {

    public static void postInit(){
        LogHelper.info("Doing FluxWarp integration");
        computercraft();
        Recipes.postInit();//mod item recipes
    }


    public static void computercraft(){
        if( Loader.isModLoaded("ComputerCraft") ){
            Block core = ModBlocks.warpcore;
            if( core instanceof IPeripheralProvider ){
                LogHelper.info("Registering Warp Core as peripheral provider");
                ComputerCraftAPI.registerPeripheralProvider( (IPeripheralProvider) core);
            }else{
                LogHelper.info("Warp Core is not a peripheral provider");
            }
        }else{
            LogHelper.info("ComputerCraft is not loaded, skipping ComputerCraft integration");
        }
    }
}
