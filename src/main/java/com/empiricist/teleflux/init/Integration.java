package com.empiricist.teleflux.init;


import com.empiricist.teleflux.utility.InfoDiskOCDriver;
import com.empiricist.teleflux.utility.LogHelper;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.SidedBlock;
import li.cil.oc.api.prefab.DriverItem;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.Optional;

public class Integration {

    public static void init(){
        if( Loader.isModLoaded("OpenComputers") ){
            opencomputers();
        }else {
            LogHelper.info("OpenComputers is not loaded, skipping OpenComputers integration");
        }
    }
    public static void postInit(){
        computercraft();
        Recipes.postInit();//mod item recipes
    }

    @Optional.Method(modid="OpenComputers")
    private static void opencomputers() {
        LogHelper.info("Registering TeleFlux OC disk driver");
        Driver.add(new InfoDiskOCDriver());//((DriverItem) driver);//register the driver to make disk work in disk drive );
    }


    public static void computercraft(){
        LogHelper.info("Doing TeleFlux CC integration");
        if( Loader.isModLoaded("ComputerCraft") ){
            Block core = ModBlocks.warpcore;
            if( core instanceof IPeripheralProvider){
                LogHelper.info("    Registering Warp Core as peripheral provider");
                ComputerCraftAPI.registerPeripheralProvider( (IPeripheralProvider) core);
            }else{
                LogHelper.info("    Warp Core is not a peripheral provider");
            }
        }else{
            LogHelper.info("    ComputerCraft is not loaded, skipping ComputerCraft integration");
        }
    }
}
