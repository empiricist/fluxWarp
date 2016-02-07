package com.empiricist.fluxwarp.utility;


import com.empiricist.fluxwarp.FluxWarp;
import com.empiricist.fluxwarp.handler.ConfigurationHandler;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.Random;

//various utility methods related to teleporting things
public class TeleportHelper {

    //moves blocks by setting chunk data, this is the preferred method
    public static boolean moveBlockChunk(World origin, World dest, BlockPos pos1, BlockPos pos2, FluxWarpEnergyStorage energyStorage){//IEnergyStorage energyStorage){

        int x1 = pos1.getX(); int y1 = pos1.getY(); int z1 = pos1.getZ();
        int x2 = pos2.getX(); int y2 = pos2.getY(); int z2 = pos2.getZ();

        //MinecraftServer.getServer().worldTickTimes.put(dest.provider.dimensionId, new long[100]);
        //LogHelper.info("Max Ticket Length: " + ForgeChunkManager.getMaxTicketLengthFor(Reference.MOD_ID));
        //LogHelper.info("Max Chunks Depth: " + ForgeChunkManager.getMaxChunkDepthFor(Reference.MOD_ID));
        //LogHelper.info("Tickets Available: " + ForgeChunkManager.ticketCountAvailableFor(FluxWarp.instance, dest));
        ForgeChunkManager.Ticket ticket = null;
        ticket = ForgeChunkManager.requestTicket(FluxWarp.instance, dest, ForgeChunkManager.Type.NORMAL);

        Chunk oChunk = origin.getChunkFromBlockCoords(pos1);
        Chunk dChunk = dest.getChunkFromBlockCoords(pos2);

        if( origin.isAirBlock(pos1) ){
            //LogHelper.info("Air Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " , not moving");
            return true;
        }else if( !dest.isBlockLoaded(pos2) ){
            //LogHelper.info("Destination block at x:" + x2 + " y:" + y2 + " z:" + z2 + " does not exist, trying to load it");

            if(ticket == null){
                LogHelper.info("Failed to get chunk loading ticket, quitting");
                return false;
            }
            ForgeChunkManager.forceChunk( ticket, dest.getChunkFromBlockCoords(pos2).getChunkCoordIntPair() );
            ImmutableSet<ChunkCoordIntPair> chunks = ticket.getChunkList();        //getMaxChunkListDepth()
            //LogHelper.info("Chunks Loaded:" + chunks.size());
        }

        if(!origin.isAirBlock(pos1) && dest.isBlockLoaded(pos2) && tryToUseEnergy(ConfigurationHandler.blockCost, energyStorage)){ //don't bother moving air, or trying to move to nonexistent spots, or moving if there is insufficient energy

            //LogHelper.info("Origin Chunk coords: " + (x1 & 15) + " " + y1 + " " + (z1 & 15));
            //LogHelper.info("Origin Chunk coords: " + (x2 & 15) + " " + y2 + " " + (z2 & 15));
            Block oldBlock = oChunk.getBlock(x1 & 15, y1 ,z1 & 15);
            Block newBlock = dChunk.getBlock(x2 & 15, y2, z2 & 15);
            float oldHard = oldBlock.getBlockHardness(origin,pos1);
            float newHard = newBlock.getBlockHardness(dest,pos2);

            if( (oldHard != -1) && (newHard != -1) && (oldHard > newHard)){ //do not teleport if either end is indestructible, only overwrite if teleported block is more durable
                //LogHelper.info("Moving Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " to x:" + x2 + " y:" + y2 + " z:" + z2);
                portalParticles(origin, x1, y1, z1);
                portalParticles(dest, x2, y2, z2);


                dest.removeTileEntity(pos2);
                //break replaced block
                if(!dest.isAirBlock(pos2)){
                    breakBlock(newBlock, dest, pos2);
                }

                //move block id, metadata
                ExtendedBlockStorage destBlockStorage = dChunk.getBlockStorageArray()[y2 >> 4];

                if (destBlockStorage == null) {
                    destBlockStorage = dChunk.getBlockStorageArray()[y2 >> 4] = new ExtendedBlockStorage(y2 >> 4 << 4, !dChunk.getWorld().provider.getHasNoSky());
                }

//                destBlockStorage.func_150818_a(x2 & 15, y2 & 15, z2 & 15, oldBlock);
//                destBlockStorage.setExtBlockMetadata(x2 & 15, y2 & 15, z2 & 15, origin.getBlockMetadata(x1, y1, z1));
                destBlockStorage.set(x2 & 15, y2 & 15, z2 & 15, origin.getBlockState(pos1));

                dChunk.setChunkModified();
                //dest.markAndNotifyBlock(x2, y2, z2, dChunk, oldBlock, newBlock, 2);
                //dest.setBlockMetadataWithNotify(x2, y2, z2, origin.getBlockMetadata(x1,y1,z1), 2); //what does extra argument mean?
                dest.markBlocksDirtyVertical(x2, z2, 0, 256);


                dest.markBlockForUpdate(pos2);//so client actually gets message that block changed
                //dest.notifyBlockChange(x2, x2, x2, newBlock);//I have no idea what this does (NOT block update dest end)
                //dest.scheduleUpdate(pos2, oldBlock, 1);
//                oldBlock.getCollisionBoundingBoxFromPool(origin, x1, y1, z1);
//                oldBlock.getCollisionBoundingBoxFromPool(dest, x2, y2, z2);//not the problem, I think?

                /*if(oldBlock.getLightOpacity() != newBlock.getLightOpacity()){
                    oChunk.relightBlock(x2 & 15, y2, z2 & 15);
                }*/


                //move tileentity type and data
                TileEntity tileOrig = origin.getTileEntity(pos1);//check for tileentity
                if (tileOrig != null) {
                    //LogHelper.info("Found Tile Entity " + tileOrig.toString() + " at x:" + x1 + " y:" + y1 + " z:" + z1);

                    NBTTagCompound nbtData = new NBTTagCompound();
                    tileOrig.writeToNBT(nbtData);

//                    LogHelper.info("  reading NBT");
//                    LogHelper.info("    " + nbtData.toString());


                    nbtData.setInteger("x", x2);
                    nbtData.setInteger("y", y2);
                    nbtData.setInteger("z", z2);
//                    LogHelper.info("  changing NBT");
//                    LogHelper.info("    " + nbtData.toString());

                    //if(dest.getTileEntity(x2,y2,z2)!=null){ LogHelper.info( "There is already a " + dest.getTileEntity(x2,y2,z2).toString() + " here!" ); }

                    //tileentity is created with blocks
                    TileEntity tileNew = TileEntity.createAndLoadEntity(nbtData); //dest.getTileEntity(x2, y2, z2);

                    //does this cause problems for opencomputers?
//                    NBTTagCompound nbtNew = new NBTTagCompound();
//                    tileNew.writeToNBT(nbtNew);
//                    LogHelper.info("  new NBT");
//                    LogHelper.info("    " + nbtNew.toString());
//                    tileNew.readFromNBT(nbtData);

                    dest.setTileEntity(pos2, tileNew);


                    //it really SHOULDN'T be necessary to do this again...
                    //destBlockStorage.setExtBlockMetadata(x2 & 15, y2 & 15, z2 & 15, origin.getBlockMetadata(x1, y1, z1));
                    tileNew.updateContainingBlockInfo();//do I need this?

                    //remove old tileentity
                    origin.removeTileEntity(pos1);
                    tileOrig.updateContainingBlockInfo();//do I need this?
                }

                //remove original block if it was moved
                //origin.setBlock(x1, y1, z1, Blocks.air, 0, 2);
                ExtendedBlockStorage originBlockStorage = oChunk.getBlockStorageArray()[y1 >> 4];

                if (originBlockStorage == null) {
                    originBlockStorage = oChunk.getBlockStorageArray()[y1 >> 4] = new ExtendedBlockStorage(y1 >> 4 << 4, !oChunk.getWorld().provider.getHasNoSky());
                }

//                originBlockStorage.func_150818_a(x1 & 15, y1 & 15, z1 & 15, Blocks.air);
//                originBlockStorage.setExtBlockMetadata(x1 & 15, y1 & 15, z1 & 15, 0);
                originBlockStorage.set(x1 & 15, y1 & 15, z1 & 15, Blocks.air.getDefaultState() );
                origin.removeTileEntity(pos1);

                oChunk.setChunkModified();
                //origin.func_147451_t(x1,y1,z1);//maybe recalculate light? doesn't seem to help
                //origin.scheduleUpdate(pos1, Blocks.air, 1);

                //Blocks.air.setBlockBoundsBasedOnState(origin, x1, y1, z1);
                //oldBlock.setBlockBoundsBasedOnState(dest, x2, y2, z2);//doesn't seem to be the problem

                origin.markBlockForUpdate(pos1);//so client actually gets message that block changed

            }else if((oldHard != -1)  && (oldHard <= newHard || newHard == -1)){
                breakBlock(oldBlock, origin, pos1);
            }

        }else if( !dest.isBlockLoaded(pos2 )){return false;}//out of world bounds

        ForgeChunkManager.unforceChunk(ticket, dest.getChunkFromBlockCoords(pos2).getChunkCoordIntPair());
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

    public static boolean moveEntity(World origin, World dest, Entity entity, double x, double y, double z){
        int destDim = dest.provider.getDimensionId();
        //LogHelper.info("Moving entity to x:" + x + ", y:" + y + ", z:" + z + " and dimension:" + destDim);
        portalParticles(origin, entity.posX, entity.posY, entity.posZ);
        portalParticles(dest, x, y, z);

        if( entity instanceof EntityPlayer){ //regular setting position won't work on player :/
            EntityPlayer player = (EntityPlayer)entity;
            if (player.riddenByEntity != null) {
                player.riddenByEntity.mountEntity(null);
            }
            if (player.ridingEntity != null) {
                player.mountEntity(null);
            }

            //player.travelToDimension(destDim);
            if( origin != dest ) {

                ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();
                WorldServer s2 = MinecraftServer.getServer().worldServerForDimension(destDim);
                manager.transferPlayerToDimension((EntityPlayerMP) player, destDim, new WarpCoreTeleporter(s2));

                //manager.updateTimeAndWeatherForPlayer((EntityPlayerMP) player, s2);//does not seem to help weird sky after leaving end
                //manager.syncPlayerInventory(player);

                //MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, destDim, MinecraftServer.getServer().worldServerForDimension(destDim).getDefaultTeleporter());
                if (origin.provider.getDimensionId() == 1) {
                    // For some reason teleporting out of the end does weird things.
                    //player.setPositionAndUpdate(player.posX + dx, player.posY + dy, player.posZ + dz);
                    s2.spawnEntityInWorld(player);
                    s2.updateEntityWithOptionalForce(player, false);
                }
            }

            player.setPositionAndUpdate(x, y, z);

            dest.playSoundAtEntity(player, "mob.endermen.portal", 1, 1);//entity, sound, volume, pitch
        }else{
            entity.setPosition(x, y, z);
            if( origin.provider.getDimensionId() != destDim) {
                //entity.travelToDimension(destDim);//already did this
                WorldServer s1 = MinecraftServer.getServer().worldServerForDimension(origin.provider.getDimensionId());
                WorldServer s2 = MinecraftServer.getServer().worldServerForDimension(destDim);
                MinecraftServer.getServer().getConfigurationManager().transferEntityToWorld(entity, origin.provider.getDimensionId(), s1, s2, new WarpCoreTeleporter(s2));
            }
        }
        return true;
    }

    private static void breakBlock(Block block, World world, BlockPos pos){
        block.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
        world.setBlockToAir( pos );
        world.removeTileEntity( pos );
    }

    public static boolean tryToUseEnergy(int energy, FluxWarpEnergyStorage energyStorage){
        if( !Loader.isModLoaded("CoFHAPI|energy") && !Loader.isModLoaded("CoFHCore") ){//to disable energy use if rf not installed
            //LogHelper.info("Not trying to use energy");
            return true;
        }else{
            //LogHelper.info("Trying to use: " + energy + " RF");
            return (energy == energyStorage.extractEnergy(energy, false));
        }
    }

    public static void portalParticles(World world, double x, double y, double z){
        Random rand = new Random();
        if(rand.nextDouble() > 0.1){
            //LogHelper.info("Spawning particles at " + x + " " + y + " " + z);

            WorldServer worldServer = (WorldServer)world;
            //for(int i = 0; i < 10; i++){
                //world.spawnParticle("portal", x + (rand.nextDouble() - 0.5D), y + rand.nextDouble() - 0.25D, z + (rand.nextDouble() - 0.5D), (rand.nextDouble() - 0.5D) * 2.0D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2.0D);
                //world.playAuxSFX(2003, (int)x, (int)y, (int)z, 1);

                //spawns particle
                worldServer.spawnParticle(EnumParticleTypes.PORTAL, x + 0.5d, y + 0.5d, z + 0.5d, 10, 0d, 0d, 0d, 5d);//name, x, y, z, count, adjX, adjY, adjZ, velocityModifier
            //}
            //world.spawnParticle("portal", x, y, z, 0, 0, 0);
        }else{
            //LogHelper.info("Not spawning particles at " + x + " " + y + " " + z);
        }
    }


    //removes and places blocks through world, may cause blocks to pop off others due to updates
    public static void moveBlockWorld(World origin, World dest, BlockPos pos1, BlockPos pos2){
        int x1 = pos1.getX(); int y1 = pos1.getY(); int z1 = pos1.getZ();
        int x2 = pos2.getX(); int y2 = pos2.getY(); int z2 = pos2.getZ();

        //MinecraftServer.getServer().worldTickTimes.put(dest.provider.dimensionId, new long[100]);
        //LogHelper.info("Max Ticket Length: " + ForgeChunkManager.getMaxTicketLengthFor(Reference.MOD_ID));
        //LogHelper.info("Max Chunks Depth: " + ForgeChunkManager.getMaxChunkDepthFor(Reference.MOD_ID));
        LogHelper.info("Tickets Available: " + ForgeChunkManager.ticketCountAvailableFor(FluxWarp.instance, dest));
        ForgeChunkManager.Ticket ticket = null;
        ticket = ForgeChunkManager.requestTicket(FluxWarp.instance, dest, ForgeChunkManager.Type.NORMAL);



        if( origin.isAirBlock(pos1) ){
            //LogHelper.info("Air Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " , not moving");
        }else if( !dest.isBlockLoaded(pos2) ){
            LogHelper.info("Destination block at x:" + x2 + " y:" + y2 + " z:" + z2 + " does not exist, trying to load it");

            if(ticket == null){
                LogHelper.info("Failed to get chunk loading ticket, quitting");
                return;
            }
            ForgeChunkManager.forceChunk( ticket, dest.getChunkFromBlockCoords(pos2).getChunkCoordIntPair() );
            ImmutableSet<ChunkCoordIntPair> chunks = ticket.getChunkList();        //getMaxChunkListDepth()
            LogHelper.info("Chunks Loaded:" + chunks.size());
        }

        if(!origin.isAirBlock(pos1) && dest.isBlockLoaded(pos2)){ //don't bother moving air, or trying to move to nonexistent spots

            Block oldBlock = origin.getBlockState(pos1).getBlock();
            Block newBlock = dest.getBlockState(pos2).getBlock();
            float oldHard = oldBlock.getBlockHardness(origin, pos1);
            float newHard = newBlock.getBlockHardness(dest, pos2);

            if( (oldHard != -1) && (newHard != -1) && (oldHard > newHard)){ //do not teleport if either end is indestructible, only overwrite if teleported block is more durable
                LogHelper.info("Moving Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " to x:" + x2 + " y:" + y2 + " z:" + z2);
                //break replaced block
                if(!dest.isAirBlock(pos2)){
                    breakBlock(newBlock, dest, pos2);
                }

                //move block id, metadata
                dest.setBlockState( pos2, origin.getBlockState(pos1), 2);
                //dest.setBlockMetadataWithNotify(x2, y2, z2, origin.getBlockMetadata(x1,y1,z1), 2); //what does extra argument mean?



                //move tileentity type and data
                TileEntity tileOrig = origin.getTileEntity(pos1);//check for tileentity
                if (tileOrig != null) {
                    //tileOrig = (TileEntity)tileOrig;//do I need this?
                    //LogHelper.info("Found Tile Entity at x:" + x1 + " y:" + y1 + " z:" + z1);
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
                    TileEntity tileNew = dest.getTileEntity(pos2);
                    if (tileNew != null) {
                        tileNew.readFromNBT(nbtData);//load data from old tileentity
                        //LogHelper.info("  new NBT");
                        NBTTagCompound nbtNew = new NBTTagCompound();
                        tileNew.writeToNBT(nbtNew);
                        //LogHelper.info("    " + nbtNew.toString());
                        //tileNew.readFromNBT(nbtData);
                    }

                    //it really SHOULDN'T be necessary to do this again...
                    dest.setBlockState( pos2, origin.getBlockState(pos1), 2);

                    //remove old tileentity
                    origin.removeTileEntity(pos1);
                    //}
                }

                //remove original block if it was moved
                origin.setBlockState(pos1, Blocks.air.getDefaultState(), 2);

            }else if((oldHard != -1)  && (oldHard <= newHard || newHard == -1)){
                breakBlock(oldBlock, origin, pos1);
            }

        }

        ForgeChunkManager.unforceChunk(ticket, dest.getChunkFromBlockCoords(pos2).getChunkCoordIntPair());
        //why does this break things for later warps????? (wait, never mind, I think that was something else)
        if (ticket != null) { ForgeChunkManager.releaseTicket(ticket); }
        //seems to work ok without it though, old tickets are probably auto-recycled

    }


}
