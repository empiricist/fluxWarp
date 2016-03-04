package com.empiricist.teleflux.client.gui;

import com.empiricist.teleflux.reference.Reference;
import com.empiricist.teleflux.tileentity.ContainerWarpCore;
import com.empiricist.teleflux.tileentity.TileEntityWarpCore;
import com.empiricist.teleflux.utility.ParseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;

public class GuiWarpCore extends GuiContainer{
    private  static final ResourceLocation guiTexture = new ResourceLocation(Reference.MOD_ID.toLowerCase(), "textures/guis/warpGui.png");
    private ArrayList<GuiButton> coordTabButtons = new ArrayList<GuiButton>();
    private ArrayList<GuiButton> boundsTabButtons = new ArrayList<GuiButton>();
    public int tab = 0;
    private int entry = -1;
    public String[] entries = {"", "", "", "", "", "", "", "", "", "", ""};
    private TileEntityWarpCore te;

    public GuiWarpCore(IInventory players, TileEntityWarpCore te) {
        super(new ContainerWarpCore(players, te));
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
        //texture coord 2 is relative to coord 1
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        if( tab == 0 ){
            //tabs
            drawTexturedModalRect(guiLeft+4, guiTop-28, 195, 30, 28, 32);
            drawTexturedModalRect(guiLeft+32, guiTop-28, 195, 0, 28, 30);
            //text boxes
            drawTexturedModalRect(guiLeft+58, guiTop+23, 0, 136, 89, 11);
            drawTexturedModalRect(guiLeft+58, guiTop+47, 0, 136, 89, 11);
            drawTexturedModalRect(guiLeft+58, guiTop+71, 0, 136, 89, 11);
            drawTexturedModalRect(guiLeft+58, guiTop+95, 0, 136, 89, 11);
        }else{
            //tabs
            drawTexturedModalRect(guiLeft+4, guiTop-28, 195, 0, 28, 30);
            drawTexturedModalRect(guiLeft+32, guiTop-28, 195, 30, 28, 32);
            //text boxes
            drawTexturedModalRect(guiLeft+58, guiTop+23, 0, 136, 25, 11);
            drawTexturedModalRect(guiLeft+58, guiTop+47, 0, 136, 25, 11);
            drawTexturedModalRect(guiLeft+58, guiTop+71, 0, 136, 25, 11);
            drawTexturedModalRect(guiLeft+124, guiTop+23, 0, 136, 25, 11);
            drawTexturedModalRect(guiLeft+124, guiTop+47, 0, 136, 25, 11);
            drawTexturedModalRect(guiLeft+124, guiTop+71, 0, 136, 25, 11);
        }
        //energy bar
        //LogHelper.info("RF : " + te.getEnergyStored(EnumFacing.NORTH) +  " / " + te.getMaxEnergyStored(EnumFacing.NORTH));
        double energyRatio = (double)te.getEnergyStored(EnumFacing.NORTH)/(te.getMaxEnergyStored(EnumFacing.NORTH));
        int barHeight = (int)(energyRatio * 112);
        if (barHeight > 0) {
            int srcX = xSize;
            int srcY = 62+112 - barHeight;

            drawTexturedModalRect(guiLeft + 174, guiTop + 17 + 112 - barHeight, srcX, srcY, 14, barHeight);
        }
        //tab icons
        drawTexturedModalRect(guiLeft+10, guiTop-19, 128, 136, 16, 16);
        drawTexturedModalRect(guiLeft+38, guiTop-19, 144, 136, 16, 16);
    }

    //draw text over gui texture (incl mouseover text)
    @Override//foreground layer doesn't know about guiLeft and guiTop?
    protected void drawGuiContainerForegroundLayer(int x, int y){
        //FR is part of gui
        //(text, pos in gui, hex color)
        fontRendererObj.drawString("Warp Core : " + (tab==0? "Warp Coordinates" : "Warp Bounds"), 6, 6, 0x404040);
        //text color seems to auto update (is this run each render tick?)
        //if we get meta from worldObj, gui will update when meta changes while gui open
        //int type = machine.getBlockMetadata() / 2;//type from card

        fontRendererObj.drawSplitString("RF", 176, 6, 100, 0x904040);//dx
        if(tab == 0){
            fontRendererObj.drawSplitString("X:", 30, 25, 100, (entry==0 ? 0x209030 : 0x606060));
            fontRendererObj.drawSplitString("Y:", 30, 49, 100, (entry==1 ? 0x209030 : 0x606060));
            fontRendererObj.drawSplitString("Z:", 30, 73, 100, (entry==2 ? 0x209030 : 0x606060));
            fontRendererObj.drawSplitString("Dim:", 25, 97, 100, (entry==3 ? 0x209030 : 0x606060));

            if(entry==0){
                fontRendererObj.drawSplitString(entries[0]+"_", 60, 25, 100, 0x209030);//dx
            }else{
                fontRendererObj.drawSplitString(entries[0], 60, 25, 100, 0x606060);//dx
            }
            if(entry==1){
                fontRendererObj.drawSplitString(entries[1]+"_", 60, 49, 100, 0x209030);//dy
            }else{
                fontRendererObj.drawSplitString(entries[1], 60, 49, 100, 0x606060);
            }
            if(entry==2){
                fontRendererObj.drawSplitString(entries[2]+"_", 60, 73, 100, 0x209030);//dz
            }else{
                fontRendererObj.drawSplitString(entries[2], 60, 73, 100, 0x606060);//dz
            }
            if(entry==3){
                fontRendererObj.drawSplitString(entries[3]+"_", 60, 97, 100, 0x209030);//dim
            }else{
                fontRendererObj.drawSplitString(entries[3], 60, 97, 100, 0x606060);//dim
            }
            //fontRendererObj.drawSplitString(entries[0], 60, 25, 100, (entry==0 ? 0x209030 : 0x606060));//dx
            //fontRendererObj.drawSplitString(entries[1], 60, 49, 100, (entry==1 ? 0x209030 : 0x606060));//dy
            //fontRendererObj.drawSplitString(entries[2], 60, 73, 100, (entry==2 ? 0x209030 : 0x606060));//dz
            //fontRendererObj.drawSplitString(entries[3], 60, 97, 100, (entry==3 ? 0x209030 : 0x606060));//dim
        }else if(tab == 1){
            //fontRendererObj.drawSplitString("HELLO!!!!!", 50, 80, 100, 0x404040);
            fontRendererObj.drawSplitString("+X:", 30, 25, 100, (entry==5 ? 0x209030 : 0x606060));
            fontRendererObj.drawSplitString("+Y:", 30, 49, 100, (entry==7 ? 0x209030 : 0x606060));
            fontRendererObj.drawSplitString("+Z:", 30, 73, 100, (entry==9 ? 0x209030 : 0x606060));
            fontRendererObj.drawSplitString("-X:", 96, 25, 100, (entry==6 ? 0x209030 : 0x606060));
            fontRendererObj.drawSplitString("-Y:", 96, 49, 100, (entry==8 ? 0x209030 : 0x606060));
            fontRendererObj.drawSplitString("-Z:", 96, 73, 100, (entry==10 ? 0x209030 : 0x606060));

            if(entry == 5){
                fontRendererObj.drawSplitString(entries[5]+"_", 60, 25, 100, 0x209030);//+x
            }else{
                fontRendererObj.drawSplitString(entries[5], 60, 25, 100, 0x606060);//+x
            }
            if(entry == 7){
                fontRendererObj.drawSplitString(entries[7]+"_", 60, 49, 100, 0x209030);//+y
            }else{
                fontRendererObj.drawSplitString(entries[7], 60, 49, 100, 0x606060);//+y
            }
            if(entry == 9){
                fontRendererObj.drawSplitString(entries[9]+"_", 60, 73, 100, 0x209030);//+z
            }else{
                fontRendererObj.drawSplitString(entries[9], 60, 73, 100, 0x606060);//+z
            }
            if(entry == 6){
                fontRendererObj.drawSplitString(entries[6]+"_", 126, 25, 100, 0x209030);//-x
            }else{
                fontRendererObj.drawSplitString(entries[6], 126, 25, 100, 0x606060);//-x
            }
            if(entry == 8){
                fontRendererObj.drawSplitString(entries[8]+"_", 126, 49, 100, 0x209030);//-y
            }else{
                fontRendererObj.drawSplitString(entries[8], 126, 49, 100, 0x606060);//-y
            }
            if(entry == 10){
                fontRendererObj.drawSplitString(entries[10]+"_", 126, 73, 100, 0x209030);//-z
            }else{
                fontRendererObj.drawSplitString(entries[10], 126, 73, 100, 0x606060);//-z
            }
//            fontRendererObj.drawSplitString(entries[5], 60, 25, 100, (entry==5 ? 0x209030 : 0x606060));//+x
//            fontRendererObj.drawSplitString(entries[7], 60, 49, 100, (entry==7 ? 0x209030 : 0x606060));//+y
//            fontRendererObj.drawSplitString(entries[9], 60, 73, 100, (entry==9 ? 0x209030 : 0x606060));//+z
//            fontRendererObj.drawSplitString(entries[6], 126, 25, 100, (entry==6 ? 0x209030 : 0x606060));//-x
//            fontRendererObj.drawSplitString(entries[8], 126, 49, 100, (entry==8 ? 0x209030 : 0x606060));//-y
//            fontRendererObj.drawSplitString(entries[10], 126, 73, 100, (entry==10 ? 0x209030 : 0x606060));//-z

            fontRendererObj.drawSplitString("These are the distances from the warp core block to the walls of the volume to teleport", 15, 90, 130, 0x606060);
        }
        //auto line break, (text, pos in gui, width to break at, hex color)



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
        xSize = 195;
        ySize = 136;
        super.initGui();//important


        buttonList.clear();//just in case some left from prev gui?

        //id, screen pos, size, text
        GuiButton xCoord = new GuiClearButton(0, guiLeft+58, guiTop+23, 89, 11, "X");
        buttonList.add(xCoord);
        coordTabButtons.add(xCoord);
        GuiButton yCoord = new GuiClearButton(1, guiLeft+58, guiTop+47, 89, 11, "Y");
        buttonList.add(yCoord);
        coordTabButtons.add(yCoord);
        GuiButton zCoord = new GuiClearButton(2, guiLeft+58, guiTop+71, 89, 11, "Z");
        buttonList.add(zCoord);
        coordTabButtons.add(zCoord);
        GuiButton dCoord = new GuiClearButton(3, guiLeft+58, guiTop+95, 89, 11, "Dim");
        buttonList.add(dCoord);
        coordTabButtons.add(dCoord);

        GuiButton doWarp = new GuiButton(4, guiLeft+65, guiTop+111, 30, 20, "Warp");
        buttonList.add(doWarp);
        coordTabButtons.add(doWarp);
        //once added, will be rendered automatically

        GuiButton xPlusButton = new GuiClearButton(5, guiLeft+58, guiTop+23, 25, 11, "+X");
        buttonList.add(xPlusButton);
        boundsTabButtons.add(xPlusButton);
        GuiButton yPlusButton = new GuiClearButton(7, guiLeft+58, guiTop+47, 25, 11, "+Y");
        buttonList.add(yPlusButton);
        boundsTabButtons.add(yPlusButton);
        GuiButton zPlusButton = new GuiClearButton(9, guiLeft+58, guiTop+71, 25, 11, "+Z");
        buttonList.add(zPlusButton);
        boundsTabButtons.add(zPlusButton);
        GuiButton xMinusButton = new GuiClearButton(6, guiLeft+124, guiTop+23, 25, 11, "-X");
        buttonList.add(xMinusButton);
        boundsTabButtons.add(xMinusButton);
        GuiButton yMinusButton = new GuiClearButton(8, guiLeft+124, guiTop+47, 25, 11, "-Y");
        buttonList.add(yMinusButton);
        boundsTabButtons.add(yMinusButton);
        GuiButton zMinusButton = new GuiClearButton(10, guiLeft+124, guiTop+71, 25, 11, "-Z");
        buttonList.add(zMinusButton);
        boundsTabButtons.add(zMinusButton);

        for (Object b : buttonList ){
            if(b instanceof GuiButton){ ((GuiButton)b).visible = false; }
        }
        for (GuiButton b : coordTabButtons ){
            b.visible = true;
        }
        entries[0] = te.getDx() + "";
        entries[1] = te.getDy() + "";
        entries[2] = te.getDz() + "";
        entries[3] = te.getDestDim() + "";

        entries[5] = te.getXPlus() + "";
        entries[6] = te.getXMinus() + "";
        entries[7] = te.getYPlus() + "";
        entries[8] = te.getYMinus() + "";
        entries[9] = te.getZPlus() + "";
        entries[10] = te.getZMinus() + "";

    }

    public void onGuiClosed(){
        if (this.mc.thePlayer != null)
        {
            this.inventorySlots.onContainerClosed(this.mc.thePlayer);
        }

    }

    @Override
    protected void actionPerformed(GuiButton button){//guibutton clicked
        saveInput();
        if(button.id != 4){ //warp is not a state we need to keep
            entry=button.id;
        }else{
            this.mc.playerController.sendEnchantPacket(inventorySlots.windowId, 4);
        }

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
    protected void mouseClicked(int x, int y, int button) throws IOException{
        super.mouseClicked(x, y, button);
        int gy = y - guiTop;
        int gx = x - guiLeft;
        if(gy < 0 && gy > -25){
            if(gx>5 && gx<30){
                tab = 0;
                for (Object b : buttonList ){
                    if(b instanceof GuiButton){ ((GuiButton)b).visible = false; }
                }
                for (GuiButton b : coordTabButtons ){
                    b.visible = true;
                }
            }else if(gx>30 && gx<55){
                tab = 1;
                for (Object b : buttonList ){
                    if(b instanceof GuiButton){ ((GuiButton)b).visible = false; }
                }
                for (GuiButton b : boundsTabButtons ){
                    b.visible = true;
                }
            }
        }
        //LogHelper.info("Mouse Clicked at x " + x + ", y " + y + ", with button " + button);
        //this.mc.playerController.sendEnchantPacket(inventorySlots.windowId, x);
        //this.mc.playerController.sendEnchantPacket(inventorySlots.windowId, y);

        //inventorySlots.slotClick(x+1000*y, 0, 0, Minecraft.getMinecraft().thePlayer);
    }

    @Override
    protected void keyTyped(char character, int key) throws IOException{
        super.keyTyped(character, key);
        //LogHelper.info("Key Pressed: " + character + " " + key);

        if(entry < 0 || entry >= entries.length){ entry = -1; return; }



        if(key == 14 || key == 211){ //backspace, delete
            if(entries[entry].length() > 1){ entries[entry] = entries[entry].substring(0, entries[entry].length() - 1); } else { entries[entry] = ""; }

        }else if (key==28){ //enter
            saveInput();
        }else if(key == 12 || key == 74){ //minus sign -
            if( entries[entry].length() >0 && entries[entry].substring(0,1).equals("-") ){
                entries[entry] = entries[entry].substring(1);//remove leading negative sign
            }else{
                entries[entry] = "-" + entries[entry];//add leading negative sign
            }
        }else if(key == 15) { //tab
            int val = entry+1;
            saveInput();
            if( tab == 0 ){
                entry = (val<5) ? val % 4 : 0;//move through tab 1 boxes (entry 0 to 3)
            }else{
                entry = (val<5) ? 5 : (val-5) % 6 + 5;//move through tab 2 boxes (entry 5 to 10)
            }

        }else { //other characters are typed
            if( (entry < 4 && entries[entry].length() < 13) || (entry > 4 && entries[entry].length() < 3)){
                entries[entry] += character;
            }
        }
    }

    private void saveInput(){
        if(entry >= 0 && entry < entries.length){
            int defaultValue = ((entry != 3)? 0 : te.getWorld().provider.getDimensionId());
            int number = ParseHelper.safeReadInt(ParseHelper.safeReadString(entries[entry]), defaultValue);
            entries[entry] = "" + number;
            this.mc.playerController.sendEnchantPacket(inventorySlots.windowId, (number<<4) + entry);
        }
        entry = -1;
    }
}
