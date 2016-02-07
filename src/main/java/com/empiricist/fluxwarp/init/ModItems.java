package com.empiricist.fluxwarp.init;

import com.empiricist.fluxwarp.item.*;
import com.empiricist.fluxwarp.reference.Reference;
import net.minecraftforge.fml.common.registry.GameRegistry;

//this annotation tells forge to preserve this as reference w/o modification (unnecessary, good practice)
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModItems {
    public static final ItemBase debugger = new ItemDebugger();
    public static final ItemDimensionAddress dimensionAddress = new ItemDimensionAddress();
    public static final ItemCreativeAddress creativeAddress = new ItemCreativeAddress();
    public static final ItemBase bearingCompass = new ItemBearingCompass();
    public static final ItemBase infoDisk = new ItemInfoDisk();

    //register items from mod
    public static void init(){
        GameRegistry.registerItem(debugger, debugger.getName());
        GameRegistry.registerItem(dimensionAddress, dimensionAddress.getName());
        GameRegistry.registerItem(creativeAddress, creativeAddress.getName());
        GameRegistry.registerItem(bearingCompass, bearingCompass.getName());
        GameRegistry.registerItem(infoDisk, infoDisk.getName());
    }
}
