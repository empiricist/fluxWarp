package com.empiricist.fluxwarp.block;

import com.empiricist.fluxwarp.FluxWarp;
import com.empiricist.fluxwarp.api.IDimensionPermissionBlock;
import com.empiricist.fluxwarp.api.IDimensionPermissionItem;
import com.empiricist.fluxwarp.creativetab.CreativeTabTestProject;
import com.empiricist.fluxwarp.init.ModBlocks;
import com.empiricist.fluxwarp.item.ItemDimensionAddress;
import com.empiricist.fluxwarp.reference.Reference;
import com.empiricist.fluxwarp.tileentity.TileEntityDimensionDatabase;
import com.empiricist.fluxwarp.tileentity.TileEntityWarpCore;
import com.empiricist.fluxwarp.utility.LogHelper;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class BlockDimensionDatabase extends BlockContainer implements IDimensionPermissionBlock{
    public BlockDimensionDatabase(){
        super(Material.iron);
        this.setHardness(50f);
        this.setUnlocalizedName("dimensionDatabase");
        this.setCreativeTab(CreativeTabTestProject.TEST_PROJECT_TAB);//need these for tab to work because this is not a subclass of blockbase
        //this.setBlockTextureName("fluxWarp:warpcore");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityDimensionDatabase();
    }

    //transfer dimension id from item to block
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ){
        ItemStack stack = player.getHeldItem();
        if( stack != null && stack.getItem() instanceof ItemDimensionAddress ){
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && tile instanceof TileEntityDimensionDatabase) {
                TileEntityDimensionDatabase database = (TileEntityDimensionDatabase) tile;

                NBTTagCompound tag = new NBTTagCompound();
                if (stack.hasTagCompound()) {
                    tag = stack.getTagCompound();
                }
                if(tag.hasKey("DimID")){//sneaking deletes ID, if it exists
                    int id = tag.getInteger("DimID");
                    database.addDimension( id );
                    if(!world.isRemote){ player.addChatComponentMessage(new ChatComponentText( "Added Dimension " + id )); }
                }
            }
        }else{
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && tile instanceof TileEntityDimensionDatabase) {
                TileEntityDimensionDatabase database = (TileEntityDimensionDatabase) tile;
                if(!world.isRemote){ player.addChatComponentMessage(new ChatComponentText( "Dimensions: " + database.dimensions.toString() ) ) ; }
            }
        }

        return true;
    }

    @Override
    public boolean canTravelTo(World world, BlockPos pos, int dimensionID) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile instanceof TileEntityDimensionDatabase) {
            TileEntityDimensionDatabase database = (TileEntityDimensionDatabase) tile;
            return database.dimensions.contains(new Integer(dimensionID));
        }
        return false;
    }


    //save dimension data to item dropped
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        //LogHelper.info("Breaking database block");
        if (te != null && te instanceof TileEntityDimensionDatabase) {
            //LogHelper.info("Found TEDatabase");
            TileEntityDimensionDatabase database = (TileEntityDimensionDatabase) te;
            ItemStack stack = new ItemStack(ModBlocks.dimensionDatabase);
            NBTTagCompound tag = new NBTTagCompound();
            if( stack.hasTagCompound() ){
                LogHelper.info("Stack has Tag " + tag);
                tag = stack.getTagCompound();
            }
            LogHelper.info("Old Tag " + tag);
            tag.setIntArray("DimIDs", Ints.toArray(database.dimensions));
            LogHelper.info("Modified Tag " + tag);
            stack.setTagCompound(tag);
            LogHelper.info("Stack Tag " + stack.getTagCompound());

            EntityItem droppedItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);

            droppedItem.motionX = -0.5f + world.rand.nextFloat();
            droppedItem.motionY = 4 + world.rand.nextFloat();
            droppedItem.motionZ = -0.5f + world.rand.nextFloat();

            world.spawnEntityInWorld(droppedItem);
        }else{
            //LogHelper.info("Null or not TEDatabase");
            super.breakBlock(world, pos, state);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        if(!world.isRemote){
            super.onBlockPlacedBy(world, pos, state, entity, stack);

            TileEntity te = world.getTileEntity(pos);
            if (te != null && te instanceof TileEntityDimensionDatabase) {
                TileEntityDimensionDatabase database = (TileEntityDimensionDatabase) te;

                NBTTagCompound tag = new NBTTagCompound();
                if( stack.hasTagCompound() ){

                    tag = stack.getTagCompound();
                }else{
                    LogHelper.info("Stack has no Tag ");
                }
                LogHelper.info("Stack Tag " + tag);
                if(tag.hasKey("DimIDs")){
                    LogHelper.info("Adding IDs");
                    database.dimensions = Lists.newArrayList(Arrays.asList(ArrayUtils.toObject(tag.getIntArray("DimIDs"))));
                }
            }else {
                LogHelper.info("No TE Found");

            }
        }

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
