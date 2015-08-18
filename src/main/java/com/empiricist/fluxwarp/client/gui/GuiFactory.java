package com.empiricist.fluxwarp.client.gui;

import cpw.mods.fml.client.IModGuiFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.Set;

public class GuiFactory implements IModGuiFactory{
    @Override
    public void initialize(Minecraft minecraftInstance) {//currently unused?

    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ModGuiConfig.class;//class that does config gui
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {//currently unused?
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {//currently unused?
        return null;
    }
}
