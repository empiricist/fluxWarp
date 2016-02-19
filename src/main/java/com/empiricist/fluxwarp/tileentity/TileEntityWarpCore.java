package com.empiricist.fluxwarp.tileentity;



import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import com.empiricist.fluxwarp.api.IDimensionPermissionBlock;
import com.empiricist.fluxwarp.handler.ConfigurationHandler;
import com.empiricist.fluxwarp.api.IDimensionPermissionItem;
import com.empiricist.fluxwarp.utility.FluxWarpEnergyStorage;
import com.empiricist.fluxwarp.utility.LogHelper;
import com.empiricist.fluxwarp.utility.ParseHelper;
import com.empiricist.fluxwarp.utility.TeleportHelper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.*;
import net.minecraftforge.common.DimensionManager;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Optional.InterfaceList({
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft", striprefs = true),
        @Optional.Interface(iface = "cofh.api.energy.IEnergyReceiver", modid = "CoFHAPI|energy", striprefs = true)
})
public class TileEntityWarpCore extends TileEntity implements ITickable, IInventory, IPeripheral, IEnergyReceiver {

    private int xPlus;
    private int yPlus;
    private int zPlus;
    private int xMinus;
    private int yMinus;
    private int zMinus;
    private boolean doWarp;
    public boolean signal;
    private boolean signalOn;

    private int dx;
    private int dy;
    private int dz;
    private int destDim;

    public EnergyStorage energyStorage;

    public TileEntityWarpCore() {
        super();
        xPlus = 1;
        yPlus = 1;
        zPlus = 1;
        xMinus = 1;
        yMinus = 1;
        zMinus = 1;
        doWarp = false; //??
        signal = true;
        signalOn = true;

        dx = 0;
        dy = 0;
        dz = 0;
        //destDim = worldObj.provider.dimensionId; //causes NPE
        energyStorage = new EnergyStorage(ConfigurationHandler.coreEnergyStorage, 1000, ConfigurationHandler.coreEnergyStorage);//capacity, receive, extract
    }

    //what is this method for?
    public boolean isIdle() {
        return !signalOn;
    }



    //@Override
    public void update() {

        if (!worldObj.isRemote) {

            //LogHelper.info("Did it work?: " + (this instanceof IPeripheral));
            if (doWarp || (signal && !signalOn)) {
                LogHelper.info("doWarp is " + doWarp + ", Signal stuff is " + (signal && !signalOn));

                signalOn = signal;
                doWarp = false;
                getDirections();

                warpEntities();//must be before blocks as it is now

                LogHelper.info("Activating Warp Drive, " + pos);
                if( dx == 0 && dy == 0 && dz == 0 && destDim == worldObj.provider.getDimensionId()){
                    LogHelper.info("Destination is the same as origin, quitting warp");
                    worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "note.bd", 1, 1);
                    return;
                }

                if( (xPlus+xMinus+1)*(yPlus+yMinus+1)*(zPlus+zMinus+1) > ConfigurationHandler.maxSize ){
                    LogHelper.info("Volume is too large!");
                    worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "note.hat", 1, 1);
                    return;
                }

                if( !DimensionManager.isDimensionRegistered(destDim) ){
                    LogHelper.info("Dimension " + destDim + " is not registered!");
                    worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "note.bassattack", 1, 1);
                    return;
                }

                if( !hasPermissionForDimension(destDim) ){
                    LogHelper.info("Dimension " + destDim + " address not found!");
                    worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "note.harp", 1, 1);
                    return;
                }

                World world2 = MinecraftServer.getServer().worldServerForDimension(destDim);//seems to not have loading problems like DimensionManager method
                /*
                World world2 = DimensionManager.getWorld(destDim);//sometimes this fails (if destination dimension is unloaded?)
                while (world2 == null){
                    LogHelper.info("Dimension " + destDim + " failed to load!");
                    DimensionManager.initDimension(destDim);//this seems to fix it
                    world2 = DimensionManager.getWorld(destDim);

                    Integer[] worlds = DimensionManager.getIDs();
                    LogHelper.info("There are " + worlds.length + " worlds available");
                    for( int i = 0; i < worlds.length; i++){
                        System.out.print("World " + worlds[i] + ", ");
                    }
                    LogHelper.info();
                    //return;
                }
                --*/

                //try to use base energy cost
                if(worldObj == world2){
                    //LogHelper.info("Same dimension");
                    if( !tryToUseEnergy(ConfigurationHandler.baseCost) ){
                        LogHelper.info("Out of energy for warp!");
                        worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "mob.enderdragon.hit", 1, 1);
                        return;
                    }
                }else{
                    //LogHelper.info("Different dimension");
                    if( !tryToUseEnergy(ConfigurationHandler.dimensionCost) ){
                        LogHelper.info("Out of energy for warp!");
                        worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "mob.enderdragon.hit", 1, 1);
                        return;
                    }
                }
                //try to use distance energy cost
                if(ConfigurationHandler.distanceCost != 0){
                    double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
                    LogHelper.info("Distance is " + distance);
                    if( !tryToUseEnergy(ConfigurationHandler.distanceCost * ((int)distance) ) ){
                        LogHelper.info("Out of energy for warp!");
                        worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "mob.enderdragon.hit", 1, 1);
                        return;
                    }
                }



                LogHelper.info("Destination World: " + world2.toString() + ", " + world2.provider.getDimensionId());//make sure it worked
                double posMultiplier = worldObj.provider.getMovementFactor() / world2.provider.getMovementFactor();//for the nether and stuff
                int newXCen = (int)((pos.getX()) * posMultiplier) + dx;
                int newYCen = pos.getY() + dy;
                int newZCen = (int)((pos.getZ()) * posMultiplier) + dz;
                BlockPos newPos = new BlockPos(newXCen, newYCen, newZCen);

                //mathemagic to make sure region does not overwrite itself if source and destination regions overlap
                //however, it may cause block updates before it finishes and pop off levers etc
                int iInc = 1;
                int iBeg = 0, jBeg = 0, kBeg = 0;
                int iEnd = 0, jEnd = 0, kEnd = 0;
                int ix = 0, iy = 0, iz = 0;
                int jx = 0, jy = 0, jz = 0;
                int kx = 0, ky = 0, kz = 0;
                if(dx != 0){
                    if(dx > 0){
                        iInc = -1;
                        iBeg = pos.getX()+xPlus;
                        iEnd = pos.getX()-xMinus-1;
                    }else{
                        iBeg = pos.getX()-xMinus;
                        iEnd = pos.getX()+xPlus+1;
                    }
                    ix = 1;
                    jBeg = pos.getY()-yMinus;
                    jEnd = pos.getY()+yPlus;
                    jy = 1;
                    kBeg = pos.getZ()-zMinus;
                    kEnd = pos.getZ()+zPlus;
                    kz = 1;
                }else if(dy != 0){
                    if(dy > 0){
                        iInc = -1;
                        iBeg = pos.getY()+yPlus;
                        iEnd = pos.getY()-yMinus-1;
                        //iy = 1;
                    }else{
                        iBeg = pos.getY()-yMinus;
                        iEnd = pos.getY()+yPlus+1;
                        //iy = -1;
                    }
                    iy = 1;
                    jBeg = pos.getX()-xMinus;
                    jEnd = pos.getX()+xPlus;
                    jx = 1;
                    kBeg = pos.getZ()-zMinus;
                    kEnd = pos.getZ()+zPlus;
                    kz = 1;
                }else{
                    if(dz > 0){
                        iInc = -1;
                        iBeg = pos.getZ()+zPlus;
                        iEnd = pos.getZ()-zMinus-1;
                        //iz = 1;
                    }else{
                        iBeg = pos.getZ()-zMinus;
                        iEnd = pos.getZ()+zPlus+1;
                        //iz = -1;
                    }
                    iz = 1;
                    jBeg = pos.getX()-xMinus;
                    jEnd = pos.getX()+xPlus;
                    jx = 1;
                    kBeg = pos.getY()-yMinus;
                    kEnd = pos.getY()+yPlus;
                    ky = 1;
                }
//                LogHelper.info("i: " + ix + " " + iy + " " + iz + " : " + iBeg + " " + iEnd);
//                LogHelper.info("j: " + jx + " " + jy + " " + jz + " : " + jBeg + " " + jEnd);
//                LogHelper.info("k: " + kx + " " + ky + " " + kz + " : " + kBeg + " " + kEnd);
                //end of magic

                boolean isWarpSuccessful = true;

                BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos(0,0,0);
                //iterate through a volume of blocks to move
                for(int i = iBeg; i != iEnd; i += iInc){
                    for(int j = jBeg; j <= jEnd; j++){
                        for(int k = kBeg; k <= kEnd; k++){

                            //wait this is also magic
                            int x = i*ix + j*jx + k*kx;
                            int y = i*iy + j*jy + k*ky;
                            int z = i*iz + j*jz + k*kz;
                            position.set(x, y, z);
                            isWarpSuccessful &= TeleportHelper.moveBlockWorld(worldObj, world2, position, position.add( - pos.getX() + newXCen, - pos.getY() + newYCen, - pos.getZ() + newZCen ), energyStorage);//supposedly this works for booleans

                        }
                    }
                }


                //schedule block updates around the outside of the bounding box
                //LogHelper.info("Scheduling block updates at x " + (xCoord-xMinus) + "to" + (xCoord+xPlus) + " y " + (yCoord-yMinus) + "to" + (yCoord+yPlus) + " z " + (zCoord-zMinus-1) + " and " + (zCoord+zPlus+1));
                for(int i = -xMinus; i <= +xPlus; i++){
                    for(int j = -yMinus; j <= +yPlus; j++){
//                        worldObj.scheduleBlockUpdate(xCoord+i, yCoord+j, zCoord-zMinus-1, worldObj.getBlock(xCoord+i, yCoord+j, zCoord-zMinus-1), 1);
//                        worldObj.scheduleBlockUpdate(xCoord+i, yCoord+j, zCoord+zPlus+1, worldObj.getBlock(xCoord+i, yCoord+j, zCoord+zMinus+1), 1);
//                        world2.scheduleBlockUpdate(newXCen+i, newYCen+j, newZCen-zMinus-1, world2.getBlock(newXCen+i, newYCen+j, newZCen-zMinus-1), 1);
//                        world2.scheduleBlockUpdate(newXCen+i, newYCen+j, newZCen+zPlus+1, world2.getBlock(newXCen+i, newYCen+j, newZCen+zPlus+1), 1);

                        worldObj.notifyBlockOfStateChange( pos.add( i, j, -zMinus-1), worldObj.getBlockState(pos.add( i, j, -zMinus-1)).getBlock() );
                        worldObj.notifyBlockOfStateChange( pos.add( i, j, zPlus+1),   worldObj.getBlockState(pos.add( i, j, zPlus+1)).getBlock() );
                        world2.notifyBlockOfStateChange( newPos.add(i, j, -zMinus-1), world2.getBlockState(newPos.add(i, j, -zMinus-1)).getBlock());
                        world2.notifyBlockOfStateChange( newPos.add(i, j, zPlus+1), world2.getBlockState(newPos.add(i, j, zPlus+1)).getBlock());
                    }
                }
                //LogHelper.info("Scheduling block updates at x " + (xCoord-xMinus) + "to" + (xCoord+xPlus) + " y " + (yCoord-yMinus-1) + " and " + (yCoord+yPlus+1) + " z " + (zCoord-zMinus) + "to" + (zCoord+zPlus));
                for(int i = -xMinus; i <= +xPlus; i++){
                    for(int j = -zMinus; j <= +zPlus; j++){
//                        worldObj.scheduleBlockUpdate(xCoord+i, yCoord-yMinus-1, zCoord+j, worldObj.getBlock(xCoord+i, yCoord-yMinus-1, zCoord+j), 1);
//                        worldObj.scheduleBlockUpdate(xCoord+i, yCoord+yPlus+1, zCoord+j, worldObj.getBlock(xCoord+i, yCoord+yPlus+1, zCoord+j), 1);
//                        world2.scheduleBlockUpdate(newXCen+i, newYCen-yMinus-1, newZCen+i, world2.getBlock(newXCen+i, newYCen-yMinus-1, newZCen+i), 1);
//                        world2.scheduleBlockUpdate(newXCen+i, newYCen+yPlus+1, newZCen+i, world2.getBlock(newXCen+i, newYCen+yPlus+1, newZCen+i), 1);

                        worldObj.notifyBlockOfStateChange( pos.add( i, -yMinus-1, j), worldObj.getBlockState( pos.add( i, -yMinus-1, j) ).getBlock());
                        worldObj.notifyBlockOfStateChange( pos.add( i, yPlus+1, j), worldObj.getBlockState( pos.add( i, yPlus+1, j) ).getBlock());
                        world2.notifyBlockOfStateChange( newPos.add( i, -yMinus-1, i), world2.getBlockState(newPos.add( i, -yMinus-1, i)).getBlock());
                        world2.notifyBlockOfStateChange( newPos.add(i, yPlus+1, i), world2.getBlockState( newPos.add(i, yPlus+1, i) ).getBlock());
                    }
                }
                //LogHelper.info("Scheduling block updates at x " + (xCoord-xMinus-1) + " and " + (xCoord+xPlus+1) + " y " + (yCoord-yMinus) + "to" + (yCoord+yPlus) + " z " + (zCoord-zMinus) + "to" + (zCoord+zPlus));
                for(int i = -yMinus; i <= +yPlus; i++){
                    for(int j = -zMinus; j <= +zPlus; j++){
//                        worldObj.scheduleBlockUpdate(xCoord-xMinus-1, yCoord+i, zCoord+j, worldObj.getBlock(xCoord-xMinus-1, yCoord+i, zCoord+j), 1);
//                        worldObj.scheduleBlockUpdate(xCoord+xPlus+1, yCoord+i, zCoord+j, worldObj.getBlock(xCoord+xPlus+1, yCoord+i, zCoord+j), 1);
//                        world2.scheduleBlockUpdate(newXCen-xMinus-1, newYCen+i, newZCen+j, world2.getBlock(newXCen-xMinus-1, newYCen+i, newZCen+j), 1);
//                        world2.scheduleBlockUpdate(newXCen+xPlus+1, newYCen+i, newZCen+j, world2.getBlock(newXCen+xPlus+1, newYCen+i, newZCen+j), 1);

                        worldObj.notifyBlockOfStateChange( pos.add( -xMinus-1, i, j), worldObj.getBlockState( pos.add( -xMinus-1, i, j) ).getBlock());
                        worldObj.notifyBlockOfStateChange( pos.add( xPlus+1, i, j), worldObj.getBlockState( pos.add( xPlus+1, i, j) ).getBlock());
                        world2.notifyBlockOfStateChange( newPos.add( -xMinus-1, i, j), world2.getBlockState( newPos.add( -xMinus-1, i, j) ).getBlock());
                        world2.notifyBlockOfStateChange( newPos.add( xPlus+1, i, j), world2.getBlockState( newPos.add( xPlus+1, i, j) ).getBlock());
                    }
                }


                LogHelper.info("Was warp successful for all blocks: " + isWarpSuccessful);
                if( !isWarpSuccessful ){
                    worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "mob.enderdragon.hit", 1, 1);
                }

                if(isWarpSuccessful) {
                }

                worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "random.fizz", 1, 1);
                world2.playSoundEffect(newXCen, newYCen, newZCen, "ambient.weather.thunder", 1, 1);

                LogHelper.info("Finished warp!");



            }

            signalOn = signal;

        }
    }

    public void warpEntities(){
        LogHelper.warn("Warping Entities " + (worldObj.isRemote?"Client":"Server"));
        if(!worldObj.isRemote){


            if (!DimensionManager.isDimensionRegistered(destDim)) {
                LogHelper.info("Dimension " + destDim + " is not registered!");
                worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "note.bassattack", 1, 1);
                return;
            }

            if (!hasPermissionForDimension(destDim)) {
                LogHelper.info("Dimension " + destDim + " address not found!");
                worldObj.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "note.harp", 1, 1);
                return;
            }

            World world2 = MinecraftServer.getServer().worldServerForDimension(destDim);
            double posMultiplier = worldObj.provider.getMovementFactor() / world2.provider.getMovementFactor();//for the nether and stuff
            int newXCen = (int) ((pos.getX()) * posMultiplier) + dx;
            int newYCen = pos.getY() + dy;
            int newZCen = (int) ((pos.getZ()) * posMultiplier) + dz;


            //move entities within volume too (but only if ship also moved, or does not exist)

            List<Entity> entitiesInVolume = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.getX() - xMinus, pos.getY() - yMinus, pos.getZ() - zMinus, pos.getX() + xPlus + 1, pos.getY() + yPlus + 1, pos.getZ() + zPlus + 1));

            //Set<Entity> entitySet = new HashSet<Entity>();
            //entitySet.addAll(entitiesInVolume);
            LogHelper.info("    Found Entities: " + entitiesInVolume.toString());
            //LogHelper.info("    Deduplicated???: " + entitySet.toString());

            for (Entity entity : entitiesInVolume) {
                //LogHelper.info("Found Entity: " + entity.toString());

                if (tryToUseEnergy(ConfigurationHandler.entityCost)) {

                    //entity should stay at same relative position within ship, even if movement factor is not 1
                    double newX = entity.posX - pos.getX() + newXCen;
                    double newY = entity.posY - pos.getY() + newYCen;
                    double newZ = entity.posZ - pos.getZ() + newZCen;

                    TeleportHelper.moveEntity(worldObj, world2, entity, newX, newY, newZ);

                } else {
                    LogHelper.info("Out of energy for entity!");
                }
            }
        }

    }



    public boolean hasPermissionForDimension( int dimID ){
        if(dimID == worldObj.provider.getDimensionId()){//don't need permission to move within same dimension
            return true;
        }else if(ConfigurationHandler.AlwaysAllowedDimensions.contains(dimID + "")){
            return true;
        }
        TileEntity te;
        Block b;
        for( EnumFacing dir : EnumFacing.VALUES ){

            b = worldObj.getBlockState( pos.offset(dir) ).getBlock();
            if(b != null && b instanceof IDimensionPermissionBlock){
                IDimensionPermissionBlock perm = (IDimensionPermissionBlock) b;
                if( perm.canTravelTo(worldObj, pos.offset(dir), dimID) ){
                    return true;
                }
            }
            te = worldObj.getTileEntity( pos.offset(dir) );
            if (te != null && te instanceof IInventory){
                IInventory inv = (IInventory) te;
                ItemStack stack;
                for (int i = 0; i < inv.getSizeInventory(); i++){
                    stack = inv.getStackInSlot(i);
                    if( stack != null && stack.getItem() instanceof IDimensionPermissionItem){
                        IDimensionPermissionItem dimPerm = (IDimensionPermissionItem) stack.getItem();
                        if(dimPerm.canTravelTo(stack, dimID)){
                            return true;
                        }
                    }
                }
            }

        }
        return false;
    }

    /*--
    public void updateActivated(boolean redstonePowered){
        if ( !signalOn && redstonePowered){
            signalOn = true;
            doWarp();
            return;
        }else {
            signalOn = redstonePowered;
        }
    }
    --*/

    private void getDirections(){
        TileEntity tile = worldObj.getTileEntity( pos.up() );

        if (tile != null && tile instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign) tile;

            IChatComponent[] text = sign.signText;
            dx = ParseHelper.safeReadInt(text[0].getUnformattedText(), 0);
            dy = ParseHelper.safeReadInt(text[1].getUnformattedText(), 0);
            dz = ParseHelper.safeReadInt(text[2].getUnformattedText(), 0);
            destDim = ParseHelper.safeReadInt(text[3].getUnformattedText(), worldObj.provider.getDimensionId() );
            LogHelper.info("Found coords dx:" + dx + " dy:" + dy + " dz:" + dz + " dim:" + destDim);
        }
    }


    public void setDoWarp(boolean newWarp){
        doWarp = newWarp;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setInteger("xPlus", (int)xPlus);
        compound.setInteger("yPlus", (int)yPlus);
        compound.setInteger("zPlus", (int)zPlus);
        compound.setInteger("xMinus", (int)xMinus);
        compound.setInteger("yMinus", (int)yMinus);
        compound.setInteger("zMinus", (int)zMinus);

        compound.setBoolean("doWarp", (boolean)doWarp);
        compound.setBoolean("signal", (boolean)signal);
        compound.setBoolean("signalOn", (boolean)signalOn);
        compound.setInteger("dx", (int)dx);
        compound.setInteger("dy", (int)dy);
        compound.setInteger("dz", (int)dz);
        compound.setInteger("destDim", (int)destDim);

        energyStorage.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        xPlus = compound.getInteger("xPlus");
        yPlus = compound.getInteger("yPlus");
        zPlus = compound.getInteger("zPlus");
        xMinus = compound.getInteger("xMinus");
        yMinus = compound.getInteger("yMinus");
        zMinus = compound.getInteger("zMinus");

        doWarp = compound.getBoolean("doWarp");
        signal = compound.getBoolean("signal");
        signalOn = compound.getBoolean("signalOn");
        dx = compound.getInteger("dx");
        dy = compound.getInteger("dy");
        dz = compound.getInteger("dz");
        destDim = compound.getInteger("destDim");

        energyStorage.readFromNBT(compound);
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound syncData = new NBTTagCompound();
        this.writeToNBT(syncData);
        return new S35PacketUpdateTileEntity(this.pos, 1, syncData);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());
    }


    //computercraft peripheral methods
    @Override
    public String getType() {
        return "fluxwarpcore";
    }

    @Override
    public String[] getMethodNames() {
        return new String[] {
            "setBounds", "getBounds",
            "setWarpVector", "getWarpVector",
            "setDimension", "getDimension",
            "getEnergy", "getMaxEnergy",
            "warp",
            "listMethods"
        };
    }

    @Override
    @Optional.Method(modid="ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        switch( method ) {
            case 0: //setBounds
                if (arguments.length >= 6) {
                    xPlus = ParseHelper.safeReadInt(ParseHelper.safeReadString(arguments[0]), 1);
                    xMinus = ParseHelper.safeReadInt(ParseHelper.safeReadString(arguments[1]), 1);
                    yPlus = ParseHelper.safeReadInt(ParseHelper.safeReadString(arguments[2]), 1);
                    yMinus = ParseHelper.safeReadInt(ParseHelper.safeReadString(arguments[3]), 1);
                    zPlus = ParseHelper.safeReadInt(ParseHelper.safeReadString(arguments[4]), 1);
                    zMinus = ParseHelper.safeReadInt(ParseHelper.safeReadString(arguments[5]), 1);
                } else {
                    //return new Object[]{-1, "Not enough arguments, 6 needed: x+, x-, y+, y-, z+, z-"};
                    throw new LuaException("Not enough arguments, 6 needed: x+, x-, y+, y-, z+, z-");
                }
                break;
            case 1: //getBounds
                return new Object[]{xPlus, xMinus, yPlus, yMinus, zPlus, zMinus};
            //break;
            case 2: //setWarpVector
                if (arguments.length >= 3) {
                    dx = ParseHelper.safeReadInt(ParseHelper.safeReadString(arguments[0]), 0);
                    dy = ParseHelper.safeReadInt(ParseHelper.safeReadString(arguments[1]), 0);
                    dz = ParseHelper.safeReadInt(ParseHelper.safeReadString(arguments[2]), 0);
                } else {
                    //return new Object[]{-1, "Not enough arguments, 3 needed: dx, dy, dz"};
                    throw new LuaException("Not enough arguments, 3 needed: dx, dy, dz");
                }
                break;
            case 3: //getWarpVector
                return new Object[]{dx, dy, dz};
            case 4: //setDimension
                if (arguments.length >= 1) {
                    destDim = ParseHelper.safeReadInt(ParseHelper.safeReadString(arguments[0]), worldObj.provider.getDimensionId());//try to set to given dimension
                } else {
                    destDim = worldObj.provider.getDimensionId(); //return new Object[]{-1, "Not enough arguments, 1 needed: dimension"};
                }
                break;
            case 5: //getDimension
                return new Object[]{destDim};
            case 6: //getEnergy
                return new Object[]{ energyStorage.getEnergyStored()};
            case 7: //getMaxEnergy
                return new Object[]{ energyStorage.getMaxEnergyStored()};
            case 8: //warp
                doWarp = true;
                break;
            case 9: //listMethods
                String methods = "";
                for( String s : getMethodNames() ){
                    methods += (s + ", ");
                }
                return new Object[]{methods};
            //break;
        }

        return new Object[0];
    }

    @Override
    @Optional.Method(modid="ComputerCraft")
    public void attach(IComputerAccess computer) {

    }

    @Override
    @Optional.Method(modid="ComputerCraft")
    public void detach(IComputerAccess computer) {

    }

    @Override
    @Optional.Method(modid="ComputerCraft")
    public boolean equals(IPeripheral other) {
        if ((other != null) && (other instanceof TileEntityWarpCore)) {
            return other == this;
        }else {
            return false;
        }
    }

    //RF methods
    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return energyStorage.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return energyStorage.getMaxEnergyStored();
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    public boolean tryToUseEnergy(int energy){
//        if( !Loader.isModLoaded("CoFHAPI|energy") && !Loader.isModLoaded("CoFHCore") ){//to disable energy use if rf not installed
//            //LogHelper.info("Not trying to use energy");
//            return true;
//        }else{
            //LogHelper.info("Trying to use: " + energy + " RF");
            return true;//(energy == energyStorage.extractEnergy(energy, false));
//        }
    }


    @Override
    public int getSizeInventory() {
        return 5;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {

    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    public int getXPlus(){ return xPlus; }
    public int getYPlus(){ return yPlus; }
    public int getZPlus(){ return zPlus; }
    public int getXMinus(){ return xMinus; }
    public int getYMinus(){ return yMinus; }
    public int getZMinus(){ return zMinus; }
    public int getDx(){ return dx; }
    public int getDy(){ return dy; }
    public int getDz(){ return dz; }
    public int getDestDim(){ return destDim; }

    public void setXPlus(int xp){ xPlus = xp; }
    public void setYPlus(int yp){ yPlus = yp; }
    public void setZPlus(int zp){ zPlus = zp; }
    public void setXMinus(int xm){ xMinus = xm; }
    public void setYMinus(int ym){ yMinus = ym; }
    public void setZMinus(int zm){ zMinus = zm; }
    public void setDx(int Dx){ dx = Dx; }
    public void setDy(int Dy){ dy = Dy; }
    public void setDz(int Dz){ dz = Dz; }
    public void setDestDim(int dest){ destDim = dest; }

    public boolean willWarp(){
        return doWarp || (signal && !signalOn);
    }

    @Override
    public String getName() {
        return "Warp Core";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public IChatComponent getDisplayName() {
        return null;
    }
}

