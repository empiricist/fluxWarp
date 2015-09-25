package com.empiricist.fluxwarp.client.gui;

import com.empiricist.fluxwarp.reference.Reference;
import com.empiricist.fluxwarp.tileentity.ContainerDimensionDatabase;
import com.empiricist.fluxwarp.tileentity.TileEntityDimensionDatabase;
import com.empiricist.fluxwarp.utility.LogHelper;
import com.empiricist.fluxwarp.utility.ParseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiDimensionDatabase extends GuiContainer{
    private  static final ResourceLocation guiTexture = new ResourceLocation(Reference.MOD_ID.toLowerCase(), "textures/blocks/debug.png");
    private ArrayList<GuiButton> coordTabButtons = new ArrayList<GuiButton>();
    private ArrayList<GuiButton> boundsTabButtons = new ArrayList<GuiButton>();
    private int entry = -1;
    public String[] entries = {"", "", "", ""};
    private TileEntityDimensionDatabase te;

    public GuiDimensionDatabase(IInventory players, TileEntityDimensionDatabase te) {
        super(new ContainerDimensionDatabase(players, te));
        this.te = te;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        GL11.glColor4f(1, 1, 1, 1);
        //set current texture to one at ResourceLocation
        Minecraft.getMinecraft().getTextureManager().bindTexture(guiTexture);
        //draw textured rectangle (screen pos, texture pos1, texture pos2)
        //guiLeft/Top are precalculated drawing locations for centered gui
        //gui is at top left of file, so start at 0, 0, go to x, y
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    //draw text over gui texture (incl mouseover text)
    @Override//foreground layer doesn't know about guiLeft and guiTop?
    protected void drawGuiContainerForegroundLayer(int x, int y){
        //FR is part of gui
        //(text, pos in gui, hex color)
        fontRendererObj.drawString("Warp GUI testing lalala", 8, 6, 0x404040);
        //text color seems to auto update (is this run each render tick?)
        //if we get meta from worldObj, gui will update when meta changes while gui open
        //int type = machine.getBlockMetadata() / 2;//type from card

        String str = "this is a String";
        boolean invalid = true;
        int color = invalid ? 0xD30000 : 0x404040;
        //auto line break, (text, pos in gui, width to break at, hex color)
        fontRendererObj.drawSplitString(str, 45, 44, 100, color);//left of icon


        //LogHelper.warn("String is \"" + entries[0] + "\"");
        fontRendererObj.drawSplitString(entries[0], guiLeft+100, guiTop+10, 100, color);//dx
        fontRendererObj.drawSplitString(entries[1], guiLeft+100, guiTop+35, 100, color);//dy
        fontRendererObj.drawSplitString(entries[2], guiLeft+100, guiTop+60, 100, color);//dz


        //we'll add mouseover text to our custom buttons
		/*//moved to GuiTabCustom
		if(type==4){//if custom mode
			for(int i = 0; i < rectangles.length; i++){
				GuiRectangle rect = rectangles[i];
				String text;
				//if(rect.inRect(this, x, y)){//if mouse over rectangle
				//ArrayList<String> lines = new ArrayList<String>();//we'll use arraylist of lines
				if(machine.customSetup[i]){
					//lines.add(GuiColor.GREEN + "Active");//color code plus text
					text = GuiColor.GREEN + "Active";
				}else{
					//lines.add(GuiColor.RED + "Inactive");
					text = GuiColor.RED + "Inactive";
				}
				//lines.add(GuiColor.YELLOW + "Click to change");
				text += "\n" + GuiColor.YELLOW + "Click to change";
				//drawHoveringText(lines, x-guiLeft, y-guiTop, fontRenderer);//gui pos
				rect.drawString(this, x, y, text);
				//}
			}
		}*/

        //give tabs mouseover text (they are rectangles so they have our drawstring method)
//        for(GuiTab tab : tabs){
//            tab.drawString(this, x, y, tab.getName());
//        }
    }

    @Override//will let us specify buttons, etc
    public void initGui(){
        super.initGui();//important
        buttonList.clear();//just in case some left from prev gui?

        //id, screen pos, size, text
        GuiButton xCoord = new GuiButton(0, guiLeft+80, guiTop+10, 30, 20, "+");
        buttonList.add(xCoord);
        coordTabButtons.add(xCoord);
        GuiButton yCoord = new GuiButton(1, guiLeft+80, guiTop+35, 30, 20, "+");
        buttonList.add(yCoord);
        coordTabButtons.add(yCoord);
        GuiButton zCoord = new GuiButton(2, guiLeft+80, guiTop+60, 30, 20, "+");
        buttonList.add(zCoord);
        coordTabButtons.add(zCoord);
        //once added, will be rendered automatically

        GuiButton clearButton = new GuiButton(10, guiLeft+130, guiTop+10, 40, 20, "Clear");
        buttonList.add(clearButton);
        boundsTabButtons.add(clearButton);

        for (Object b : buttonList ){
            if(b instanceof GuiButton){ ((GuiButton)b).visible = false; }
        }
        for (GuiButton b : coordTabButtons ){
            b.visible = true;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button){//vanilla type button clicked
        entry = button.id;
//        if(button.id==0){//update text w/o reopening gui
//            //not the best way to do this, doesn't deal w/ several players in gui at once
//            //button.visible = !button.visible; //displayString = button.displayString.equals(DISABLE_TEXT)? ENABLE_TEXT : DISABLE_TEXT;
//            entry = 0;
//        }else if(button.id == 1){
//            //button.enabled = false;//type cleared, button now disabled
//            entry = 1;
//        }
//        else if(button.id == 2){
//            //button.enabled = false;//type cleared, button now disabled
//            entry = 2;
//        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        if(y<50){
            if(x>50 && x<100){
                for (Object b : buttonList ){
                    if(b instanceof GuiButton){ ((GuiButton)b).visible = false; }
                }
                for (GuiButton b : boundsTabButtons ){
                    b.visible = true;
                }
            }else if(x>100 && x<150){
                for (Object b : buttonList ){
                    if(b instanceof GuiButton){ ((GuiButton)b).visible = false; }
                }
                for (GuiButton b : coordTabButtons ){
                    b.visible = true;
                }
            }
        }
        this.mc.playerController.sendEnchantPacket(inventorySlots.windowId, x);
        this.mc.playerController.sendEnchantPacket(inventorySlots.windowId, y);
        //inventorySlots.slotClick(x+1000*y, 0, 0, Minecraft.getMinecraft().thePlayer);
    }

    @Override
    protected void keyTyped(char character, int key){
        super.keyTyped(character, key);
        System.out.println("Key Pressed: " + character + " " + key);

        if(entry < 0 || entry >= entries.length){ entry = -1; return; }



//        String next = "";
//        if(key==2 || key==79){
//            next = "1";
//        }else if(key == 3 || key == 80){
//            next = "2";
//        }

//        switch(entry) {
//            case 0:
//                xString += next;
//                break;
//            case 1:
//                yString += next;
//                break;
//            case 2:
//                zString += next;
//                break;
//        }
        if(key == 14 || key == 211){ //backspace, delete
            if(entries[entry].length() > 1){ entries[entry] = entries[entry].substring(0, entries[entry].length() - 1); } else { entries[entry] = ""; }
//            switch(entry) {
//                case 0:
//                    if(xString.length() > 1){ xString = xString.substring(0, xString.length()-1); } else { xString = "";}
//                    break;
//                case 1:
//                    if(yString.length() > 1){ yString = yString.substring(0, yString.length()-1); } else { yString = "";}
//                    break;
//                case 2:
//                    if(zString.length() > 1){ zString = zString.substring(0, zString.length()-1); } else { zString = "";}
//                    break;
//            }
        }else if (key==28){ //enter
            if(entry >= 0 && entry < entries.length){
                int number = ParseHelper.safeReadInt(ParseHelper.safeReadString(entries[entry]), 0);
                entries[entry] = "" + number;
                this.mc.playerController.sendEnchantPacket(inventorySlots.windowId, number);
            }
            entry = -1;
        }else{
            entries[entry] += character;
        }
    }
}
