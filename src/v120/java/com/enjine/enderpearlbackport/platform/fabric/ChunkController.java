package com.enjine.enderpearlbackport.platform.fabric.bridge;

import com.enjine.enderpearlbackport.platform.fabric.bridge.VersionedChunkController;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class ChunkController implements VersionedChunkController {

    private static final int ENTITY_TICKING_LEVEL = 31;

    public static final ChunkTicketType<ChunkPos> ENDER_PEARL_TICKET =
            ChunkTicketType.create("ender_pearl", 0L);

    private final MinecraftServer server;

    public ChunkController(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void addTicket(String dim, ChunkPos pos) {
        ServerWorld w = world(dim);
        if (w != null) {
            ServerChunkManager chunkManager = w.getChunkManager();
            chunkManager.addTicket(ENDER_PEARL_TICKET, pos, ENTITY_TICKING_LEVEL, pos);
        }
    }

    @Override
    public void removeTicket(String dim, ChunkPos pos) {
        ServerWorld w = world(dim);
        if (w != null) {
            ServerChunkManager chunkManager = w.getChunkManager();
            chunkManager.removeTicket(ENDER_PEARL_TICKET, pos, ENTITY_TICKING_LEVEL, pos);
        }
    }

    private ServerWorld world(String dim) {
        Identifier id = Identifier.tryParse(dim);
        if (id == null) return null;
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
    }
}
