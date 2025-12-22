package com.enjine.enderpearlbackport.platform.fabric.bridge;

import net.minecraft.server.MinecraftServer;
import com.enjine.enderpearlbackport.platform.fabric.bridge.*;

public final class FabricVersionBridge {

    public static VersionedChunkController chunk;
    public static VersionedTeleportController teleport;
    public static VersionedPearlHooks hooks;

    public static void init(MinecraftServer server) {
        chunk = new ChunkController(server);
        teleport = new TeleportController(server);
        hooks = new PearlHooks();
    }
}

