package com.empiricist.fluxwarp.client.handler;

import com.empiricist.fluxwarp.client.Settings.Keybindings;
import com.empiricist.fluxwarp.reference.Key;
import com.empiricist.fluxwarp.utility.LogHelper;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//currently keys are disabled in main class preinit method, and here with subscribe annotation
public class KeyInputEventHandler {
    private static Key getPressedKeyBinding(){//event has little info, we need to check ourselves
        if(Keybindings.charge.isPressed()){
            return Key.CHARGE;//from our enum of keybindings
        }else if(Keybindings.release.isPressed()){
            return Key.RELEASE;
        }else{
            return Key.UNKNOWN;
        }
    }

    //@SubscribeEvent
    public void handleKeyInputEvent(InputEvent.KeyInputEvent event){
        Key pressed = getPressedKeyBinding();
        //LogHelper.info("Key Pressed: " + pressed);
        if (pressed == Key.CHARGE){
            Minecraft mc = Minecraft.getMinecraft();
            int x = (int)mc.thePlayer.posX;
            int y = (int)mc.thePlayer.posY - 2;
            int z = (int)mc.thePlayer.posZ - 1;
            LogHelper.info("X:" + x + " Y:" + y + " Z:" + z + " Block:" + mc.theWorld.getBlockState(new BlockPos(x,y,z)) );
            //LogHelper.info("Block: " + getPressedKeyBinding());
        }
        if (pressed == Key.RELEASE){
            //LogHelper.info("Release Key");
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.thePlayer;
            ItemStack stack = player.getHeldItem();
            LogHelper.info( (stack != null) ? "Display Name: " + stack.getDisplayName() + ", Unlocalized Name:" + stack.getItem().getUnlocalizedName() : "Stack is null");

        }

        //note: to do anything serverside, like opening a gui, you will need to send info to server
    }
}
