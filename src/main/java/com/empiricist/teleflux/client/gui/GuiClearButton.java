package com.empiricist.teleflux.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

//button that is invisible
public class GuiClearButton extends GuiButton{

    public GuiClearButton(int id, int xPos, int yPos, String displayString){
        super(id, xPos, yPos, 200, 20, displayString);
    }

    public GuiClearButton(int id, int xPos, int yPos, int width, int height, String displayString){
        super(id, xPos, yPos, width, height, displayString);
    }

    public void drawButton(Minecraft mc, int x, int y){
        //does not render
    }

}
