package com.enjine.enderpearlbackport.platform.fabric.bridge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public interface VersionedWorldLookup {
    ServerWorld getWorld(MinecraftServer server, String dimensionId);
}
