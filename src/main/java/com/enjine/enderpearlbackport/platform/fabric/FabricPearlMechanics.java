package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import com.enjine.enderpearlbackport.platform.fabric.bridge.FabricVersionBridge;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FabricPearlMechanics {

    private FabricPearlMechanics() {}

    private static final Map<UUID, ChunkPos> PEARL_TICKETS = new ConcurrentHashMap<>();

    public static void updatePearlTicket(EnderPearlEntity pearl) {
        if (pearl.isRemoved()) {
            removePearlTicket(pearl);
            return;
        }

        if (!(pearl.getWorld() instanceof ServerWorld world)) return;

        UUID pearlId = pearl.getUuid();
        ChunkPos currentChunk = new ChunkPos(pearl.getBlockPos());
        ChunkPos prevChunk = PEARL_TICKETS.get(pearlId);

        if (prevChunk != null && prevChunk.equals(currentChunk)) return;

        String dim = world.getRegistryKey().getValue().toString();

        if (prevChunk != null) {
            FabricVersionBridge.chunk.removeTicket(dim, prevChunk);
        }

        FabricVersionBridge.chunk.addTicket(dim, currentChunk);
        PEARL_TICKETS.put(pearlId, currentChunk);
    }

    public static void removePearlTicket(EnderPearlEntity pearl) {
        UUID pearlId = pearl.getUuid();
        ChunkPos prevChunk = PEARL_TICKETS.remove(pearlId);
        if (prevChunk != null && pearl.getWorld() instanceof ServerWorld world) {
            String dim = world.getRegistryKey().getValue().toString();
            FabricVersionBridge.chunk.removeTicket(dim, prevChunk);
        }
    }

    public static void ensureCrossDimensionTeleport(ServerPlayerEntity player, EnderPearlEntity pearl) {
        if (!(pearl.getWorld() instanceof ServerWorld pearlWorld)) return;
        if (!(player.getWorld() instanceof ServerWorld playerWorld)) return;

        if (playerWorld != pearlWorld) {
            String dim = pearlWorld.getRegistryKey().getValue().toString();

            FabricVersionBridge.teleport.teleport(
                    player.getUuid(),
                    new EnderpearlRecord(
                            pearl.getUuid(),
                            dim,
                            pearl.getX(), pearl.getY(), pearl.getZ(),
                            pearl.getVelocity().x,
                            pearl.getVelocity().y,
                            pearl.getVelocity().z
                    )
            );
        }
    }

    public static void migrateOrphanedForcedChunks(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            for (long packedPos : world.getForcedChunks().toLongArray()) {
                ChunkPos cp = new ChunkPos(packedPos);
                world.setChunkForced(cp.x, cp.z, false);
            }
        }
    }
}
