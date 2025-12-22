package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.api.TeleportAdapter;
import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.Set;
import java.util.UUID;

public class FabricTeleportAdapter implements TeleportAdapter {

    private final MinecraftServer server;

    public FabricTeleportAdapter(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void teleport(UUID playerId, EnderpearlRecord record) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
        if (player == null) return;

        Identifier identifier = Identifier.tryParse(record.dimensionId());
        if (identifier == null) return;

        RegistryKey<World> worldKey =
                RegistryKey.of(RegistryKeys.WORLD, identifier);

        ServerWorld world = server.getWorld(worldKey);
        if (world == null) return;

        TeleportTarget target = new TeleportTarget(
                world,
                new Vec3d(record.x(), record.y(), record.z()),
                new Vec3d(record.vx(), record.vy(), record.vz()),
                player.getYaw(),
                player.getPitch(),
                TeleportTarget.NO_OP
        );

        player.teleportTo(target);
    }
}
