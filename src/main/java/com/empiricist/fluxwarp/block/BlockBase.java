package com.empiricist.fluxwarp.block;

import com.empiricist.fluxwarp.creativetab.CreativeTabTestProject;
import com.empiricist.fluxwarp.reference.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;

public class BlockBase extends Block{

    public BlockBase(Material material) {
        //material determines sound, map color, tool?, flammability, etc
        super(material);
        this.setCreativeTab(CreativeTabTestProject.TEST_PROJECT_TAB);
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

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister){
        //this assumes file name is same as name
        blockIcon = iconRegister.registerIcon( getUnwrappedUnlocalizedName( this.getUnlocalizedName() ) );
    }

    protected String getUnwrappedUnlocalizedName( String unlocalizedName ){
        return unlocalizedName.substring(unlocalizedName.indexOf(".")+1);
    }

}
