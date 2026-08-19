package com.enjine.enderpearlbackport;

import com.enjine.enderpearlbackport.api.ServerPlayerEntityAccessor;
import com.enjine.enderpearlbackport.platform.fabric.FabricPearlMechanics;
import com.enjine.enderpearlbackport.platform.fabric.bridge.FabricVersionBridge;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Enderpearlbackport implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            FabricVersionBridge.init(server);
            FabricPearlMechanics.migrateOrphanedForcedChunks(server);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (!(player instanceof ServerPlayerEntityAccessor accessor)) return;

            Set<UUID> pearlIds = new ArrayList<>(accessor.epb$getEnderPearls()).stream().collect(java.util.stream.Collectors.toSet());
            for (UUID pearlId : pearlIds) {
                EnderPearlEntity pearl = findPearl(server, pearlId);
                if (pearl != null && !pearl.isRemoved()) {
                    NbtCompound pearlNbt = new NbtCompound();
                    pearl.writeNbt(pearlNbt);
                    pearlNbt.putString("epb$dim", pearl.getWorld().getRegistryKey().getValue().toString());
                    accessor.epb$addPendingPearlNbt(pearlNbt);
                    pearl.discard();
                }
            }
        });
    }

    private static EnderPearlEntity findPearl(MinecraftServer server, UUID pearlId) {
        for (ServerWorld world : server.getWorlds()) {
            Entity entity = world.getEntity(pearlId);
            if (entity instanceof EnderPearlEntity pearl && !pearl.isRemoved()) {
                return pearl;
            }
        }
        return null;
    }
}
