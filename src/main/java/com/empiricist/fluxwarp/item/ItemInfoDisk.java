package com.empiricist.fluxwarp.item;

import com.empiricist.fluxwarp.FluxWarp;
import com.empiricist.fluxwarp.reference.Reference;
import net.minecraftforge.fml.common.Optional;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

@Optional.Interface(iface = "dan200.computercraft.api.media.IMedia", modid = "ComputerCraft", striprefs = true)
public class ItemInfoDisk extends ItemBase implements IMedia{

    public ItemInfoDisk(){
        super();
        name = "infoDisk";
        this.setUnlocalizedName(name);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
        //list.add( "N?" + stack.hasDisplayName() + " Name:" + stack.getDisplayName() );
        list.add("Looks like it would fit in a disk drive");
        super.addInformation(stack, player, list, bool);
    }

    @Override
    public String getLabel(ItemStack stack) {
        return "Warp Info Disk";
    }

    @Override
    public boolean setLabel(ItemStack stack, String label) {
        return false;
    }

    @Override
    public String getAudioTitle(ItemStack stack) {
        if (stack.hasDisplayName()){
            return stack.getDisplayName();
        }else{
            return "Random Music";
        }
    }

    @Override
    public String getAudioRecordName(ItemStack stack) {
        if (stack.hasDisplayName()){
            return stack.getDisplayName();
        }else{
            return "music.game";
        }
    }

    @Override
    @Optional.Method(modid="ComputerCraft")
    public IMount createDataMount(ItemStack stack, World world) {
        return new ComputerCraftAPI().createResourceMount(FluxWarp.class, Reference.MOD_ID.toLowerCase(), "files");
    }
}
