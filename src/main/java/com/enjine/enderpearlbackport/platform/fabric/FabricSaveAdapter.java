package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.api.ChunkAdapter;
import com.enjine.enderpearlbackport.common.api.PlatformAdapter;
import com.enjine.enderpearlbackport.common.api.TeleportAdapter;
import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.UUID;

public class FabricSaveAdapter implements PlatformAdapter {

    private final MinecraftServer server;
    private final ChunkAdapter chunkAdapter;
    private final TeleportAdapter teleportAdapter;

    public FabricSaveAdapter(MinecraftServer server) {
        this.server = server;
        this.chunkAdapter = new FabricChunkAdapter(server);
        this.teleportAdapter = new FabricTeleportAdapter(server);
    }

    @Override public ChunkAdapter chunk() { return chunkAdapter; }
    @Override public TeleportAdapter teleport() { return teleportAdapter; }

    @Override
    public void savePearls(UUID playerId, List<EnderpearlRecord> pearls) {
        ServerWorld overworld = server.getOverworld();
        EnderpearlPersistentState.get(overworld).savePearls(playerId, pearls);
    }

    @Override
    public List<EnderpearlRecord> popPearls(UUID playerId) {
        ServerWorld overworld = server.getOverworld();
        return EnderpearlPersistentState.get(overworld).popPearls(playerId);
    }
}
