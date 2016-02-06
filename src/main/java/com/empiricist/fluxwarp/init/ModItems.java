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
    public static final ItemBase itemBearingCompass = new ItemBearingCompass();
    public static final ItemBase itemInfoDisk = new ItemInfoDisk();

    //register items from mod
    public static void init(){
        GameRegistry.registerItem(debugger, "debugger");
        GameRegistry.registerItem(dimensionAddress, "dimensionAddress");
        GameRegistry.registerItem(creativeAddress, "creativeAddress");
        GameRegistry.registerItem(itemBearingCompass, "itemBearingCompass");
        GameRegistry.registerItem(itemInfoDisk, "itemInfoDisk");
    }
}
