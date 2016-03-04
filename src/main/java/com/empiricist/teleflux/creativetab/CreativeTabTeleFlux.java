package com.empiricist.teleflux.creativetab;

import com.empiricist.teleflux.init.ModBlocks;
import com.empiricist.teleflux.reference.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//custom creative tab
public class CreativeTabTeleFlux {
    //I guess you don't need to register tabs anywhere? b/c registered items added to it?
    public static final CreativeTabs TELEFLUX_TAB = new CreativeTabs(Reference.MOD_ID.toLowerCase()) {
        @Override
        public Item getTabIconItem() {
            return new ItemStack(ModBlocks.warpcore).getItem();// Items.dimensionAddress;//this tab's icon
        }
    };
}
