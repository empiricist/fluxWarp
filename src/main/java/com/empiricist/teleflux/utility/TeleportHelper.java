package com.empiricist.teleflux.utility;


import cofh.api.energy.EnergyStorage;
import com.empiricist.teleflux.TeleFlux;
import com.empiricist.teleflux.handler.ConfigurationHandler;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
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
import net.minecraftforge.fml.common.Loader;

import java.util.Random;

//various utility methods related to teleporting things
public class TeleportHelper {

    //moves blocks by setting chunk data, this is the preferred method
    public static boolean moveBlockChunk(World origin, World dest, BlockPos pos1, BlockPos pos2, EnergyStorage energyStorage){//IEnergyStorage energyStorage){

        int x1 = pos1.getX(); int y1 = pos1.getY(); int z1 = pos1.getZ();
        int x2 = pos2.getX(); int y2 = pos2.getY(); int z2 = pos2.getZ();

        //MinecraftServer.getServer().worldTickTimes.put(dest.provider.dimensionId, new long[100]);
        //LogHelper.info("Max Ticket Length: " + ForgeChunkManager.getMaxTicketLengthFor(Reference.MOD_ID));
        //LogHelper.info("Max Chunks Depth: " + ForgeChunkManager.getMaxChunkDepthFor(Reference.MOD_ID));
        //LogHelper.info("Tickets Available: " + ForgeChunkManager.ticketCountAvailableFor(FluxWarp.instance, dest));
        ForgeChunkManager.Ticket ticket = null;
        ticket = ForgeChunkManager.requestTicket(TeleFlux.instance, dest, ForgeChunkManager.Type.NORMAL);

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
            //ForgeChunkManager.forceChunk( ticket, dest.getChunkFromBlockCoords(pos2).getChunkCoordIntPair() );
            ImmutableSet<ChunkCoordIntPair> chunks = ticket.getChunkList();        //getMaxChunkListDepth()
            //LogHelper.info("Chunks Loaded:" + chunks.size());
        }

        if(!origin.isAirBlock(pos1) && dest.isBlockLoaded(pos2) && tryToUseEnergy(ConfigurationHandler.blockCost, energyStorage)){ //don't bother moving air, or trying to move to nonexistent spots, or moving if there is insufficient energy

            //LogHelper.info("Origin Chunk coords: " + (x1 & 15) + " " + y1 + " " + (z1 & 15));
            //LogHelper.info("Origin Chunk coords: " + (x2 & 15) + " " + y2 + " " + (z2 & 15));
            Block oldBlock = oChunk.getBlock(x1 & 15, y1 ,z1 & 15);
            Block newBlock = dChunk.getBlock(x2 & 15, y2, z2 & 15);
            IBlockState oldState = origin.getBlockState(pos1);
            IBlockState newState = dest.getBlockState(pos2);
            float oldHard = oldBlock.getBlockHardness(origin, pos1);
            float newHard = newBlock.getBlockHardness(dest, pos2);
            //LogHelper.info("  Teleporting " + oldBlock.getLocalizedName() + " (" + oldHard + ") to " + newBlock.getLocalizedName() + " (" + newHard + ")");

            if( (oldHard != -1) && (newHard != -1) && (oldHard > newHard)){ //do not teleport if either end is indestructible, only overwrite if teleported block is more durable
                //LogHelper.info("Moving Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " to x:" + x2 + " y:" + y2 + " z:" + z2);
                portalParticles(origin, x1, y1, z1);
                portalParticles(dest, x2, y2, z2);


                //dest.removeTileEntity(pos2);
                //break replaced block
                if(!dest.isAirBlock(pos2)){
                    breakBlock(newBlock, dest, pos2);
                }


                //move block id, metadata
                ExtendedBlockStorage destBlockStorage = dChunk.getBlockStorageArray()[y2 >> 4];

                if (destBlockStorage == null) {
                    destBlockStorage = dChunk.getBlockStorageArray()[y2 >> 4] = new ExtendedBlockStorage(y2 >> 4 << 4, !dChunk.getWorld().provider.getHasNoSky());
                }

                destBlockStorage.set(x2 & 15, y2 & 15, z2 & 15, oldState);

                dChunk.setChunkModified();
                //dest.markAndNotifyBlock(pos2, dChunk, oldState, newState, 2);
                //dest.setBlockMetadataWithNotify(x2, y2, z2, origin.getBlockMetadata(x1,y1,z1), 2); //what does extra argument mean?
                dest.markBlocksDirtyVertical(x2, z2, 0, 256);


                dest.markBlockForUpdate(pos2);//so client actually gets message that block changed


                //dest.addBlockEvent(pos2, oldBlock, 0, 2);
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
                    tileOrig.setPos(pos2);
                    tileOrig.setWorldObj(dest);
                    origin.removeTileEntity(pos1);//also invalidates
                    tileOrig.validate();
                    dest.setTileEntity(pos2, tileOrig);//only works if valid
                    //dest.addTileEntity(tileOrig);
                    //LogHelper.info("<<<<< Checking " + pos2 + " after moving tileentity >>>>>");
//                    NBTTagCompound data = new NBTTagCompound();
//                    for(TileEntity te : dest.loadedTileEntityList){
//                        if(te.getPos().equals(pos2)){
//                            LogHelper.info("  *There is a " + te.getBlockType().getLocalizedName() + " here called " + te + " at " + te.getPos());
//                            te.writeToNBT(data);
//                            LogHelper.info( "    -NBTData:" + data.toString());
//                        }
//                    }
                    dest.setBlockState(pos2, oldState);
                    /*
                    NBTTagCompound data = new NBTTagCompound();
                    tileOrig.writeToNBT(data);

                    TileEntity tileentity = dest.getTileEntity(pos2);
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    tileentity.writeToNBT(nbttagcompound);

                    for (String s : data.getKeySet())
                    {
                        NBTBase nbtbase = data.getTag(s);

                        if (!s.equals("x") && !s.equals("y") && !s.equals("z"))
                        {
                            nbttagcompound.setTag(s, nbtbase.copy());
                        }
                    }

                    tileentity.readFromNBT(nbttagcompound);
                    tileentity.markDirty();

                    /*
                    tileOrig.invalidate();
                    TileEntity tileNew = TileEntity.createAndLoadEntity(data);
                    tileNew.setWorldObj(dest);
                    tileNew.setPos(pos2);
                    dest.setTileEntity(pos2, tileNew);

//                    TileEntity bork = dest.getChunkFromBlockCoords(pos2).getTileEntity(pos2, Chunk.EnumCreateEntityType.IMMEDIATE);//getTileEntity(pos2);
//                    bork.readFromNBT(data);
//                    TileEntity tew = dest.getTileEntity(pos2);
//                    if( !bork.equals(tew) ) LogHelper.error("WUGFIUEGFUGFSUEGFIEFG");
//                    dest.loadedTileEntityList.remove(bork);
//                    dest.getChunkFromBlockCoords(pos2).removeTileEntity(pos2);
//                    bork.onChunkUnload();
//                    List justBork = new ArrayList<TileEntity>();
//                    justBork.add(bork);
//                    dest.tickableTileEntities.removeAll(justBork);
//                    dest.loadedTileEntityList.removeAll(justBork);

                    LogHelper.info("<<<<< Checking " + pos2 + " after moving tileentity >>>>>");
                    for(TileEntity te : dest.loadedTileEntityList){
                        if(te.getPos().equals(pos2)){
                            LogHelper.info("  There is a " + te.getBlockType().getLocalizedName() + " here called " + te + " at " + te.getPos());
                            te.writeToNBT(data);
                            LogHelper.info( "    NBTData:" + data.toString());
                            //dest.markTileEntityForRemoval(te);
                        }
                    }

//                    dest.getTileEntity(pos2).invalidate();

//                    if( (new BlockPos(1,2,3)).equals(new BlockPos(1,2,3))){
//                        LogHelper.info("<<<<<BlockPos works like that>>>>>");
//                    }


/*
                    LogHelper.info("Found Tile Entity " + tileOrig.toString() + " at x:" + x1 + " y:" + y1 + " z:" + z1);

                    NBTTagCompound nbtData = new NBTTagCompound();
                    tileOrig.writeToNBT(nbtData);

                    LogHelper.info("  reading NBT");
                    LogHelper.info("    " + nbtData.toString());


                    nbtData.setInteger("x", x2);
                    nbtData.setInteger("y", y2);
                    nbtData.setInteger("z", z2);
//                    LogHelper.info("  changing NBT");
//                    LogHelper.info("    " + nbtData.toString());

                    //if(dest.getTileEntity(pos2)!=null){ LogHelper.info( "    There is already a " + dest.getTileEntity(pos2).toString() + " here!" ); }

                    //tileentity is created with blocks
                    TileEntity tileNew = TileEntity.createAndLoadEntity(nbtData); //dest.getTileEntity(x2, y2, z2);

//                    //does this cause problems for opencomputers?
//                    NBTTagCompound nbtNew = new NBTTagCompound();
//                    tileNew.writeToNBT(nbtNew);
//                    LogHelper.info("  new NBT");
//                    LogHelper.info("    " + nbtNew.toString());
//                    tileNew.readFromNBT(nbtData);
//
//                    dest.removeTileEntity(pos2);
                    //tileNew.setWorldObj(dest);
                    //tileNew.setPos(pos2);
                    //dest.addTileEntity(tileNew);
                    dest.setTileEntity(pos2, tileNew);

                    //TileEntity tileNew = dest.getTileEntity(pos2);
                    //tileNew.readFromNBT(nbtData);
                    //tileNew.markDirty();
                    //dest.markBlockForUpdate(pos2);

                    NBTTagCompound nbtNew = new NBTTagCompound();
                    tileNew.writeToNBT(nbtNew);
                    //dest.getTileEntity(pos2).writeToNBT(nbtNew);
                    LogHelper.info("  written NBT");
                    LogHelper.info("    " + nbtNew.toString());


                    //it really SHOULDN'T be necessary to do this again...
                    //destBlockStorage.setExtBlockMetadata(x2 & 15, y2 & 15, z2 & 15, origin.getBlockMetadata(x1, y1, z1));
                    destBlockStorage.set(x2 & 15, y2 & 15, z2 & 15, oldState);
                    tileNew.updateContainingBlockInfo();//do I need this?

                    //remove old tileentity
                    //origin.removeTileEntity(pos1);
                    tileOrig.invalidate();
                    //tileOrig.updateContainingBlockInfo();//do I need this?
                    */
                }






                //remove original block if it was moved
                //origin.setBlock(x1, y1, z1, Blocks.air, 0, 2);
                ExtendedBlockStorage originBlockStorage = oChunk.getBlockStorageArray()[y1 >> 4];

                if (originBlockStorage == null) {
                    originBlockStorage = oChunk.getBlockStorageArray()[y1 >> 4] = new ExtendedBlockStorage(y1 >> 4 << 4, !oChunk.getWorld().provider.getHasNoSky());
                }


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
            LogHelper.info("Ticket is null, not releasing");
        }
        //seems to work ok without it though, old tickets are probably auto-recycled

        return true;

    }

    public static boolean moveEntity(World origin, World dest, Entity entity, double x, double y, double z){
        int destDim = dest.provider.getDimensionId();
        LogHelper.info("Moving entity " + entity.getName() +  " to x:" + x + ", y:" + y + ", z:" + z + " and dimension:" + destDim);
        portalParticles(origin, entity.posX, entity.posY, entity.posZ);
        portalParticles(dest, x, y, z);


        if( entity instanceof EntityPlayerMP){ //regular setting position won't work on player :/
            LogHelper.info("    Entity is a player");
            EntityPlayerMP player = (EntityPlayerMP)entity;
/*
            player.fallDistance = 0F;

            if(origin.provider.getDimensionId() == destDim)
            {
                if(x == player.posX && y == player.posY && z == player.posZ) return true;

                player.playerNetServerHandler.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
                return true;
            }

            int from = player.dimension;
            float rotationYaw = player.rotationYaw;
            float rotationPitch = player.rotationPitch;
            MinecraftServer server = MinecraftServer.getServer();
            WorldServer fromDim = server.worldServerForDimension(from);
            WorldServer toDim = server.worldServerForDimension(destDim);

            if(player != null)
            {
                server.getConfigurationManager().transferPlayerToDimension(player, destDim, new WarpCoreTeleporter(toDim));
                if(from == 1 && entity.isEntityAlive())
                {
                    // get around vanilla End hacks
                    toDim.spawnEntityInWorld(entity);
                    toDim.updateEntityWithOptionalForce(entity, false);
                }
            }
            else
            {
                NBTTagCompound tagCompound = new NBTTagCompound();
                entity.writeToNBT(tagCompound);
                Class<? extends Entity> entityClass = entity.getClass();
                fromDim.removeEntity(entity);

                try
                {
                    Entity newEntity = entityClass.getConstructor(World.class).newInstance(toDim);
                    newEntity.readFromNBT(tagCompound);
                    newEntity.setLocationAndAngles(x, y, z, rotationYaw, rotationPitch);
                    newEntity.forceSpawn = true;
                    toDim.spawnEntityInWorld(newEntity);
                    newEntity.forceSpawn = false;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            player.fallDistance = 0;
            player.rotationYaw = rotationYaw;
            player.rotationPitch = rotationPitch;
            if(player != null) player.setPositionAndUpdate(x, y, z);
            else entity.setPosition(x, y, z);
            return true;
*/


            if( origin.provider.getDimensionId() != destDim) {
                WorldServer s1 = MinecraftServer.getServer().worldServerForDimension(player.dimension);
                WorldServer s2 = MinecraftServer.getServer().worldServerForDimension(destDim);
                ServerConfigurationManager mgr = s2.getMinecraftServer().getConfigurationManager();

                //yes, this is just all the stuff that normally gets called by travelToDimension()
                //because the end is dumb
                player.dimension = destDim;
                player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, s2.getDifficulty(), s2.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
                s1.removePlayerEntityDangerously(player);
                player.isDead = false;
                player.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);
                WarpCoreTeleporter tele = new WarpCoreTeleporter(s2);
                tele.placeInPortal(player, player.rotationYaw);
                s2.spawnEntityInWorld(player);
                s2.updateEntityWithOptionalForce(player, false);
                player.setWorld(s2);
                mgr.preparePlayer(player, s1);//yes, this is old world, because that is where player is removed from
                player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
                player.theItemInWorldManager.setWorld(s2);
                mgr.updateTimeAndWeatherForPlayer(player, s2);
                mgr.syncPlayerInventory(player);
                for (PotionEffect potioneffect : player.getActivePotionEffects()) {
                    player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
                }
                net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, origin.provider.getDimensionId(), destDim);

            }else{
                player.setPositionAndUpdate(x,y,z);
            }


/*
            if( origin.provider.getDimensionId() != destDim) {
                //if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(player, destDim)) return false;
                if ( player.dimension == 1 ) { //leaving the end
                    player.triggerAchievement(AchievementList.theEnd2);
                    player.worldObj.removeEntity(player);
                    player.playerConqueredTheEnd = true;
                    //player.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(4, 0.0F));//this tries to do end poem gui, GuiWinGame

                    //WarpCoreTeleporter tele = new WarpCoreTeleporter((WorldServer) dest);
                    player.mcServer.getConfigurationManager().transferPlayerToDimension(player, destDim, new WarpCoreTeleporter((WorldServer) dest));
                    //tele.placeInPortal(player, player.rotationYaw);
                    //dest.spawnEntityInWorld(player); //these are already handled for other dimensions.  doing them more than once causes problems with duplicate entity registrations
                    //player.markPlayerActive();
                    //player = ((WorldServer) dest).getMinecraftServer().getConfigurationManager().recreatePlayerEntity(player, 0, true);
                    player.setPositionAndUpdate(x, y, z);
                } else {
                    if ( destDim == 1 ) {//enter the end
                        player.triggerAchievement(AchievementList.theEnd);
//                        BlockPos blockpos = player.mcServer.worldServerForDimension(destDim).getSpawnCoordinate();
//
//                        if (blockpos != null) {
//                            player.playerNetServerHandler.setPlayerLocation((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ(), 0.0F, 0.0F);
//                        }
                        //player.playerNetServerHandler.setPlayerLocation(x, y, z, 0.0F, 0.0F);

                        //WarpCoreTeleporter tele = new WarpCoreTeleporter((WorldServer) dest);
                        player.mcServer.getConfigurationManager().transferPlayerToDimension(player, destDim, tele);
                        tele.placeInPortal(player, player.rotationYaw);
                        dest.spawnEntityInWorld(player);

                        destDim = 1;
                    } else {
                        //player.triggerAchievement(AchievementList.portal);
                        player.mcServer.getConfigurationManager().transferPlayerToDimension(player, destDim, new WarpCoreTeleporter((WorldServer) dest));
                    }


                    player.setPositionAndUpdate(x, y, z);
//                    if ( origin.provider.getDimensionId() == 1 ) {
//                        // For some reason teleporting out of the end does weird things.
//                        player = ((WorldServer) dest).getMinecraftServer().getConfigurationManager().recreatePlayerEntity(player, origin.provider.getDimensionId(), true);
//
//                        player.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);
//                        dest.spawnEntityInWorld(player); //these are already handled for other dimensions.  doing them more than once causes problems with duplicate entity registrations
//                        dest.updateEntityWithOptionalForce(player, false);
//                    }

//                player.lastExperience = -1;
//                player.lastHealth = -1.0F;
//                player.lastFoodLevel = -1;
                }
            }else{
                player.setPositionAndUpdate(x, y, z);
            }
            /*
            if (player.riddenByEntity != null) {
                player.riddenByEntity.mountEntity(null);
            }
            if (player.ridingEntity != null) {
                player.mountEntity(null);
            }

            //player.travelToDimension(destDim);
            if( origin != dest ) {

                //ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();
                WorldServer s2 = MinecraftServer.getServer().worldServerForDimension(destDim);
                //manager.transferPlayerToDimension((EntityPlayerMP) player, destDim, new WarpCoreTeleporter(s2));
                ((EntityPlayerMP)player).mcServer.getConfigurationManager().transferPlayerToDimension((EntityPlayerMP)player, destDim, new WarpCoreTeleporter(s2));

                //manager.updateTimeAndWeatherForPlayer((EntityPlayerMP) player, s2);//does not seem to help weird sky after leaving end
                //manager.syncPlayerInventory(player);

                //MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, destDim, MinecraftServer.getServer().worldServerForDimension(destDim).getDefaultTeleporter());
                if ( origin.provider.getDimensionId() == 1 ) {
                    // For some reason teleporting out of the end does weird things.
                    //player.setPositionAndUpdate(player.posX + dx, player.posY + dy, player.posZ + dz);
                    player.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);
                    //WarpCoreTeleporter tele = new WarpCoreTeleporter(s2);
                    //tele.placeInPortal(player, player.rotationYaw);
                    s2.spawnEntityInWorld(player); //these are already handled for other dimensions.  doing them more than once causes problems with duplicate entity registrations
                    s2.updateEntityWithOptionalForce(player, false);
                }
            }
            */




            dest.playSoundAtEntity(player, "mob.endermen.portal", 1, 1);//entity, sound, volume, pitch

        }else{ //not player
            if( origin.provider.getDimensionId() != destDim) {
                WorldServer s1 = MinecraftServer.getServer().worldServerForDimension(origin.provider.getDimensionId());
                WorldServer s2 = MinecraftServer.getServer().worldServerForDimension(destDim);
                ServerConfigurationManager mgr = s2.getMinecraftServer().getConfigurationManager();

                //yes, this is just all the stuff that normally gets called by travelToDimension()
                //because the end is dumb
                entity.dimension = destDim;
                s1.removeEntity(entity);
                entity.isDead = false;
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
                WarpCoreTeleporter tele = new WarpCoreTeleporter(s2);
                tele.placeInPortal(entity, entity.rotationYaw);
                s2.spawnEntityInWorld(entity);
                s2.updateEntityWithOptionalForce(entity, false);
                entity.setWorld(s2);
                Entity entityNew = EntityList.createEntityByName(EntityList.getEntityString(entity), s2);
                if (entityNew != null)
                {
                    entityNew.copyDataFromOld(entity);
                    s2.spawnEntityInWorld(entityNew);
                }

                entity.isDead = true;
                s1.resetUpdateEntityTick();
                s2.resetUpdateEntityTick();

                /*
                //entity.travelToDimension(destDim);//already doing this, but with own teleporter
                WorldServer s1 = MinecraftServer.getServer().worldServerForDimension(origin.provider.getDimensionId());
                WorldServer s2 = MinecraftServer.getServer().worldServerForDimension(destDim);
                MinecraftServer.getServer().getConfigurationManager().transferEntityToWorld(entity, origin.provider.getDimensionId(), s1, s2, new WarpCoreTeleporter(s2));
                */

            }
            entity.setPosition(x, y, z);
        }




        LogHelper.info("    Moved entity to x:" + entity.posX + ", y:" + entity.posY + ", z:" + entity.posZ + " and dimension:" + entity.worldObj.provider.getDimensionId());
        return true;
    }

    private static void breakBlock(Block block, World world, BlockPos pos){
        block.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
        world.setBlockToAir( pos );
        world.removeTileEntity( pos );
    }

    public static boolean tryToUseEnergy(int energy, EnergyStorage energyStorage){
        if( !ConfigurationHandler.useEnergy ){//to disable energy use if rf not installed
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
    public static boolean moveBlockWorld(World origin, World dest, BlockPos pos1, BlockPos pos2, EnergyStorage energyStorage){
        int x1 = pos1.getX(); int y1 = pos1.getY(); int z1 = pos1.getZ();
        int x2 = pos2.getX(); int y2 = pos2.getY(); int z2 = pos2.getZ();

        //MinecraftServer.getServer().worldTickTimes.put(dest.provider.dimensionId, new long[100]);
        //LogHelper.info("Max Ticket Length: " + ForgeChunkManager.getMaxTicketLengthFor(Reference.MOD_ID));
        //LogHelper.info("Max Chunks Depth: " + ForgeChunkManager.getMaxChunkDepthFor(Reference.MOD_ID));
        //LogHelper.info("Tickets Available: " + ForgeChunkManager.ticketCountAvailableFor(FluxWarp.instance, dest));
        ForgeChunkManager.Ticket ticket = null;
        ticket = ForgeChunkManager.requestTicket(TeleFlux.instance, dest, ForgeChunkManager.Type.NORMAL);



        if( origin.isAirBlock(pos1) ){
            //LogHelper.info("Air Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " , not moving");
            return true;
        }else if( !dest.isBlockLoaded(pos2) ){
            LogHelper.info("Destination block at x:" + x2 + " y:" + y2 + " z:" + z2 + " does not exist, trying to load it");

            if(ticket == null){
                LogHelper.info("Failed to get chunk loading ticket, quitting");
                return false;
            }
            ForgeChunkManager.forceChunk( ticket, dest.getChunkFromBlockCoords(pos2).getChunkCoordIntPair() );
            ImmutableSet<ChunkCoordIntPair> chunks = ticket.getChunkList();        //getMaxChunkListDepth()
            LogHelper.info("Chunks Loaded:" + chunks.size());
        }

        if(!origin.isAirBlock(pos1) && dest.isBlockLoaded(pos2) && tryToUseEnergy(ConfigurationHandler.blockCost, energyStorage)){ //don't bother moving air, or trying to move to nonexistent spots

            Block oldBlock = origin.getBlockState(pos1).getBlock();
            Block newBlock = dest.getBlockState(pos2).getBlock();
            float oldHard = oldBlock.getBlockHardness(origin, pos1);
            float newHard = newBlock.getBlockHardness(dest, pos2);

            if( (oldHard != -1) && (newHard != -1) && (oldHard > newHard)){ //do not teleport if either end is indestructible, only overwrite if teleported block is more durable
                //LogHelper.info("Moving Block at x:" + x1 + " y:" + y1 + " z:" + z1 + " to x:" + x2 + " y:" + y2 + " z:" + z2);
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

                    LogHelper.info("  reading NBT");
                    LogHelper.info("    " + nbtData.toString());

                    //LogHelper.info("  changing NBT");
                    nbtData.setInteger("x", x2);
                    nbtData.setInteger("y", y2);
                    nbtData.setInteger("z", z2);
                    //LogHelper.info("    " + nbtData.toString());


                    //dest.addTileEntity(TileEntity.createAndLoadEntity(nbtData));

                    //tileentity is created with blocks
                    TileEntity tileNew = dest.getTileEntity(pos2);
                    if (tileNew != null) {
                        tileNew.readFromNBT(nbtData);//load data from old tileentity
                        tileNew.markDirty();//chunk has changed data, so save it next time you save world
                        dest.markBlockForUpdate(pos2);//tell client
//                        try{
//                            Chunk chunk = dest.getChunkFromBlockCoords(pos2);
//                            MinecraftServer.getServer().worldServerForDimension(dest.provider.getDimensionId()).send
//                            MinecraftServer.getServer().worldServerForDimension(dest.provider.getDimensionId()).theChunkProviderServer.chunkLoader.saveChunk(dest, chunk);
//                            MinecraftServer.getServer().worldServerForDimension(dest.provider.getDimensionId()).theChunkProviderServer.chunkLoader.loadChunk(dest, chunk.xPosition, chunk.zPosition );
//                        }catch( IOException e ){
//                            LogHelper.warn("IOEXception trying to reload chunks");
//                        }catch( MinecraftException e){
//                            LogHelper.warn("MinecraftException trying to reload chunks");
//                        }


                        LogHelper.info("  new NBT");
                        NBTTagCompound nbtNew = new NBTTagCompound();
                        tileNew.writeToNBT(nbtNew);
                        LogHelper.info("    " + nbtNew.toString());
                        //tileNew.readFromNBT(nbtData);
                    }

                    //it really SHOULDN'T be necessary to do this again...
                    //dest.setBlockState( pos2, origin.getBlockState(pos1), 2);

                    //remove old tileentity
                    tileOrig.invalidate();
                    //origin.removeTileEntity(pos1);
                    //tileOrig.markDirty();
                    //tileOrig.updateContainingBlockInfo();
//                    if( origin.tickableTileEntities.contains(tileOrig)){
//                        origin.tickableTileEntities.remove(tileOrig);
//                    }
                    //}
                }

                //remove original block if it was moved
                origin.setBlockState(pos1, Blocks.air.getDefaultState(), 2);

            }else if((oldHard != -1)  && (oldHard <= newHard || newHard == -1)){
                breakBlock(oldBlock, origin, pos1);
            }

        }else if( !dest.isBlockLoaded(pos2 )){return false;}//out of world bounds

        ForgeChunkManager.unforceChunk(ticket, dest.getChunkFromBlockCoords(pos2).getChunkCoordIntPair());
        //why does this break things for later warps????? (wait, never mind, I think that was something else)
        if (ticket != null) { ForgeChunkManager.releaseTicket(ticket); }
        //seems to work ok without it though, old tickets are probably auto-recycled

        return true;
    }


}
