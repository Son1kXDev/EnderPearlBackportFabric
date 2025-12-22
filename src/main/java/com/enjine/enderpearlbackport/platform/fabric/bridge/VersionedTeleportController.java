package com.enjine.enderpearlbackport.platform.fabric.bridge;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;

import java.util.UUID;

public interface VersionedTeleportController {
    void teleport(UUID playerId, EnderpearlRecord record);
}
