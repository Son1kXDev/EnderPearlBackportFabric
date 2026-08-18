package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.api.PlatformAdapter;
import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.UUID;

public class FabricSaveAdapter implements PlatformAdapter {

    private final MinecraftServer server;

    public FabricSaveAdapter(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void savePearls(UUID playerId, List<EnderpearlRecord> pearls) {
        ServerWorld overworld = server.getOverworld();
        EnderpearlPersistentState.get(overworld).savePearls(playerId, pearls);
    }

    @Override
    public List<EnderpearlRecord> getPearls(UUID playerId) {
        ServerWorld overworld = server.getOverworld();
        return EnderpearlPersistentState.get(overworld).getPearls(playerId);
    }

    @Override
    public void clearPearls(UUID playerId) {
        ServerWorld overworld = server.getOverworld();
        EnderpearlPersistentState.get(overworld).clearPearls(playerId);
    }
}
