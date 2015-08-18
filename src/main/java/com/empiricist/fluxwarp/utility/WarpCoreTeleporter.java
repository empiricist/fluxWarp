package com.empiricist.fluxwarp.utility;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class WarpCoreTeleporter extends Teleporter {

    public WarpCoreTeleporter(WorldServer server) {
        super(server);
    }

    public boolean makePortal(Entity entity) { return true; }

    public void placeInPortal(Entity p_77185_1_, double p_77185_2_, double p_77185_4_, double p_77185_6_, float p_77185_8_){
        this.placeInExistingPortal(p_77185_1_, p_77185_2_, p_77185_4_, p_77185_6_, p_77185_8_);
    }

    public boolean placeInExistingPortal(Entity entity, double what1, double what2, double what3, float p_77184_8_){
        entity.setLocationAndAngles(what1, what2, what3, entity.rotationYaw, entity.rotationPitch);
        return true;
    }
}
