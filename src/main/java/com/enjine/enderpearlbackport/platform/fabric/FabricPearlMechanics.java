package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.api.Platform;
import com.enjine.enderpearlbackport.common.data.EnderpearlData;
import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import com.enjine.enderpearlbackport.platform.fabric.bridge.FabricVersionBridge;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.*;

public final class FabricPearlMechanics {

    private FabricPearlMechanics() {}

    private record PearlKey(String dim, UUID pearlUuid) {}
    private static final Map<PearlKey, ChunkPos> FORCED = new HashMap<>();
    private static final Map<String, Map<ChunkPos, Integer>> CHUNK_REFCOUNT = new HashMap<>();
    private static final Set<PearlKey> ALIVE = new HashSet<>();

    public static void onEndServerTick(MinecraftServer server) {
        ALIVE.clear();

        for (ServerWorld world : server.getWorlds()) {
            String dim = world.getRegistryKey().getValue().toString();

            List<? extends EnderPearlEntity> pearls =
                    world.getEntitiesByType(EntityType.ENDER_PEARL, p -> true);

            for (EnderPearlEntity pearl : pearls) {
                if (pearl.isRemoved()) continue;

                PearlKey key = new PearlKey(dim, pearl.getUuid());
                BlockPos bp = pearl.getBlockPos();
                ChunkPos cp = new ChunkPos(bp);

                ChunkPos prev = FORCED.get(key);

                if (prev == null || !prev.equals(cp)) {
                    if (prev != null) {
                        decrementChunk(dim, prev);
                    }
                    incrementChunk(dim, cp);
                    FORCED.put(key, cp);
                }

                ALIVE.add(key);
            }
        }

        Iterator<Map.Entry<PearlKey, ChunkPos>> it = FORCED.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<PearlKey, ChunkPos> e = it.next();
            if (!ALIVE.contains(e.getKey())) {
                decrementChunk(e.getKey().dim, e.getValue());
                it.remove();
            }
        }
    }

    private static void incrementChunk(String dim, ChunkPos pos) {
        Map<ChunkPos, Integer> map =
                CHUNK_REFCOUNT.computeIfAbsent(dim, k -> new HashMap<>());

        int count = map.getOrDefault(pos, 0);
        if (count == 0) {
            FabricVersionBridge.chunk.force(dim, new ChunkPos(pos.x, pos.z));
        }
        map.put(pos, count + 1);
    }

    private static void decrementChunk(String dim, ChunkPos pos) {
        Map<ChunkPos, Integer> map = CHUNK_REFCOUNT.get(dim);
        if (map == null) return;

        int count = map.getOrDefault(pos, 0) - 1;
        if (count <= 0) {
            map.remove(pos);
            FabricVersionBridge.chunk.release(dim, new ChunkPos(pos.x, pos.z));
        } else {
            map.put(pos, count);
        }

        if (map.isEmpty()) {
            CHUNK_REFCOUNT.remove(dim);
        }
    }

    public static void saveAndRemovePlayerPearls(ServerPlayerEntity player, MinecraftServer server) {
        UUID playerId = player.getUuid();
        List<EnderpearlRecord> list = new ArrayList<>();

        for (ServerWorld world : server.getWorlds()) {
            String dim = world.getRegistryKey().getValue().toString();

            List<? extends EnderPearlEntity> pearls =
                    world.getEntitiesByType(EntityType.ENDER_PEARL, p -> true);

            for (EnderPearlEntity pearl : pearls) {
                if (pearl.isRemoved()) continue;
                if (!(pearl.getOwner() instanceof ServerPlayerEntity owner)) continue;
                if (!owner.getUuid().equals(playerId)) continue;

                list.add(new EnderpearlRecord(
                        pearl.getUuid(),
                        dim,
                        pearl.getX(), pearl.getY(), pearl.getZ(),
                        pearl.getVelocity().x, pearl.getVelocity().y, pearl.getVelocity().z
                ));

                pearl.discard();
            }
        }

        EnderpearlData.savePearls(playerId, list);
    }

    public static void restorePlayerPearls(ServerPlayerEntity player, MinecraftServer server) {
        UUID playerId = player.getUuid();
        List<EnderpearlRecord> list = EnderpearlData.popPearls(playerId);
        if (list.isEmpty()) return;

        for (EnderpearlRecord r : list) {
            Identifier id = Identifier.tryParse(r.dimensionId());
            if (id == null) continue;

            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, id);
            ServerWorld world = server.getWorld(key);
            if (world == null) continue;

            EnderPearlEntity pearl = new EnderPearlEntity(world, player);
            pearl.refreshPositionAndAngles(r.x(), r.y(), r.z(), player.getYaw(), player.getPitch());
            pearl.setVelocity(r.vx(), r.vy(), r.vz());
            world.spawnEntity(pearl);
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
}
