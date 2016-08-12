package com.empiricist.teleflux.init;


import com.empiricist.teleflux.item.ItemBase;
import com.empiricist.teleflux.item.ItemCreativeAddress;
import com.empiricist.teleflux.item.ItemDebugger;
import com.empiricist.teleflux.item.ItemDimensionAddress;
import com.empiricist.teleflux.item.ItemBearingCompass;
import com.empiricist.teleflux.item.ItemInfoDisk;
import com.empiricist.teleflux.reference.Reference;
import com.empiricist.teleflux.utility.ParseHelper;
import net.minecraft.util.ResourceLocation;
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
        GameRegistry.register(debugger,         new ResourceLocation(Reference.MOD_ID + ":" + debugger.getName()));
        GameRegistry.register(dimensionAddress, new ResourceLocation(Reference.MOD_ID + ":" + dimensionAddress.getName()));
        GameRegistry.register(creativeAddress,  new ResourceLocation(Reference.MOD_ID + ":" + creativeAddress.getName()));
        GameRegistry.register(bearingCompass,   new ResourceLocation(Reference.MOD_ID + ":" + bearingCompass.getName()));
        GameRegistry.register(infoDisk,         new ResourceLocation(Reference.MOD_ID + ":" + infoDisk.getName()));
    }
}
