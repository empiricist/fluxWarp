package com.empiricist.teleflux.block;

import com.empiricist.teleflux.creativetab.CreativeTabTeleFlux;
import com.empiricist.teleflux.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
//import net.minecraft.client.renderer.texture.IIconRegister;

public class BlockBase extends Block{
    protected String name;

    public BlockBase(Material material) {
        //material determines sound, map color, tool?, flammability, etc
        super(material);
        this.setCreativeTab(CreativeTabTeleFlux.TELEFLUX_TAB);
    }

    public BlockBase(){
        this(Material.rock);//we'll use rock as default
    }

    //unlocalized name here, localized name comes from lang file
    @Override
    public String getUnlocalizedName(){
        //easy storage format: blockName
        //convert to proper format: tile.[modID]:[blockName].name
        return String.format("tile.%s:%s", Reference.MOD_ID.toLowerCase(), getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void registerBlockIcons(IIconRegister iconRegister){
//        //this assumes file name is same as name
//        blockIcon = iconRegister.registerIcon( getUnwrappedUnlocalizedName( this.getUnlocalizedName() ) );
//        LogHelper.warn("Registered icon for " + blockIcon.getIconName());
//    }

    protected String getUnwrappedUnlocalizedName( String unlocalizedName ){
        return unlocalizedName.substring(unlocalizedName.indexOf(".")+1);
    }

    public String getName(){
        return name;
    }

}
