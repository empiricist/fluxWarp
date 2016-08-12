package com.empiricist.teleflux.item;

import com.empiricist.teleflux.creativetab.CreativeTabTeleFlux;
import com.empiricist.teleflux.reference.Reference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//if you do this you can make making new items easier by leaving common stuff here
public class ItemBase extends Item {
    protected String name;

    public ItemBase(){
        super();
        this.setCreativeTab(CreativeTabTeleFlux.TELEFLUX_TAB);
    }

    //unlocalized name here, localized name comes from lang file
    @Override
    public String getUnlocalizedName(){
        //easy storage format: itemName
        //convert to proper format: item.[modID]:[itemName].name
        return String.format("item.%s:%s", Reference.MOD_ID.toLowerCase(), getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack){
        //same format
        return String.format("item.%s:%s", Reference.MOD_ID.toLowerCase(), getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void registerIcons(IIconRegister iconRegister){
//        //this assumes file name is same as name
//        itemIcon = iconRegister.registerIcon( getUnwrappedUnlocalizedName( this.getUnlocalizedName() ) );
//        LogHelper.warn("Registered icon for " + itemIcon.getIconName());
//    }

    protected String getUnwrappedUnlocalizedName( String unlocalizedName ){
        return unlocalizedName.substring(unlocalizedName.indexOf(".")+1);
    }

    public String getName(){
        return name;
    }


}
