package com.empiricist.fluxwarp.item;

import com.empiricist.fluxwarp.api.IDimensionPermissionItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemCreativeAddress extends ItemBase implements IDimensionPermissionItem {

    public ItemCreativeAddress(){
        super();
        name = "creativeAddress";
        this.setUnlocalizedName(name);
    }

    @Override
    public boolean canTravelTo(ItemStack stack, int dimensionID) {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
        list.add("DIM: All of them");
        list.add("ID : All of them");
    }
}
