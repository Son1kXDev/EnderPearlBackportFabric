package com.enjine.enderpearlbackport.platform.fabric.bridge;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import com.enjine.enderpearlbackport.platform.fabric.bridge.VersionedTeleportController;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.UUID;

public class TeleportController implements VersionedTeleportController {

    private final MinecraftServer server;

    public TeleportController(MinecraftServer server) { this.server = server; }

    @Override
    public void teleport(UUID playerId, EnderpearlRecord r) {
        ServerPlayerEntity p = server.getPlayerManager().getPlayer(playerId);
        if (p == null) return;

        Identifier id = Identifier.tryParse(r.dimensionId());
        if (id == null) return;

        ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
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
