package com.empiricist.teleflux.init;

import com.empiricist.teleflux.handler.ConfigurationHandler;
import com.empiricist.teleflux.utility.LogHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class Recipes {

    //register recipes
    public static void init(){
        //to add vanilla shaped crafting recipe (can also use addShapedRecipe?)
        GameRegistry.addRecipe(new ItemStack(ModItems.bearingCompass), "ggg", "gcg", "ggg", 'g', new ItemStack(Blocks.glass_pane), 'c', new ItemStack(Items.compass));
        GameRegistry.addRecipe(new ItemStack(ModItems.infoDisk), "r", "p", "e", 'r', new ItemStack(Items.redstone), 'p', new ItemStack(Items.paper), 'e', new ItemStack(Items.ender_pearl));

        //to add vanilla shapeless crafting recipe
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.test), new ItemStack(Items.redstone), new ItemStack(Blocks.wool));

        //to add forge oredict shaped crafting recipe (can use oredict names or itemStacks)
        if(ConfigurationHandler.vanillaRecipes){
            LogHelper.info("Registered vanilla Minecraft recipe for Dimension Address");
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.dimensionAddress), " r ", "ibi", " g ", 'r', "dustRedstone", 'i', "ingotIron", 'b', new ItemStack(Items.glass_bottle), 'g', "ingotGold"));
            LogHelper.info("Registered vanilla Minecraft recipe for Warp Core");
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.warpcore), "gig", "pep", "iri", 'g', "ingotGold", 'i', "ingotIron", 'p', new ItemStack(Blocks.piston), 'e', new ItemStack(Items.ender_pearl), 'r', "dustRedstone"));
        }

    }

    public static void postInit(){
        if(ConfigurationHandler.thermalRecipes){

            String thermalID = "ThermalExpansion";

            if(!Loader.isModLoaded(thermalID)){
                LogHelper.warn(thermalID + " is not loaded!");
//                List<ModContainer> mods = Loader.instance().getActiveModList();
//                for( ModContainer mod : mods ){
//                    LogHelper.info("ModID:" + mod.getModId());
//                }
            }else{
//                LogHelper.info("Dumping Registry to " + Minecraft.getMinecraft().mcDataDir);
//
//                //GameData.dumpRegistry(Minecraft.getMinecraft().mcDataDir);
//                Set example = GameData.getItemRegistry().getKeys();
//
//                LogHelper.info("Found " + example.size() + " items");
//                String name = "";
//                for(Object o : example){
//                    name = o.toString();
//                    if(name.contains(thermalID)){
//                        LogHelper.info(name);
//                    }
//
//                }
//
//                example = Block.blockRegistry.getKeys();
//                LogHelper.info("Found " + example.size() + " blocks");
//
//                for(Object o : example){
//                    name = o.toString();
//                    if(name.contains("Thermal")){
//                        LogHelper.info(name);
//                    }
//
//                }

            }

            if(exists(thermalID, "material") && exists(thermalID, "diagram") ){
                LogHelper.info("Registered " + thermalID + " recipe for Dimension Address");
                GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ModItems.dimensionAddress), new ItemStack(GameRegistry.findItem(thermalID, "material"), 1, 0), new ItemStack(GameRegistry.findItem(thermalID, "diagram"), 1, 1 ) ) );//servo and redprint
            }
            if(exists(thermalID, "Frame") && exists(thermalID, "Plate") && exists(thermalID, "material")){
                LogHelper.info("Registered " + thermalID + " recipe for Warp Core");
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.warpcore), "sds", "ptp", "scs", 's', "ingotSilver", 'd', "dustEnderium", 'p', new ItemStack(GameRegistry.findBlock(thermalID, "Plate"), 1, 3), 't', new ItemStack(GameRegistry.findBlock(thermalID, "Frame"), 1, 11), 'c', new ItemStack(GameRegistry.findItem(thermalID, "material"), 1, 1 )));//translocation plate, full tesseract frame, redstone reception coil
            }
        }
        if(ConfigurationHandler.exutRecipes){

            String xuID = "ExtraUtilities";

            if(!Loader.isModLoaded(xuID)){
                LogHelper.warn(xuID + " is not loaded!");
//                List<ModContainer> mods = Loader.instance().getActiveModList();
//                for( ModContainer mod : mods ){
//                    LogHelper.info("ModID:" + mod.getModId());
//                }
            }

            if( exists(xuID, "pipes")  ){
                LogHelper.info("Registered " + xuID + " recipe for Dimension Address");
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.dimensionAddress), "pip", "pip", " g ", 'p', new ItemStack(GameRegistry.findItem(xuID, "pipes"), 1, 0), 'i', "ingotIron", 'g', "ingotGold" ) );//transfer pipes
            }
            if(exists(xuID, "decorativeBlock1") && exists(xuID, "extractor_base") && exists(xuID, "endConstructor")){
                LogHelper.info("Registered " + xuID + " recipe for Warp Core");
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.warpcore), "ono", "ece", "omo", 'o', new ItemStack(GameRegistry.findBlock(xuID, "decorativeBlock1"), 1, 1), 'n', new ItemStack(GameRegistry.findBlock(xuID, "extractor_base"), 1, 12), 'e', new ItemStack(GameRegistry.findBlock(xuID, "endConstructor"), 1, 2), 'c', new ItemStack(GameRegistry.findBlock(xuID, "decorativeBlock1"), 1, 11), 'm', new ItemStack(GameRegistry.findItem(xuID, "decorativeBlock1"), 1, 12 )));//infused obsidian, energy node, flux crystal, ender core, etched computational matrix
            }
        }
        if(ConfigurationHandler.eioRecipes) {

            String eioID = "EnderIO";

            if (!Loader.isModLoaded(eioID)) {
                LogHelper.warn(eioID + " is not loaded!");
//                List<ModContainer> mods = Loader.instance().getActiveModList();
//                for( ModContainer mod : mods ){
//                    LogHelper.info("ModID:" + mod.getModId());
//                }
            }

            if (exists(eioID, "itemBasicFilterUpgrade") && exists(eioID, "itemBasicCapacitor")) {
                LogHelper.info("Registered " + eioID + " recipe for Dimension Address");
                GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ModItems.dimensionAddress), new ItemStack(GameRegistry.findItem(eioID, "itemBasicFilterUpgrade"), 1, 0), new ItemStack(GameRegistry.findItem(eioID, "itemBasicCapacitor"), 1, 0)));//filter and basic capacitor
            }
            if (exists(eioID, "itemBasicCapacitor") && exists(eioID, "blockTravelAnchor") && exists(eioID, "itemFrankenSkull")) {
                LogHelper.info("Registered " + eioID + " recipe for Warp Core");
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.warpcore), "ece", "iai", "ehe", 'e', "ingotElectricalSteel", 'c', new ItemStack(GameRegistry.findItem(eioID, "itemBasicCapacitor"), 1, 2), 'i', "ingotPhasedIron", 'a', new ItemStack(GameRegistry.findBlock(eioID, "blockTravelAnchor"), 1, 0), 'h', new ItemStack(GameRegistry.findItem(eioID, "itemFrankenSkull"), 1, 3)));//big cap, travel anchor, enderman head circuit        }
            }
        }
        if(ConfigurationHandler.ic2Recipes){

            String ic2ID = "IC2";

            if(!Loader.isModLoaded(ic2ID)){
                LogHelper.warn(ic2ID + " is not loaded!");
//                List<ModContainer> mods = Loader.instance().getActiveModList();
//                for( ModContainer mod : mods ){
//                    LogHelper.info("ModID:" + mod.getModId());
//                }
            }

            if( exists(ic2ID, "itemPartCircuit") && exists(ic2ID, "itemCasing") ){
                LogHelper.info("Registered " + ic2ID + " recipe for Dimension Address");
                GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ModItems.dimensionAddress), "circuitBasic", new ItemStack(GameRegistry.findItem(ic2ID, "itemCasing"), 1, 4) ) );//circuit and iron casing
            }
            if( exists(ic2ID, "itemCasing") && exists(ic2ID, "itemRecipePart") && exists(ic2ID, "itemPartCircuitAdv") && exists(ic2ID, "blockMachine2")){
                LogHelper.info("Registered " + ic2ID + " recipe for Warp Core");
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.warpcore), "ibi", "ctc", "iai", 'i', new ItemStack(GameRegistry.findItem(ic2ID, "itemCasing"), 1, 4), 'b', "circuitBasic", 'c', new ItemStack(GameRegistry.findItem(ic2ID, "itemRecipePart"), 1, 0), 't', new ItemStack(GameRegistry.findBlock(ic2ID, "blockMachine2"), 1, 0), 'a', "circuitAdvanced" ));//iron case, circuit, coil, teleporter, adv circuit
            }
        }
    }

    public static boolean exists(String modid, String name) {
        if ( GameRegistry.findItem(modid, name) != null) {
            LogHelper.info("Found item " + name + " for recipe");
            return true;
        }else if (GameRegistry.findBlock(modid, name) != null){
            LogHelper.info("Found block " + name + " for recipe");
            return true;
        }else{
            LogHelper.warn("Did not find " + name + " for recipe!");
            return false;
        }
    }
}
