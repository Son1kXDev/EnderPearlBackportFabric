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
        Identifier id;
        try {
            id = new Identifier(dimensionId);
        } catch (Exception e) {
            return null;
        }
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
    }
}
