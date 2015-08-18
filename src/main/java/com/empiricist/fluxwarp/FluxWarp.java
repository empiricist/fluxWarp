package com.empiricist.fluxwarp;


import com.empiricist.fluxwarp.client.handler.KeyInputEventHandler;
import com.empiricist.fluxwarp.handler.ConfigurationHandler;
import com.empiricist.fluxwarp.init.Integration;
import com.empiricist.fluxwarp.init.ModBlocks;
import com.empiricist.fluxwarp.init.ModItems;
import com.empiricist.fluxwarp.init.Recipes;
import com.empiricist.fluxwarp.reference.Reference;
import com.empiricist.fluxwarp.utility.LogHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import com.empiricist.fluxwarp.proxy.IProxy;
import net.minecraftforge.common.ForgeChunkManager;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY_CLASS)
public class FluxWarp {
    @Mod.Instance(Reference.MOD_ID)
    public static FluxWarp instance;//instance of mod

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static IProxy proxy;//holds proxy (ClientProxy on client, ServerProxy on server)

    //Preinit - config, network, items, blocks
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        //handle config file at default location
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());

        //register our class to listen for config events from FML's event bus
        FMLCommonHandler.instance().bus().register(new ConfigurationHandler());

        //register keybindings
        proxy.registerKeyBindings();

        //initialize the mod's items and blocks
        ModItems.init();
        ModBlocks.init();


        LogHelper.info("PreInit Complete");
    }

    //init - gui handler, tileentity, renderers, event handlers, recipes
    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        //register other classes to listen for events from event bus
        FMLCommonHandler.instance().bus().register(new KeyInputEventHandler());

        //register recipes
        Recipes.init();

        ForgeChunkManager.setForcedChunkLoadingCallback(instance, null);

        LogHelper.info("Init Complete");
    }

    //postinit - wrap up after mods initialize (ie compatibility)
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        Integration.postInit();
        LogHelper.info("PostInit Complete");

    }
}
