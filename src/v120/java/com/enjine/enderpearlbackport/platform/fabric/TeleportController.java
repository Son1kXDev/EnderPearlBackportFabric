package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import com.enjine.enderpearlbackport.platform.fabric.bridge.VersionedTeleportController;
import com.enjine.enderpearlbackport.platform.fabric.bridge.FabricVersionBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public class TeleportController implements VersionedTeleportController {

    private final MinecraftServer server;

    public TeleportController(MinecraftServer server) { this.server = server; }

    @Override
    public void teleport(UUID playerId, EnderpearlRecord r) {
        ServerPlayerEntity p = server.getPlayerManager().getPlayer(playerId);
        if (p == null) return;

        ServerWorld world = FabricVersionBridge.worldLookup.getWorld(server, r.dimensionId());
        if (world == null) return;

        p.teleport(
                world,
                r.x(),
                r.y(),
                r.z(),
                p.getYaw(),
                p.getPitch()
        );
    }
}
