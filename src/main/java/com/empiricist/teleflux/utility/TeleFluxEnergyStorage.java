package com.empiricist.teleflux.utility;

import net.minecraftforge.fml.common.Optional;
import net.minecraft.nbt.NBTTagCompound;

//reimplementation so it can still run without cofhapi being installed necessarily

@Optional.Interface(iface = "cofh.api.energy.IEnergyStorage", modid = "CoFHAPI|energy", striprefs = true)
public class TeleFluxEnergyStorage {//implements IEnergyStorage{
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public TeleFluxEnergyStorage(int capacity){
        this(capacity, capacity, capacity);
    }
    public TeleFluxEnergyStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }
    public TeleFluxEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    //@Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));

        if (!simulate) {
            energy += energyReceived;
        }
        return energyReceived;
    }

    //@Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));

        if (!simulate) {
            energy -= energyExtracted;
        }
        return energyExtracted;
    }

    //@Override
    public int getEnergyStored() {
        return energy;
    }

    //@Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    public TeleFluxEnergyStorage readFromNBT(NBTTagCompound nbt) {

        this.energy = nbt.getInteger("Energy");

        if (energy > capacity) {
            energy = capacity;
        }
        return this;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

        if (energy < 0) {
            energy = 0;
        }
        nbt.setInteger("Energy", energy);
        return nbt;
    }

    public void setCapacity(int capacity) {

        this.capacity = capacity;

        if (energy > capacity) {
            energy = capacity;
        }
    }

    public void setMaxTransfer(int maxTransfer) {

        setMaxReceive(maxTransfer);
        setMaxExtract(maxTransfer);
    }

    public void setMaxReceive(int maxReceive) {

        this.maxReceive = maxReceive;
    }

    public void setMaxExtract(int maxExtract) {

        this.maxExtract = maxExtract;
    }

    public int getMaxReceive() {

        return maxReceive;
    }

    public int getMaxExtract() {

        return maxExtract;
    }

    public void setEnergyStored(int energy){
        this.energy = energy;
    }
}
