package com.enjine.enderpearlbackport.platform.fabric.bridge;

import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface VersionedPearlHooks {

    void onPearlCollision(ServerPlayerEntity player, EnderPearlEntity pearl);
}
