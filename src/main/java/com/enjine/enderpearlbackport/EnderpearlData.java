package com.enjine.enderpearlbackport;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class EnderpearlData {
    public final UUID pearlId;
    public final UUID playerId;
    public final RegistryKey<World> worldKey;
    public final Vec3d position;
    public final Vec3d velocity;

    public EnderpearlData(
            UUID pearlId,
            UUID playerId,
            RegistryKey<World> worldKey,
            Vec3d position,
            Vec3d velocity
    ) {
        this.pearlId = pearlId;
        this.playerId = playerId;
        this.worldKey = worldKey;
        this.position = position;
        this.velocity = velocity;
    }
}
