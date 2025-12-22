package com.enjine.enderpearlbackport.platform.fabric.bridge;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import com.enjine.enderpearlbackport.platform.fabric.bridge.FabricVersionBridge;
import com.enjine.enderpearlbackport.platform.fabric.bridge.VersionedPearlHooks;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PearlHooks implements VersionedPearlHooks {

    @Override
    public void onPearlCollision(ServerPlayerEntity player, EnderPearlEntity pearl) {
        if (player.getWorld() != pearl.getWorld()) {
            FabricVersionBridge.teleport.teleport(
                    player.getUuid(),
                    new EnderpearlRecord(
                            pearl.getUuid(),
                            pearl.getWorld().getRegistryKey().getValue().toString(),
                            pearl.getX(), pearl.getY(), pearl.getZ(),
                            pearl.getVelocity().x, pearl.getVelocity().y, pearl.getVelocity().z
                    )
            );
        }
    }
}
