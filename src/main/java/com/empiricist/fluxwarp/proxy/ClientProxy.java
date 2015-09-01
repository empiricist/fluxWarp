package com.empiricist.fluxwarp.proxy;

import com.empiricist.fluxwarp.client.Settings.Keybindings;
import cpw.mods.fml.client.registry.ClientRegistry;


public class ClientProxy extends CommonProxy{
    @Override
    public void registerKeyBindings(){
        //currently keys are disabled in main class preinit method
        ClientRegistry.registerKeyBinding(Keybindings.charge);
        ClientRegistry.registerKeyBinding(Keybindings.release);
    }
}
