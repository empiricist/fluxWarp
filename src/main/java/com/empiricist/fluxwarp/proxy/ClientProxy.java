package com.empiricist.fluxwarp.proxy;

import com.empiricist.fluxwarp.client.Settings.Keybindings;
import com.empiricist.fluxwarp.init.ModBlocks;
import com.empiricist.fluxwarp.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
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
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        //blocks
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(ModBlocks.test), 0, new ModelResourceLocation(Reference.MOD_ID + ":" + ModBlocks.test.getUnlocalizedName(), "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(ModBlocks.warpcore), 0, new ModelResourceLocation(Reference.MOD_ID + ":" + ModBlocks.warpcore.getUnlocalizedName(), "inventory"));

        //items
        //renderItem.getItemModelMesher().register(tutorialItem, 0, new ModelResourceLocation(Reference.MODID + ":" + ((ItemTutorial) tutorialItem).getName(), "inventory"));

    }
}
