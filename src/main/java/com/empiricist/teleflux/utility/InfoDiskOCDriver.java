package com.empiricist.teleflux.utility;

import com.empiricist.teleflux.init.ModItems;
import li.cil.oc.api.FileSystem;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.prefab.DriverItem", modid = "OpenComputers", striprefs = true)
public class InfoDiskOCDriver extends DriverItem{

    public InfoDiskOCDriver() {
        super(new ItemStack(ModItems.infoDisk));//only exists if OC is loaded, but super call must be first line
    }

//
//    public static InfoDiskOCDriver getDriverSafely(){
//        if(Loader.isModLoaded("OpenComputers")){
//            return new InfoDiskOCDriver();
//        }else{
//            return new InfoDiskOCDriver(false);
//        }
//    }

    @Override
    @Optional.Method(modid="OpenComputers")
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
        /*
        li.cil.oc.api.fs.FileSystem fs = FileSystem.fromClass( this.getClass(), "teleflux", "files");
        if(fs == null){
            LogHelper.info("FileSystem made from class is null because path cannot be located");
        }
        fs = FileSystem.asReadOnly( fs );
        if(fs == null){
            LogHelper.info("FileSystem is null after setting to readonly");
        }
        ManagedEnvironment mge = FileSystem.asManagedEnvironment( fs, "infoDisk", host, "opencomputers:floppy_access");//for resources/assets/teleflux/files inside the mod .jar (read only)
        if(mge == null){
            LogHelper.info("ManagedEnvironment is null after creating from FileSystem");
        }else if(!(mge.node() instanceof Component)){
            LogHelper.info("ManagedEnvironment's node is not a Component");//this one
        }
        return mge;
        */
        return FileSystem.asManagedEnvironment( FileSystem.asReadOnly( FileSystem.fromClass( this.getClass(), "teleflux", "files")), "infoDisk", host, "opencomputers:floppy_access");//node that lets you access resources/assets/teleflux/files inside the mod .jar (read only)
    }

    @Override
    @Optional.Method(modid="OpenComputers")
    public String slot(ItemStack stack) {
        return Slot.Floppy;
    }
}
