package com.empiricist.teleflux.client.Settings;

import com.empiricist.teleflux.reference.Names;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

//currently keys are disabled in main class preinit method
//we will store our keybindings here
public class Keybindings {
    //needs name, default key int value, and category name
    public static KeyBinding charge = new KeyBinding(Names.Keys.CHARGE, Keyboard.KEY_C, Names.Keys.CATEGORY);
    public static KeyBinding release = new KeyBinding(Names.Keys.RELEASE, Keyboard.KEY_R, Names.Keys.CATEGORY);
}
