package com.empiricist.fluxwarp.block;

import com.empiricist.fluxwarp.creativetab.CreativeTabTestProject;
import com.empiricist.fluxwarp.reference.Reference;
import com.empiricist.fluxwarp.tileentity.TileEntityDimensionDatabase;
import com.empiricist.fluxwarp.tileentity.TileEntityWarpCore;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockDimensionDatabase extends BlockContainer {
    public BlockDimensionDatabase(){
        super(Material.iron);
        this.setHardness(50f);
        this.setBlockName("dimensionDatabase");
        this.setCreativeTab(CreativeTabTestProject.TEST_PROJECT_TAB);//need these for tab to work because this is not a subclass of blockbase
        //this.setBlockTextureName("fluxWarp:warpcore");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityDimensionDatabase();
    }

    //need these for name to work because this is not a subclass of blockbase
    @Override
    public String getUnlocalizedName(){
        //easy storage format: blockName
        //convert to proper format: tile.[modID]:[blockName].name
        return String.format("tile.%s:%s", Reference.MOD_ID.toLowerCase(), getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

    protected String getUnwrappedUnlocalizedName( String unlocalizedName ){
        return unlocalizedName.substring(unlocalizedName.indexOf(".")+1);
    }

}
