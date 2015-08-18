package com.empiricist.fluxwarp.tileentity;


import cofh.api.energy.IEnergyReceiver;
import com.empiricist.fluxwarp.FluxWarp;
import com.empiricist.fluxwarp.handler.ConfigurationHandler;
import com.empiricist.fluxwarp.init.ModBlocks;
import com.empiricist.fluxwarp.item.IDimensionPermissionItem;
import com.empiricist.fluxwarp.reference.Reference;
import com.empiricist.fluxwarp.utility.FluxWarpEnergyStorage;
import com.empiricist.fluxwarp.utility.LogHelper;
import com.empiricist.fluxwarp.utility.WarpCoreTeleporter;
import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
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

    public FluxWarpEnergyStorage energyStorage;

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
        energyStorage = new FluxWarpEnergyStorage(10000, 1000, 10000);
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
                    return;
                }

                if( !DimensionManager.isDimensionRegistered(destDim) ){
                    LogHelper.info("Dimension " + destDim + " is not registered!");
                    return;
                }

                if( !hasPermissionForDimension(destDim) ){
                    LogHelper.info("Dimension " + destDim + " not found in address database");
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
                        return;
                    }
                }else{
                    LogHelper.info("Different dimension");
                    if( !tryToUseEnergy(ConfigurationHandler.dimensionCost) ){
                        LogHelper.info("Out of energy for warp!");
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
                LogHelper.info("i: " + ix + " " + iy + " " + iz + " : " + iBeg + " " + iEnd);
                LogHelper.info("j: " + jx + " " + jy + " " + jz + " : " + jBeg + " " + jEnd);
                LogHelper.info("k: " + kx + " " + ky + " " + kz + " : " + kBeg + " " + kEnd);
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
                            isWarpSuccessful &= moveBlock2(worldObj, world2, x, y, z, (x - xCoord) + newXCen, (y - yCoord) + newYCen, (z - zCoord) + newZCen);//supposedly this works for booleans

                        }
                    }
                }
                LogHelper.info("Was warp successful for all blocks: " + isWarpSuccessful);
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
                            LogHelper.info("Moving entity to x:" + newX + ", y:" + newY + ", z:" + newZ);

                            if( entity instanceof EntityPlayer ){ //regular setting position won't work on player :/
                                EntityPlayer player = (EntityPlayer)entity;
                                if (player.riddenByEntity != null) {
                                    player.riddenByEntity.mountEntity(null);
                                }
                                if (player.ridingEntity != null) {
                                    player.mountEntity(null);
                                }
                                player.setPositionAndUpdate(newX, newY, newZ);

                                //player.travelToDimension(destDim);
                                if( worldObj.provider.dimensionId != destDim) {
                                    ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();
                                    manager.transferPlayerToDimension((EntityPlayerMP) player, destDim, new WarpCoreTeleporter(MinecraftServer.getServer().worldServerForDimension(destDim)));// new RfToolsTeleporter(worldServer, x, y, z));
                                    WorldServer s2 = MinecraftServer.getServer().worldServerForDimension(destDim);
                                    //manager.updateTimeAndWeatherForPlayer(player, s2);
                                    //dmanager.syncPlayerInventory(player);

                                    //MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, destDim, MinecraftServer.getServer().worldServerForDimension(destDim).getDefaultTeleporter());
                                    if (worldObj.provider.dimensionId == 1) {
                                        // For some reason teleporting out of the end does weird things.
                                        //player.setPositionAndUpdate(player.posX + dx, player.posY + dy, player.posZ + dz);
                                        s2.spawnEntityInWorld(player);
                                        s2.updateEntityWithOptionalForce(player, false);
                                    }
                                }
                                worldObj.playSoundAtEntity(player, "mob.endermen.portal", 1, 1);
                            }else{
                                entity.setPosition(newX, newY, newZ);
                                if( worldObj.provider.dimensionId != destDim) {
                                    //entity.travelToDimension(destDim);//already did this
                                    WorldServer s1 = MinecraftServer.getServer().worldServerForDimension(worldObj.provider.dimensionId);
                                    WorldServer s2 = MinecraftServer.getServer().worldServerForDimension(destDim);
                                    MinecraftServer.getServer().getConfigurationManager().transferEntityToWorld(entity, worldObj.provider.dimensionId, s1, s2, new WarpCoreTeleporter(MinecraftServer.getServer().worldServerForDimension(destDim)));
                                }
                            }
                        }else{
                            LogHelper.info("Out of energy for entity!");
                        }
                    }
                }

            }

            signalOn = signal;
        }
    }

    public void moveBlock(World origin, World dest, int x1, int y1, int z1, int x2, int y2, int z2){
        //MinecraftServer.getServer().worldTickTimes.put(dest.provider.dimensionId, new long[100]);
        //LogHelper.info("Max Ticket Length: " + ForgeChunkManager.getMaxTicketLengthFor(Reference.MOD_ID));
        //LogHelper.info("Max Chunks Depth: " + ForgeChunkManager.getMaxChunkDepthFor(Reference.MOD_ID));
        LogHelper.info("Tickets Available: " + ForgeChunkManager.ticketCountAvailableFor(FluxWarp.instance, dest));
        ForgeChunkManager.Ticket ticket = null;
        ticket = ForgeChunkManager.requestTicket(FluxWarp.instance, dest, ForgeChunkManager.Type.NORMAL);



        if( origin.isAirBlock(x1,y1,z1) ){
            //LogHelper.info("Air Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " , not moving");
        }else if( !dest.blockExists(x2,y2,z2) ){
            LogHelper.info("Destination block at x:" + x2 + " y:" + y2 + " z:" + z2 + " does not exist, trying to load it");

            if(ticket == null){
                LogHelper.info("Failed to get chunk loading ticket, quitting");
                return;
            }
            ForgeChunkManager.forceChunk( ticket, dest.getChunkFromBlockCoords(x2,z2).getChunkCoordIntPair() );
            ImmutableSet<ChunkCoordIntPair> chunks = ticket.getChunkList();        //getMaxChunkListDepth()
            LogHelper.info("Chunks Loaded:" + chunks.size());
        }

        if(!origin.isAirBlock(x1,y1,z1) && dest.blockExists(x2,y2,z2)){ //don't bother moving air, or trying to move to nonexistent spots

            Block oldBlock = origin.getBlock(x1,y1,z1);
            Block newBlock = dest.getBlock(x2,y2,z2);
            float oldHard = oldBlock.getBlockHardness(origin,x1,y1,z1);
            float newHard = newBlock.getBlockHardness(dest,x2,y2,z2);

            if( (oldHard != -1) && (newHard != -1) && (oldHard > newHard)){ //do not teleport if either end is indestructible, only overwrite if teleported block is more durable
                LogHelper.info("Moving Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " to x:" + x2 + " y:" + y2 + " z:" + z2);
                //break replaced block
                if(!dest.isAirBlock(x2,y2,z2)){
                    breakBlock(newBlock, dest, x2, y2, z2);
                }

                //move block id, metadata
                dest.setBlock(x2, y2, z2, origin.getBlock(x1,y1,z1), origin.getBlockMetadata(x1,y1,z1), 2);
                //dest.setBlockMetadataWithNotify(x2, y2, z2, origin.getBlockMetadata(x1,y1,z1), 2); //what does extra argument mean?



                //move tileentity type and data
                TileEntity tileOrig = origin.getTileEntity(x1, y1, z1);//check for tileentity
                if (tileOrig != null) {
                    //tileOrig = (TileEntity)tileOrig;//do I need this?
                    LogHelper.info("Found Tile Entity at x:" + x1 + " y:" + y1 + " z:" + z1);
                                        /*--
                                        if (x == xCoord && y == yCoord && z == zCoord) { //treat the active warp core specially until copying nbt works
                                            LogHelper.info("  It is this warp core");
                                            TileEntity tile = worldObj.getTileEntity(x + dx, y + dy, z + dz);

                                            if (tile != null && tile instanceof TileEntityWarpCore) {
                                                TileEntityWarpCore core = (TileEntityWarpCore) tile;

                                                core.setDoWarp(false);//to prevent copy from warping too
                                            }
                                        } else {
                                        --*/
                    NBTTagCompound nbtData = new NBTTagCompound();
                    tileOrig.writeToNBT(nbtData);

                    //LogHelper.info("  reading NBT");
                    //LogHelper.info("    " + nbtData.toString());

                    //LogHelper.info("  changing NBT");
                    nbtData.setInteger("x", x2);
                    nbtData.setInteger("y", y2);
                    nbtData.setInteger("z", z2);
                    //LogHelper.info("    " + nbtData.toString());


                    dest.addTileEntity(TileEntity.createAndLoadEntity(nbtData));

                    //tileentity is created with blocks
                    TileEntity tileNew = dest.getTileEntity(x2, y2, z2);
                    if (tileNew != null) {
                        tileNew.readFromNBT(nbtData);//load data from old tileentity
                        //LogHelper.info("  new NBT");
                        NBTTagCompound nbtNew = new NBTTagCompound();
                        tileNew.writeToNBT(nbtNew);
                        //LogHelper.info("    " + nbtNew.toString());
                        //tileNew.readFromNBT(nbtData);
                    }

                    //it really SHOULDN'T be necessary to do this again...
                    dest.setBlockMetadataWithNotify(x2, y2, z2, origin.getBlockMetadata(x1,y1,z1), 2);//but it is

                    //remove old tileentity
                    origin.removeTileEntity(x1, y1, z1);
                    //}
                }

                //remove original block if it was moved
                origin.setBlock(x1, y1, z1, Blocks.air, 0, 2);

            }else if((oldHard != -1)  && (oldHard <= newHard || newHard == -1)){
                breakBlock(oldBlock, origin, x1, y1, z1);
            }

        }

        ForgeChunkManager.unforceChunk(ticket, dest.getChunkFromBlockCoords(x2,z2).getChunkCoordIntPair());
        //why does this break things for later warps????? (wait, never mind, I think that was something else)
        if (ticket != null) { ForgeChunkManager.releaseTicket(ticket); }
        //seems to work ok without it though, old tickets are probably auto-recycled

    }

    public boolean moveBlock2(World origin, World dest, int x1, int y1, int z1, int x2, int y2, int z2){

        //MinecraftServer.getServer().worldTickTimes.put(dest.provider.dimensionId, new long[100]);
        //LogHelper.info("Max Ticket Length: " + ForgeChunkManager.getMaxTicketLengthFor(Reference.MOD_ID));
        //LogHelper.info("Max Chunks Depth: " + ForgeChunkManager.getMaxChunkDepthFor(Reference.MOD_ID));
        //LogHelper.info("Tickets Available: " + ForgeChunkManager.ticketCountAvailableFor(FluxWarp.instance, dest));
        ForgeChunkManager.Ticket ticket = null;
        ticket = ForgeChunkManager.requestTicket(FluxWarp.instance, dest, ForgeChunkManager.Type.NORMAL);

        Chunk oChunk = origin.getChunkFromBlockCoords(x1,z1);
        Chunk dChunk = dest.getChunkFromBlockCoords(x2,z2);

        if( origin.isAirBlock(x1,y1,z1) ){
            //LogHelper.info("Air Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " , not moving");
            return true;
        }else if( !dest.blockExists(x2,y2,z2) ){
            LogHelper.info("Destination block at x:" + x2 + " y:" + y2 + " z:" + z2 + " does not exist, trying to load it");

            if(ticket == null){
                LogHelper.info("Failed to get chunk loading ticket, quitting");
                return false;
            }
            ForgeChunkManager.forceChunk( ticket, dest.getChunkFromBlockCoords(x2,z2).getChunkCoordIntPair() );
            ImmutableSet<ChunkCoordIntPair> chunks = ticket.getChunkList();        //getMaxChunkListDepth()
            LogHelper.info("Chunks Loaded:" + chunks.size());
        }

        if(!origin.isAirBlock(x1,y1,z1) && dest.blockExists(x2,y2,z2) && tryToUseEnergy(ConfigurationHandler.blockCost)){ //don't bother moving air, or trying to move to nonexistent spots, or moving if there is insufficient energy

            //LogHelper.info("Origin Chunk coords: " + (x1 & 15) + " " + y1 + " " + (z1 & 15));
            //LogHelper.info("Origin Chunk coords: " + (x2 & 15) + " " + y2 + " " + (z2 & 15));
            Block oldBlock = oChunk.getBlock(x1 & 15, y1 ,z1 & 15);
            Block newBlock = dChunk.getBlock(x2 & 15, y2, z2 & 15);
            float oldHard = oldBlock.getBlockHardness(origin,x1,y1,z1);
            float newHard = newBlock.getBlockHardness(dest,x2,y2,z2);

            if( (oldHard != -1) && (newHard != -1) && (oldHard > newHard)){ //do not teleport if either end is indestructible, only overwrite if teleported block is more durable
                //LogHelper.info("Moving Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " to x:" + x2 + " y:" + y2 + " z:" + z2);

                //break replaced block
                if(!dest.isAirBlock(x2,y2,z2)){
                    breakBlock(newBlock, dest, x2, y2, z2);
                }

                //move block id, metadata
                ExtendedBlockStorage extendedblockstorage = dChunk.getBlockStorageArray()[y2 >> 4];

                if (extendedblockstorage == null) {
                    extendedblockstorage = dChunk.getBlockStorageArray()[y2 >> 4] = new ExtendedBlockStorage(y2 >> 4 << 4, !dChunk.worldObj.provider.hasNoSky);
                }

                extendedblockstorage.func_150818_a(x2 & 15, y2 & 15, z2 & 15, oldBlock);
                extendedblockstorage.setExtBlockMetadata(x2 & 15, y2 & 15, z2 & 15, origin.getBlockMetadata(x1,y1,z1));

                dChunk.isModified = true;
                //dest.setBlockMetadataWithNotify(x2, y2, z2, origin.getBlockMetadata(x1,y1,z1), 2); //what does extra argument mean?


                dest.markBlockForUpdate(x2, y2, z2);//so client actually gets message that block changed
                //dest.notifyBlockChange(x2, x2, x2, newBlock);//I have no idea what this does (NOT block update dest end)

                /*if(oldBlock.getLightOpacity() != newBlock.getLightOpacity()){
                    oChunk.relightBlock(x2 & 15, y2, z2 & 15);
                }*/



                //move tileentity type and data
                TileEntity tileOrig = origin.getTileEntity(x1, y1, z1);//check for tileentity
                if (tileOrig != null) {
                    //tileOrig = (TileEntity)tileOrig;//do I need this?
                    LogHelper.info("Found Tile Entity at x:" + x1 + " y:" + y1 + " z:" + z1);
                                        /*--
                                        if (x == xCoord && y == yCoord && z == zCoord) { //treat the active warp core specially until copying nbt works
                                            LogHelper.info("  It is this warp core");
                                            TileEntity tile = worldObj.getTileEntity(x + dx, y + dy, z + dz);

                                            if (tile != null && tile instanceof TileEntityWarpCore) {
                                                TileEntityWarpCore core = (TileEntityWarpCore) tile;

                                                core.setDoWarp(false);//to prevent copy from warping too
                                            }
                                        } else {
                                        --*/
                    NBTTagCompound nbtData = new NBTTagCompound();
                    tileOrig.writeToNBT(nbtData);

//                    LogHelper.info("  reading NBT");
//                    LogHelper.info("    " + nbtData.toString());

                    //LogHelper.info("  changing NBT");
                    nbtData.setInteger("x", x2);
                    nbtData.setInteger("y", y2);
                    nbtData.setInteger("z", z2);
                    //LogHelper.info("    " + nbtData.toString());


                    dChunk.addTileEntity(TileEntity.createAndLoadEntity(nbtData));

                    //tileentity is created with blocks
                    TileEntity tileNew = dest.getTileEntity(x2, y2, z2);
                    if (tileNew != null) {
                        tileNew.readFromNBT(nbtData);//load data from old tileentity

                        NBTTagCompound nbtNew = new NBTTagCompound();
                        tileNew.writeToNBT(nbtNew);
//                        LogHelper.info("  new NBT");
//                        LogHelper.info("    " + nbtNew.toString());
                        tileNew.readFromNBT(nbtData);
                    }

                    //it really SHOULDN'T be necessary to do this again...
                    extendedblockstorage.setExtBlockMetadata(x2 & 15, y2 & 15, z2 & 15, origin.getBlockMetadata(x1,y1,z1));

                    //remove old tileentity
                    origin.removeTileEntity(x1, y1, z1);
                    //}
                }

                //remove original block if it was moved
                //origin.setBlock(x1, y1, z1, Blocks.air, 0, 2);
                ExtendedBlockStorage originblockstorage = oChunk.getBlockStorageArray()[y1 >> 4];

                if (originblockstorage == null) {
                    originblockstorage = oChunk.getBlockStorageArray()[y1 >> 4] = new ExtendedBlockStorage(y1 >> 4 << 4, !oChunk.worldObj.provider.hasNoSky);
                }

                originblockstorage.func_150818_a(x1 & 15, y1 & 15, z1 & 15, Blocks.air);
                originblockstorage.setExtBlockMetadata(x1 & 15, y1 & 15, z1 & 15, 0);

                oChunk.isModified = true;

                origin.markBlockForUpdate(x1, y1, z1);//so client actually gets message that block changed

            }else if((oldHard != -1)  && (oldHard <= newHard || newHard == -1)){
                breakBlock(oldBlock, origin, x1, y1, z1);
            }

        }else if( !dest.blockExists(x2,y2,z2 )){return false;}//out of world bounds

        ForgeChunkManager.unforceChunk(ticket, dest.getChunkFromBlockCoords(x2,z2).getChunkCoordIntPair());
        //why does this break things for later warps????? (wait, never mind, I think that was something else)
        if (ticket != null) {
            ForgeChunkManager.releaseTicket(ticket);
            //LogHelper.info("Releasing Ticket");
        }else{
            //LogHelper.info("Ticket is null, not releasing");
        }
        //seems to work ok without it though, old tickets are probably auto-recycled

        return true;

    }

    public boolean hasPermissionForDimension( int dimID ){
        if(dimID == worldObj.provider.dimensionId){//don't need permission to move within same dimension
            return true;
        }
        TileEntity te = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
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
        return false;
    }

    private void breakBlock(Block block, World world, int x, int y, int z){
        block.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x,y,z), 0);
        world.setBlockToAir(x,y,z);
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

