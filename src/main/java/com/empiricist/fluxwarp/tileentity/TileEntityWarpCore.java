package com.empiricist.fluxwarp.tileentity;


import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import com.empiricist.fluxwarp.api.IDimensionPermissionBlock;
import com.empiricist.fluxwarp.handler.ConfigurationHandler;
import com.empiricist.fluxwarp.api.IDimensionPermissionItem;
import com.empiricist.fluxwarp.utility.LogHelper;
import com.empiricist.fluxwarp.utility.TeleportHelper;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.*;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

@Optional.InterfaceList({
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft", striprefs = true),
        @Optional.Interface(iface = "cofh.api.energy.IEnergyReceiver", modid = "CoFHAPI|energy", striprefs = true)
})
public class TileEntityWarpCore extends TileEntity implements IPeripheral, IEnergyReceiver{

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

    //public static int costPerBlock = 10;

    public TileEntityWarpCore() {
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
        //destDim = worldObj.provider.dimensionId;
        energyStorage = new EnergyStorage(ConfigurationHandler.coreEnergyStorage, 1000, ConfigurationHandler.coreEnergyStorage);//capacity, receive, extract
    }

    //what is this method for?
    public boolean isIdle() {
        return !signalOn;
    }



    @Override
    public void updateEntity() {


        if (!worldObj.isRemote) {


            //LogHelper.info("Did it work?: " + (this instanceof IPeripheral));
            if (doWarp || (signal && !signalOn)) {
                LogHelper.info("doWarp is " + doWarp);
                LogHelper.info("Signal stuff is " + (signal && !signalOn));

                signalOn = signal;
                doWarp = false;
                getDirections();

                LogHelper.info("Activating Warp Drive, x:" + xCoord + " y:" + yCoord + " z:" + zCoord);
                if( dx == 0 && dy == 0 && dz == 0 && destDim == worldObj.provider.dimensionId){
                    LogHelper.info("Destination is the same as origin, quitting warp");
                    worldObj.playSoundEffect(xCoord, yCoord, zCoord, "note.bd", 1, 1);
                    return;
                }

                if( !DimensionManager.isDimensionRegistered(destDim) ){
                    LogHelper.info("Dimension " + destDim + " is not registered!");
                    worldObj.playSoundEffect(xCoord, yCoord, zCoord, "note.bassattack", 1, 1);
                    return;
                }

                if( !hasPermissionForDimension(destDim) ){
                    LogHelper.info("Dimension " + destDim + " not found in address database");
                    worldObj.playSoundEffect(xCoord, yCoord, zCoord, "note.harp", 1, 1);
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
                    LogHelper.info("Same dimension");
                    if( !tryToUseEnergy(ConfigurationHandler.baseCost) ){
                        LogHelper.info("Out of energy for warp!");
                        worldObj.playSoundEffect(xCoord, yCoord, zCoord, "mob.enderdragon.hit", 1, 1);
                        return;
                    }
                }else{
                    LogHelper.info("Different dimension");
                    if( !tryToUseEnergy(ConfigurationHandler.dimensionCost) ){
                        LogHelper.info("Out of energy for warp!");
                        worldObj.playSoundEffect(xCoord, yCoord, zCoord, "mob.enderdragon.hit", 1, 1);
                        return;
                    }
                }


                LogHelper.info("Destination World: " + world2.toString() + ", " + world2.provider.dimensionId);//make sure it worked
                double posMultiplier = worldObj.provider.getMovementFactor() / world2.provider.getMovementFactor();//for the nether and stuff
                int newXCen = (int)((xCoord) * posMultiplier) + dx;
                int newYCen = yCoord + dy;
                int newZCen = (int)((zCoord) * posMultiplier) + dz;

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
                        iBeg = xCoord+xPlus;
                        iEnd = xCoord-xMinus-1;
                    }else{
                        iBeg = xCoord-xMinus;
                        iEnd = xCoord+xPlus+1;
                    }
                    ix = 1;
                    jBeg = yCoord-yMinus;
                    jEnd = yCoord+yPlus;
                    jy = 1;
                    kBeg = zCoord-zMinus;
                    kEnd = zCoord+zPlus;
                    kz = 1;
                }else if(dy != 0){
                    if(dy > 0){
                        iInc = -1;
                        iBeg = yCoord+yPlus;
                        iEnd = yCoord-yMinus-1;
                        //iy = 1;
                    }else{
                        iBeg = yCoord-yMinus;
                        iEnd = yCoord+yPlus+1;
                        //iy = -1;
                    }
                    iy = 1;
                    jBeg = xCoord-xMinus;
                    jEnd = xCoord+xPlus;
                    jx = 1;
                    kBeg = zCoord-zMinus;
                    kEnd = zCoord+zPlus;
                    kz = 1;
                }else{
                    if(dz > 0){
                        iInc = -1;
                        iBeg = zCoord+zPlus;
                        iEnd = zCoord-zMinus-1;
                        //iz = 1;
                    }else{
                        iBeg = zCoord-zMinus;
                        iEnd = zCoord+zPlus+1;
                        //iz = -1;
                    }
                    iz = 1;
                    jBeg = xCoord-xMinus;
                    jEnd = xCoord+xPlus;
                    jx = 1;
                    kBeg = yCoord-yMinus;
                    kEnd = yCoord+yPlus;
                    ky = 1;
                }
//                LogHelper.info("i: " + ix + " " + iy + " " + iz + " : " + iBeg + " " + iEnd);
//                LogHelper.info("j: " + jx + " " + jy + " " + jz + " : " + jBeg + " " + jEnd);
//                LogHelper.info("k: " + kx + " " + ky + " " + kz + " : " + kBeg + " " + kEnd);
                //end of magic

                boolean isWarpSuccessful = true;

                //iterate through a volume of blocks to move
                for(int i = iBeg; i != iEnd; i += iInc){
                    for(int j = jBeg; j <= jEnd; j++){
                        for(int k = kBeg; k <= kEnd; k++){

                            //wait this is also magic
                            int x = i*ix + j*jx + k*kx;
                            int y = i*iy + j*jy + k*ky;
                            int z = i*iz + j*jz + k*kz;
                            isWarpSuccessful &= TeleportHelper.moveBlock2(worldObj, world2, x, y, z, (x - xCoord) + newXCen, (y - yCoord) + newYCen, (z - zCoord) + newZCen, energyStorage);//supposedly this works for booleans

                        }
                    }
                }

                //schedule block updates around the outside of the bounding box
                //LogHelper.info("Scheduling block updates at x " + (xCoord-xMinus) + "to" + (xCoord+xPlus) + " y " + (yCoord-yMinus) + "to" + (yCoord+yPlus) + " z " + (zCoord-zMinus-1) + " and " + (zCoord+zPlus+1));
                for(int i = -xMinus; i <= +xPlus; i++){
                    for(int j = -yMinus; j <= +yPlus; j++){
                        worldObj.scheduleBlockUpdate(xCoord+i, yCoord+j, zCoord-zMinus-1, worldObj.getBlock(xCoord+i, yCoord+j, zCoord-zMinus), 1);
                        worldObj.scheduleBlockUpdate(xCoord+i, yCoord+j, zCoord+zPlus+1, worldObj.getBlock(xCoord+i, yCoord+j, zCoord+zMinus), 1);
                        world2.scheduleBlockUpdate(newXCen+i, newYCen+j, newZCen-zMinus-1, world2.getBlock(newXCen+i, newYCen+j, newZCen-zMinus-1), 1);
                        world2.scheduleBlockUpdate(newXCen+i, newYCen+j, newZCen+zPlus+1, world2.getBlock(newXCen+i, newYCen+j, newZCen+zPlus+1), 1);
                    }
                }
                //LogHelper.info("Scheduling block updates at x " + (xCoord-xMinus) + "to" + (xCoord+xPlus) + " y " + (yCoord-yMinus-1) + " and " + (yCoord+yPlus+1) + " z " + (zCoord-zMinus) + "to" + (zCoord+zPlus));
                for(int i = -xMinus; i <= +xPlus; i++){
                    for(int j = -zMinus; j <= +zPlus; j++){
                        worldObj.scheduleBlockUpdate(xCoord+i, yCoord-yMinus-1, zCoord+j, worldObj.getBlock(xCoord+i, yCoord-yMinus-1, zCoord+j), 1);
                        worldObj.scheduleBlockUpdate(xCoord+i, yCoord+yPlus+1, zCoord+j, worldObj.getBlock(xCoord+i, yCoord+yPlus+1, zCoord+j), 1);
                        world2.scheduleBlockUpdate(newXCen+i, newYCen-yMinus+1, newZCen+i, world2.getBlock(newXCen+i, newYCen-yMinus+1, newZCen+i), 1);
                        world2.scheduleBlockUpdate(newXCen+i, newYCen+yPlus+1, newZCen+i, world2.getBlock(newXCen+i, newYCen+yPlus+1, newZCen+i), 1);
                    }
                }
                //LogHelper.info("Scheduling block updates at x " + (xCoord-xMinus-1) + " and " + (xCoord+xPlus+1) + " y " + (yCoord-yMinus) + "to" + (yCoord+yPlus) + " z " + (zCoord-zMinus) + "to" + (zCoord+zPlus));
                for(int i = -yMinus; i <= +yPlus; i++){
                    for(int j = -zMinus; j <= +zPlus; j++){
                        worldObj.scheduleBlockUpdate(xCoord-xMinus-1, yCoord+i, zCoord+j, worldObj.getBlock(xCoord-xMinus-1, yCoord+i, zCoord+j), 1);
                        worldObj.scheduleBlockUpdate(xCoord+xPlus+1, yCoord+i, zCoord+j, worldObj.getBlock(xCoord+xPlus+1, yCoord+i, zCoord+j), 1);
                        world2.scheduleBlockUpdate(newXCen-xMinus-1, newYCen+i, newZCen+j, world2.getBlock(newXCen-xMinus-1, newYCen+i, newZCen+j), 1);
                        world2.scheduleBlockUpdate(newXCen+xPlus+1, newYCen+i, newZCen+j, world2.getBlock(newXCen+xPlus+1, newYCen+i, newZCen+j), 1);
                    }
                }

                LogHelper.info("Was warp successful for all blocks: " + isWarpSuccessful);
                if( !isWarpSuccessful ){
                    worldObj.playSoundEffect(xCoord, yCoord, zCoord, "mob.enderdragon.hit", 1, 1);
                }
/*
                //iterate through a volume of blocks to move
                for(int x = xCoord-xMinus; x <= xCoord+xPlus; x++){
                    for(int y = yCoord-yMinus; y <= yCoord+yPlus; y++){
                        for(int z = zCoord-zMinus; z <= zCoord+zPlus; z++){

                            moveBlock(worldObj, world2, x, y, z, x+dx, y+dy, z+dz);

                        }
                    }
                }
-*/
                //move entities within volume too (but only if ship also moved, or does not exist)
                if(isWarpSuccessful){
                    List<Entity> entitiesInVolume = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(xCoord - xMinus, yCoord - yMinus, zCoord - zMinus, xCoord + xPlus+1, yCoord + yPlus+1, zCoord + zPlus+1));
                    for (Entity entity : entitiesInVolume){
                        LogHelper.info("Found Entity: " + entity.toString());

                        if( tryToUseEnergy(ConfigurationHandler.entityCost) ){

                            //entity should stay at same relative position within ship, even if movement factor is not 1
                            double newX = entity.posX - xCoord + newXCen;
                            double newY = entity.posY - yCoord + newYCen;
                            double newZ = entity.posZ - zCoord + newZCen;

                            TeleportHelper.moveEntity(worldObj, world2, entity, newX, newY, newZ);

                        }else{
                            LogHelper.info("Out of energy for entity!");
                        }
                    }
                }

                worldObj.playSoundEffect(xCoord, yCoord, zCoord, "random.fizz", 1, 1);
                world2.playSoundEffect(newXCen, newYCen, newZCen, "ambient.weather.thunder", 1, 1);

            }

            signalOn = signal;
        }
    }

    public boolean hasPermissionForDimension( int dimID ){
        if(dimID == worldObj.provider.dimensionId){//don't need permission to move within same dimension
            return true;
        }
        TileEntity te;
        Block b;
        for( ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS ){

            b = worldObj.getBlock(xCoord + dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
            if(b != null && b instanceof IDimensionPermissionBlock){
                IDimensionPermissionBlock perm = (IDimensionPermissionBlock) b;
                if( perm.canTravelTo(worldObj, xCoord + dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ, dimID) ){
                    return true;
                }
            }
            te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
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
        TileEntity tile = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);

        if (tile != null && tile instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign) tile;

            String[] text = sign.signText;
            dx = safeReadInt(text[0], 0);
            dy = safeReadInt(text[1], 0);
            dz = safeReadInt(text[2], 0);
            destDim = safeReadInt(text[3], worldObj.provider.dimensionId);
            LogHelper.info("Found coords dx:" + dx + " dy:" + dy + " dz:" + dz + " dim:" + destDim);
        }
    }

    private int safeReadInt(String string, int defaultVal){
        int res = defaultVal;
        LogHelper.info("Trying to read string: " + string);
        try{
            res = (int)Double.parseDouble(string);//computercraft returns all numbers as decimals, b/c lua
        }catch(NumberFormatException e){
            res = defaultVal;
        }
        return res;
    }


    public void setDoWarp(boolean newWarp){
        signalOn = newWarp;
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

    public String safeReadString(Object obj){
        //if (obj instanceof String){
            return obj.toString();
        //}else{
        //    return "";
        //}
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
                    xPlus = safeReadInt(safeReadString(arguments[0]), 1);
                    xMinus = safeReadInt(safeReadString(arguments[1]), 1);
                    yPlus = safeReadInt(safeReadString(arguments[2]), 1);
                    yMinus = safeReadInt(safeReadString(arguments[3]), 1);
                    zPlus = safeReadInt(safeReadString(arguments[4]), 1);
                    zMinus = safeReadInt(safeReadString(arguments[5]), 1);
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
                    dx = safeReadInt(safeReadString(arguments[0]), 0);
                    dy = safeReadInt(safeReadString(arguments[1]), 0);
                    dz = safeReadInt(safeReadString(arguments[2]), 0);
                } else {
                    //return new Object[]{-1, "Not enough arguments, 3 needed: dx, dy, dz"};
                    throw new LuaException("Not enough arguments, 3 needed: dx, dy, dz");
                }
                break;
            case 3: //getWarpVector
                return new Object[]{dx, dy, dz};
            case 4: //setDimension
                if (arguments.length >= 1) {
                    destDim = safeReadInt(safeReadString(arguments[0]), worldObj.provider.dimensionId);//try to set to given dimension
                } else {
                    destDim = worldObj.provider.dimensionId; //return new Object[]{-1, "Not enough arguments, 1 needed: dimension"};
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
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        return energyStorage.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return energyStorage.getMaxEnergyStored();
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return true;
    }

    public boolean tryToUseEnergy(int energy){
        if( !Loader.isModLoaded("CoFHAPI|energy") && !Loader.isModLoaded("CoFHCore") ){//to disable energy use if rf not installed
            //LogHelper.info("Not trying to use energy");
            return true;
        }else{
            //LogHelper.info("Trying to use: " + energy + " RF");
            return (energy == energyStorage.extractEnergy(energy, false));
        }
    }
}

