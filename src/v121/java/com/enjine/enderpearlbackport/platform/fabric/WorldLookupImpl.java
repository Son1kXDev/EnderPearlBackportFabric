package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.platform.fabric.bridge.VersionedWorldLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class WorldLookupImpl implements VersionedWorldLookup {

    @Override
    public ServerWorld getWorld(MinecraftServer server, String dimensionId) {
        Identifier id = Identifier.tryParse(dimensionId);
        if (id == null) return null;
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
    }
}
