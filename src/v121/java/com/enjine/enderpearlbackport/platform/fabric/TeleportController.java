package com.enjine.enderpearlbackport.platform.fabric.bridge;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import com.enjine.enderpearlbackport.platform.fabric.bridge.VersionedTeleportController;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.UUID;

public class TeleportController implements VersionedTeleportController {

    private final MinecraftServer server;

    public TeleportController(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void teleport(UUID playerId, EnderpearlRecord r) {
        ServerPlayerEntity p = server.getPlayerManager().getPlayer(playerId);
        if (p == null) return;

        Identifier id = Identifier.tryParse(r.dimensionId());
        if (id == null) return;

        ServerWorld w = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
        if (w == null) return;

        p.teleportTo(new TeleportTarget(
                w,
                new Vec3d(r.x(), r.y(), r.z()),
                new Vec3d(r.vx(), r.vy(), r.vz()),
                p.getYaw(),
                p.getPitch(),
                TeleportTarget.NO_OP
        ));
    }
}
