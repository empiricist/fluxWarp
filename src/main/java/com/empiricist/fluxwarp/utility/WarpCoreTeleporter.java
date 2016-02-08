package com.empiricist.fluxwarp.utility;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class WarpCoreTeleporter extends Teleporter {

    public WarpCoreTeleporter(WorldServer server) {
        super(server);
    }

    @Override
    public boolean makePortal(Entity entity) {
        return true;
    }

    @Override
    public void placeInPortal(Entity entity, float rotationYaw){
        this.placeInExistingPortal(entity, rotationYaw);
    }

    @Override
    public boolean placeInExistingPortal(Entity entity, float rotationYaw){
        entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
        return true;
    }
}
