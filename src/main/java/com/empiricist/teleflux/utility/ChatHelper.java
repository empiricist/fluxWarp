package com.empiricist.teleflux.utility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class ChatHelper {
    public static void sendText(EntityPlayer player, String text){
        player.addChatMessage(new ChatComponentText(text));
    }
}
