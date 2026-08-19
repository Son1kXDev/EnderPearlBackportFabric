package com.enjine.enderpearlbackport.api;

import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ServerPlayerEntityAccessor {
    Set<UUID> epb$getEnderPearls();
    void epb$registerEnderPearl(EnderPearlEntity pearl);
    void epb$deregisterEnderPearl(EnderPearlEntity pearl);
    void epb$addPendingPearlNbt(NbtCompound nbt);
    List<NbtCompound> epb$getPendingPearlNbt();
    void epb$spawnPendingPearls();
}
