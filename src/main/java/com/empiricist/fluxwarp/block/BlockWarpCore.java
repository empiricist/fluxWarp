package com.empiricist.fluxwarp.block;

import com.empiricist.fluxwarp.FluxWarp;
import com.empiricist.fluxwarp.creativetab.CreativeTabTestProject;
import com.empiricist.fluxwarp.reference.Reference;
import com.empiricist.fluxwarp.tileentity.TileEntityWarpCore;
import com.empiricist.fluxwarp.utility.ChatHelper;
import com.empiricist.fluxwarp.utility.LogHelper;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft", striprefs = true)
public class BlockWarpCore extends BlockContainer implements IPeripheralProvider{
    //@SideOnly(Side.CLIENT)
    //private IIcon sideIcon;
    @SideOnly(Side.CLIENT)
    private IIcon topIcon;

    public BlockWarpCore(){
        super(Material.iron);
        this.setHardness(50f);
        this.setBlockName("warpcore");
        this.setCreativeTab(CreativeTabTestProject.TEST_PROJECT_TAB);//need these for tab to work because this is not a subclass of blockbase
        this.setBlockTextureName("fluxWarp:warpcore");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityWarpCore();
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

    //to tell tileentity it is activated
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block){
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile != null && tile instanceof TileEntityWarpCore) {
            TileEntityWarpCore warpCore = (TileEntityWarpCore)tile;
            warpCore.signal = world.isBlockIndirectlyGettingPowered(x, y, z);
        }
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_){
        //world.markBlockForUpdate(x, y, z); // Makes the server call getDescriptionPacket for a full data sync
        TileEntity t = world.getTileEntity(x, y, z);
        if (t != null && t instanceof TileEntityWarpCore) {
            TileEntityWarpCore warpCore = (TileEntityWarpCore) t;

            NBTTagCompound nbt = new NBTTagCompound();
            warpCore.writeToNBT(nbt);
            LogHelper.info((world.isRemote ? "Client " : "Server ") + " Data: " + nbt);
        }
        if(!world.isRemote){
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile != null && tile instanceof TileEntityWarpCore) {
                TileEntityWarpCore warpCore = (TileEntityWarpCore)tile;
                //Minecraft.getMinecraft().getNetHandler().addToSendQueue(warpCore.getDescriptionPacket());
                //world.markBlockForUpdate(x, y, z);
                //ChatHelper.sendText(player, "Energy: " + warpCore.getEnergyStored(ForgeDirection.NORTH) + " / " + warpCore.getMaxEnergyStored(ForgeDirection.NORTH));


                player.openGui(FluxWarp.instance, 0, world, x, y, z);
            }
        }


        return true;
    }


    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta){
        //top and bottom have different texture
        return (side == 1 || side == 0) ? this.topIcon : this.blockIcon;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg)
    {
        super.registerBlockIcons(reg);
        //this.blockIcon = reg.registerIcon(Reference.MOD_ID + ":warpCore");
        this.topIcon = reg.registerIcon(Reference.MOD_ID + ":warpCoreTop");
        //this.innerTopIcon = p_149651_1_.registerIcon("piston_inner");
        //this.bottomIcon = p_149651_1_.registerIcon("piston_bottom");
    }

    @Override
    @Optional.Method(modid="ComputerCraft")
    public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
        if( !world.isRemote ){ LogHelper.info("Looking for peripheral at " + x + " " + y + " " + z + " at side " + side); }
        TileEntity tile = world.getTileEntity(x, y, z);
        if( !world.isRemote ){ LogHelper.info( tile != null  ? tile.toString() : "Tile Entity is null, making peripheral may fail"); }
        if (tile instanceof IPeripheral) {
            LogHelper.info("    Found a peripheral");
            return (IPeripheral)tile;
        }else{
            LogHelper.info("    Not a peripheral");
            return null;
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
