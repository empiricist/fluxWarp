package com.empiricist.teleflux;


import com.empiricist.teleflux.client.gui.GuiHandler;
import com.empiricist.teleflux.client.handler.KeyInputEventHandler;
import com.empiricist.teleflux.handler.ConfigurationHandler;
import com.empiricist.teleflux.init.Integration;
import com.empiricist.teleflux.init.ModBlocks;
import com.empiricist.teleflux.init.ModItems;
import com.empiricist.teleflux.init.Recipes;
import com.empiricist.teleflux.reference.Reference;
import com.empiricist.teleflux.utility.LogHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.empiricist.teleflux.proxy.IProxy;
import net.minecraftforge.common.ForgeChunkManager;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY_CLASS)
public class TeleFlux {
    @Mod.Instance(Reference.MOD_ID)
    public static TeleFlux instance;//instance of mod

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
        //proxy.registerKeyBindings(); //no key bindings needed

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

        proxy.registerRenders();

        //register recipes
        Recipes.init();

        new GuiHandler();

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
