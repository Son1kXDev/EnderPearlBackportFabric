package com.enjine.enderpearlbackport.platform.fabric.bridge;

import net.minecraft.server.MinecraftServer;

public final class FabricVersionBridge {

    public static VersionedChunkController chunk;
    public static VersionedTeleportController teleport;
    public static VersionedPearlHooks hooks;

    private FabricVersionBridge() {}

    public static void init121(MinecraftServer server) {
        chunk = new com.enjine.enderpearlbackport.platform.fabric.v121.ChunkController121(server);
        teleport = new com.enjine.enderpearlbackport.platform.fabric.v121.TeleportController121(server);
        hooks = new com.enjine.enderpearlbackport.platform.fabric.v121.PearlHooks121();
    }
}
