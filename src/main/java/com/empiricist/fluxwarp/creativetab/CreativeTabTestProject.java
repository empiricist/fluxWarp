package com.empiricist.fluxwarp.creativetab;

import com.empiricist.fluxwarp.init.ModBlocks;
import com.empiricist.fluxwarp.init.ModItems;
import com.empiricist.fluxwarp.reference.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//custom creative tab
public class CreativeTabTestProject {
    //I guess you don't need to register tabs anywhere? b/c registered items added to it?
    public static final CreativeTabs TEST_PROJECT_TAB = new CreativeTabs(Reference.MOD_ID.toLowerCase()) {
        @Override
        public Item getTabIconItem() {
            return new ItemStack(ModBlocks.warpcore).getItem();// Items.dimensionAddress;//this tab's icon
        }
    };
}
