package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import com.enjine.enderpearlbackport.platform.fabric.bridge.VersionedTeleportController;
import com.enjine.enderpearlbackport.platform.fabric.bridge.FabricVersionBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

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

        ServerWorld w = FabricVersionBridge.worldLookup.getWorld(server, r.dimensionId());
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
