package com.enjine.enderpearlbackport.platform.fabric.bridge;

import net.minecraft.util.math.ChunkPos;

public interface VersionedChunkController {
    void force(String dimensionId, ChunkPos pos);
    void release(String dimensionId, ChunkPos pos);
}
