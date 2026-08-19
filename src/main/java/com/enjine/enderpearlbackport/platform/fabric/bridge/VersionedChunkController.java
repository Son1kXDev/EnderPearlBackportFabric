package com.enjine.enderpearlbackport.platform.fabric.bridge;

import net.minecraft.util.math.ChunkPos;

public interface VersionedChunkController {
    void addTicket(String dimensionId, ChunkPos pos);
    void removeTicket(String dimensionId, ChunkPos pos);
}
