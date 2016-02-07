package com.empiricist.fluxwarp.proxy;

import com.empiricist.fluxwarp.client.Settings.Keybindings;
import com.empiricist.fluxwarp.init.ModBlocks;
import com.empiricist.fluxwarp.init.ModItems;
import com.empiricist.fluxwarp.reference.Reference;
import com.empiricist.fluxwarp.utility.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;


public class ClientProxy extends CommonProxy{
    @Override
    public void registerKeyBindings(){
        //currently keys are disabled in main class preinit method
        ClientRegistry.registerKeyBinding(Keybindings.charge);
        ClientRegistry.registerKeyBinding(Keybindings.release);
    }

    @Override
    public void registerRenders(){
        LogHelper.info("Doing rendery model thingies");
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        //blocks
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(ModBlocks.test), 0, new ModelResourceLocation(Reference.MOD_ID + ":" + ModBlocks.test.getName(), "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(ModBlocks.warpcore), 0, new ModelResourceLocation(Reference.MOD_ID + ":" + ModBlocks.warpcore.getName(), "inventory"));

        //items
        renderItem.getItemModelMesher().register(ModItems.bearingCompass, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + ModItems.bearingCompass.getName(), "inventory"));
        renderItem.getItemModelMesher().register(ModItems.creativeAddress, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + ModItems.creativeAddress.getName(), "inventory"));
        renderItem.getItemModelMesher().register(ModItems.debugger, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + ModItems.debugger.getName(), "inventory"));
        renderItem.getItemModelMesher().register(ModItems.dimensionAddress, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + ModItems.dimensionAddress.getName(), "inventory"));
        renderItem.getItemModelMesher().register(ModItems.infoDisk, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + ModItems.infoDisk.getName(), "inventory"));

        ModelBakery.addVariantName(ModItems.dimensionAddress, new String[]{"fluxwarp:dimensionAddress", "fluxwarp:dimensionAddressSaved"});
    }
}
