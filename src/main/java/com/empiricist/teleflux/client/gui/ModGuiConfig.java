package com.empiricist.teleflux.client.gui;

import com.empiricist.teleflux.handler.ConfigurationHandler;
import com.empiricist.teleflux.reference.Reference;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

//does the gui for adjusting config values in-game
public class ModGuiConfig extends GuiConfig{

    public ModGuiConfig(GuiScreen guiScreen) {
        super(guiScreen,
                new ConfigElement(ConfigurationHandler.configuration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                Reference.MOD_ID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(ConfigurationHandler.configuration.toString()));
        System.out.println("finished super call");
    }
}
