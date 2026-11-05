package com.kahzerx.kahzerxmod.extensions.sitExtension;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SitEntity extends ArmorStand implements SitEntityInterface {
    private boolean sitEntity = false;
    public SitEntity(Level world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public boolean isSitEntity() {
        return sitEntity;
    }

    private byte setBitField(byte value, boolean set) {
        return set ? (byte)(value | 16) : (byte)(value & ~16);
    }

    @Override
    public void setSitEntity(boolean sitEntity) {
        this.sitEntity = sitEntity;
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBitField((Byte)this.entityData.get(DATA_CLIENT_FLAGS), sitEntity));
        this.setInvisible(sitEntity);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        if (this.isSitEntity()) {
            this.setPos(this.getX(), this.getY() + 0.16, this.getZ());
            if (this.level() instanceof ServerLevel serverLevel) {
                // ArmorStand#kill(level, null) la quita sin soltar el item (como el antiguo kill()).
                this.kill(serverLevel, null);
            }
        }
        super.removePassenger(passenger);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.sitEntity) {
            output.putBoolean("SitEntity", true);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.sitEntity = input.getBooleanOr("SitEntity", false);
    }
}
