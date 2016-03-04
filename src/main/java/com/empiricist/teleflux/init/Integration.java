package com.empiricist.teleflux.init;


import com.empiricist.teleflux.utility.LogHelper;
import net.minecraftforge.fml.common.Loader;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.block.Block;

public class Integration {

    public static void postInit(){
        LogHelper.info("Doing TeleFlux integration");
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
