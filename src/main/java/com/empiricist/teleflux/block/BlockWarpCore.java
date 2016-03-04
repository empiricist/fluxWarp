package com.empiricist.teleflux.block;

import com.empiricist.teleflux.TeleFlux;
import com.empiricist.teleflux.creativetab.CreativeTabTeleFlux;
import com.empiricist.teleflux.tileentity.TileEntityWarpCore;
import com.empiricist.teleflux.utility.LogHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
//import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.Facing;
//import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft", striprefs = true)
public class BlockWarpCore extends BlockBase implements IPeripheralProvider{
    //@SideOnly(Side.CLIENT)
    //private IIcon sideIcon;
//    @SideOnly(Side.CLIENT)
//    private IIcon topIcon;
    private String name;

    public BlockWarpCore(){
        super(Material.iron);
        this.setHardness(50f);
        name = "warpcore";
        this.setUnlocalizedName(name);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityWarpCore();
    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

//    //need these for name to work because this is not a subclass of blockbase
//    @Override
//    @SideOnly(Side.CLIENT)
//    public String getUnlocalizedName(){
//        //easy storage format: blockName
//        //convert to proper format: tile.[modID]:[blockName].name
//        return String.format("tile.%s:%s", Reference.MOD_ID.toLowerCase(), getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
//    }
//
//    protected String getUnwrappedUnlocalizedName( String unlocalizedName ){
//        return unlocalizedName.substring(unlocalizedName.indexOf(".")+1);
//    }

    //to tell tileentity it is activated
    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighbor){
        //LogHelper.info("Warp Core Neighbor Block Change");
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile instanceof TileEntityWarpCore) {
            //LogHelper.info("Found TileEntityWarpCore");
            TileEntityWarpCore warpCore = (TileEntityWarpCore)tile;
            warpCore.updateActivated(world.isBlockIndirectlyGettingPowered(pos) > 0);//checks if warp core should warp
            if (warpCore.willWarp()) {
                //LogHelper.info("Ready to warp!");
                //warpCore.warpEntities();
                world.scheduleUpdate(pos, this, 1);//schedules tick so warp occurs during block update step
                //warpCore.update2();
            }
            //warpCore.tryWarp();
            //warpCore.updateEntity();
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ){
        //world.markBlockForUpdate(x, y, z); // Makes the server call getDescriptionPacket for a full data sync
//        TileEntity t = world.getTileEntity(pos);
//        if (t != null && t instanceof TileEntityWarpCore) {
//            TileEntityWarpCore warpCore = (TileEntityWarpCore) t;
//
//            NBTTagCompound nbt = new NBTTagCompound();
//            warpCore.writeToNBT(nbt);
//            LogHelper.info((world.isRemote ? "Client " : "Server ") + " Data: " + nbt);
//        }
        if(!world.isRemote){
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && tile instanceof TileEntityWarpCore) {
                TileEntityWarpCore warpCore = (TileEntityWarpCore)tile;
                //Minecraft.getMinecraft().getNetHandler().addToSendQueue(warpCore.getDescriptionPacket());
                //world.markBlockForUpdate(x, y, z);
                //ChatHelper.sendText(player, "Energy: " + warpCore.getEnergyStored(EnumFacing.NORTH) + " / " + warpCore.getMaxEnergyStored(EnumFacing.NORTH));


                player.openGui(TeleFlux.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
        }


        return true;
    }


//    @SideOnly(Side.CLIENT)
//    public IIcon getIcon(int side, int meta){
//        //top and bottom have different texture
//        return (side == 1 || side == 0) ? this.topIcon : this.blockIcon;
//    }
//
//    @SideOnly(Side.CLIENT)
//    public void registerBlockIcons(IIconRegister reg)
//    {
//        super.registerBlockIcons(reg);
//        LogHelper.warn("Registered icon for " + blockIcon.getIconName());
//        //this.blockIcon = reg.registerIcon(Reference.MOD_ID + ":warpCore");
//        this.topIcon = reg.registerIcon(Reference.MOD_ID.toLowerCase() + ":warpCoreTop");
//        //this.innerTopIcon = p_149651_1_.registerIcon("piston_inner");
//        //this.bottomIcon = p_149651_1_.registerIcon("piston_bottom");
//    }

    @Override
    @Optional.Method(modid="ComputerCraft")
    public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side) {
        //if( !world.isRemote ){ LogHelper.info("Looking for peripheral at " + pos + " at side " + side.getName()); }
        TileEntity tile = world.getTileEntity(new BlockPos(pos));
        //if( !world.isRemote ){ LogHelper.info( tile != null  ? tile.toString() : "Tile Entity is null, making peripheral may fail"); }
        if (tile instanceof IPeripheral) {
            //LogHelper.info("    Found a peripheral");
            return (IPeripheral)tile;
        }else{
            //LogHelper.info("    Not a peripheral");
            return null;
        }
    }

    public String getName(){
        return name;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand){
        LogHelper.info("Ticking warpcore");
        TileEntity t = world.getTileEntity(pos);
        if (t != null && t instanceof TileEntityWarpCore) {
            TileEntityWarpCore warpCore = (TileEntityWarpCore) t;
            //warpCore.warpEntities();
            warpCore.tryWarp();//calls warp
        }
    }

    /*--
    @Override
    public void onNeighborBlockChange(World world, int xCoord, int yCoord, int zCoord, Block block) {
        super.onNeighborBlockChange(world, xCoord, yCoord, zCoord, block);
        world.setBlockToAir(xCoord, yCoord, zCoord);
    }
    --*/
}
