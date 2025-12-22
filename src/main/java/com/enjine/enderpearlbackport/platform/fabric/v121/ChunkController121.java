package com.enjine.enderpearlbackport.platform.fabric.v121;

import com.enjine.enderpearlbackport.platform.fabric.bridge.VersionedChunkController;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class ChunkController121 implements VersionedChunkController {

    private final MinecraftServer server;

    public ChunkController121(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void force(String dim, ChunkPos pos) {
        ServerWorld w = world(dim);
        if (w != null) w.setChunkForced(pos.x, pos.z, true);
    }

    @Override
    public void release(String dim, ChunkPos pos) {
        ServerWorld w = world(dim);
        if (w != null) w.setChunkForced(pos.x, pos.z, false);
    }

    private ServerWorld world(String dim) {
        Identifier id = Identifier.tryParse(dim);
        if (id == null) return null;
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
    }
}
