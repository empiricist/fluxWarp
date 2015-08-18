package com.empiricist.fluxwarp.init;

import com.empiricist.fluxwarp.item.*;
import com.empiricist.fluxwarp.reference.Reference;
import cpw.mods.fml.common.registry.GameRegistry;

//this annotation tells forge to preserve this as reference w/o modification (unnecessary, good practice)
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModItems {
    public static final ItemBase fan = new ItemFan();
    public static final ItemBase staff = new ItemStaff();
    public static final ItemDimensionAddress dimensionAddress = new ItemDimensionAddress();
    public static final ItemCreativeAddress creativeAddress = new ItemCreativeAddress();

    //register items from mod
    public static void init(){
        GameRegistry.registerItem(fan, "fan");
        GameRegistry.registerItem(staff, "staff");
        GameRegistry.registerItem(dimensionAddress, "dimensionAddress");
        GameRegistry.registerItem(creativeAddress, "creativeAddress");
    }
}
