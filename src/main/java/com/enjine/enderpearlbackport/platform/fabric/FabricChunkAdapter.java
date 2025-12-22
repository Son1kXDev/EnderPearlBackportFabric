package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.api.ChunkAdapter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class FabricChunkAdapter implements ChunkAdapter {

    private final MinecraftServer server;

    public FabricChunkAdapter(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void forceLoad(String dimensionId, int chunkX, int chunkZ) {
        ServerWorld world = getWorld(dimensionId);
        if (world != null) {
            world.setChunkForced(chunkX, chunkZ, true);
        }
    }

    @Override
    public void release(String dimensionId, int chunkX, int chunkZ) {
        ServerWorld world = getWorld(dimensionId);
        if (world != null) {
            world.setChunkForced(chunkX, chunkZ, false);
        }
    }

    private ServerWorld getWorld(String id) {
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) return null;

        RegistryKey<World> key =
                RegistryKey.of(RegistryKeys.WORLD, identifier);
        return server.getWorld(key);
    }
}
