package com.empiricist.fluxwarp.handler;

import com.empiricist.fluxwarp.reference.Reference;
import com.google.common.collect.Lists;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

//uses the new (~1.7.10) in-game config GUI
public class ConfigurationHandler {

    public static Configuration configuration;//we will want this in other methods too
    //public static boolean testValue = false;
    public static ArrayList<String> AlwaysAllowedDimensions;

    public static int coreEnergyStorage = 1000000;
    public static int baseCost = 50;
    public static int blockCost = 10;
    public static int entityCost = 10;
    public static int dimensionCost = 50;
    public static int distanceCost = 1;

    public static boolean vanillaRecipes = false;
    public static boolean thermalRecipes = true;
    public static boolean eioRecipes = false;
    public static boolean ic2Recipes = false;
    public static boolean exutRecipes = false;

    public static void init(File configFile) {//initialize configuration
        if (configuration == null) {
            configuration = new Configuration(configFile);
            loadConfiguration();
        }
    }
/*
    public static void laterMethod(){//read in values from file
        try {//because file stuff
            //load config and values from config
            configuration.load();
            boolean testValue = configuration.get(Configuration.CATEGORY_GENERAL, "testValue", true, "Comment").getBoolean();

        }catch(Exception e){//should be more specific
            //log problem
            System.out.println("Exception reading from config file");

        }finally{
            if( configuration.hasChanged() ) {
                configuration.save();
            }
        }
    }
*/
    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event){
        if( event.modID.equalsIgnoreCase(Reference.MOD_ID) ){//if it is this mod
            loadConfiguration();//resync for new value
        }
    }

    private static void loadConfiguration(){
        //testValue = configuration.getBoolean("testValue", Configuration.CATEGORY_GENERAL, false, "Comment");

        String[] dimIDs = configuration.getStringList("AlwaysAllowedDimensions", Configuration.CATEGORY_GENERAL, new String[]{"0"}, "These dimension IDs will not require an address to teleport to");
        AlwaysAllowedDimensions = Lists.newArrayList(Arrays.asList(dimIDs));

        coreEnergyStorage = configuration.getInt("coreEnergyStorage", Configuration.CATEGORY_GENERAL, 1000000, 0, Integer.MAX_VALUE, "Maximum energy capacity of warp core");
        baseCost = configuration.getInt("baseCost", Configuration.CATEGORY_GENERAL, 50, 0, Integer.MAX_VALUE, "Cost to initiate warp");
        blockCost = configuration.getInt("blockCost", Configuration.CATEGORY_GENERAL, 10, 0, Integer.MAX_VALUE, "Cost to warp each block");
        entityCost = configuration.getInt("entityCost", Configuration.CATEGORY_GENERAL, 10, 0, Integer.MAX_VALUE, "Cost to warp each entity");
        dimensionCost = configuration.getInt("dimensionCost", Configuration.CATEGORY_GENERAL, 50, 0, Integer.MAX_VALUE, "Base cost to change dimensions");
        distanceCost = configuration.getInt("distanceCost", Configuration.CATEGORY_GENERAL, 1, 0, Integer.MAX_VALUE, "Cost per meter travelled");

        vanillaRecipes = configuration.getBoolean("vanillaRecipes", Configuration.CATEGORY_GENERAL, false, "Enable recipes using only Minecraft items");
        thermalRecipes = configuration.getBoolean("thermalRecipes", Configuration.CATEGORY_GENERAL, true, "Enable recipes using Thermal Expansion/Foundation/etc items");
        eioRecipes = configuration.getBoolean("eioRecipes", Configuration.CATEGORY_GENERAL, false, "Enable recipes using EnderIO items");
        ic2Recipes = configuration.getBoolean("ic2Recipes", Configuration.CATEGORY_GENERAL, false, "Enable recipes using IndustrialCraft 2 items (note, it will still not use EU");
        exutRecipes = configuration.getBoolean("exutRecipes", Configuration.CATEGORY_GENERAL, false, "Enable recipes using Extra Utilities items");

        if( configuration.hasChanged() ) {
            configuration.save();
        }
    }
}
