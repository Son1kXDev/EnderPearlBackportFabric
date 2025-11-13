package com.enjine.enderpearlbackport;

import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.*;


public class EnderpearlChunkManager {

    public record PearlKey(RegistryKey<World> worldKey, UUID pearlUuid) {}

    private static final Map<PearlKey, ChunkPos> FORCED_CHUNKS = new HashMap<>();
    private static final Set<PearlKey> ALIVE_THIS_TICK = new HashSet<>();

    public static void beginTick() {
        ALIVE_THIS_TICK.clear();
    }

    public static void trackPearl(ServerWorld world, EnderPearlEntity pearl) {
        if (pearl.isRemoved()) return;

        RegistryKey<World> worldKey = world.getRegistryKey();
        PearlKey key = new PearlKey(worldKey, pearl.getUuid());

        BlockPos pos = pearl.getBlockPos();
        ChunkPos chunkPos = new ChunkPos(pos);

        ChunkPos prev = FORCED_CHUNKS.get(key);
        if (prev == null || prev.x != chunkPos.x || prev.z != chunkPos.z) {
            if (prev != null) {
                setChunkForced(world, prev, false);
            }
            setChunkForced(world, chunkPos, true);
            FORCED_CHUNKS.put(key, chunkPos);
        }

        ALIVE_THIS_TICK.add(key);
    }

    public static void endTick(MinecraftServer server) {
        Iterator<Map.Entry<PearlKey, ChunkPos>> it = FORCED_CHUNKS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<PearlKey, ChunkPos> entry = it.next();
            PearlKey key = entry.getKey();
            if (!ALIVE_THIS_TICK.contains(key)) {
                ServerWorld world = server.getWorld(key.worldKey());
                if (world != null) {
                    setChunkForced(world, entry.getValue(), false);
                }
                it.remove();
            }
        }

        ALIVE_THIS_TICK.clear();
    }


    private static void setChunkForced(ServerWorld world, ChunkPos pos, boolean forced) {
        world.setChunkForced(pos.x, pos.z, forced);
    }


    public static void releaseAll(MinecraftServer server) {
        for (Map.Entry<PearlKey, ChunkPos> entry : FORCED_CHUNKS.entrySet()) {
            ServerWorld world = server.getWorld(entry.getKey().worldKey());
            if (world != null) {
                setChunkForced(world, entry.getValue(), false);
            }
        }
        FORCED_CHUNKS.clear();
        ALIVE_THIS_TICK.clear();
    }
}
