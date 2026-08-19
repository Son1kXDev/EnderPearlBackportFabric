package com.enjine.enderpearlbackport.platform.fabric.bridge;

import com.enjine.enderpearlbackport.platform.fabric.ChunkController;
import com.enjine.enderpearlbackport.platform.fabric.TeleportController;
import com.enjine.enderpearlbackport.platform.fabric.WorldLookupImpl;
import net.minecraft.server.MinecraftServer;

public final class FabricVersionBridge {

    public static VersionedWorldLookup worldLookup;
    public static VersionedChunkController chunk;
    public static VersionedTeleportController teleport;
    public static VersionedPearlHooks hooks;

    public static void init(MinecraftServer server) {
        worldLookup = new WorldLookupImpl();
        chunk = new ChunkController(server);
        teleport = new TeleportController(server);
        hooks = new PearlHooks();
    }
}
