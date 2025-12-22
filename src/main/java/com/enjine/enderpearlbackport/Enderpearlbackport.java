package com.enjine.enderpearlbackport;

import com.enjine.enderpearlbackport.common.api.Platform;
import com.enjine.enderpearlbackport.platform.fabric.FabricPearlMechanics;
import com.enjine.enderpearlbackport.platform.fabric.FabricSaveAdapter;
import com.enjine.enderpearlbackport.platform.fabric.bridge.FabricVersionBridge;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Enderpearlbackport implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Platform.init(new FabricSaveAdapter(server));
            FabricVersionBridge.init121(server);
        });

        ServerTickEvents.END_SERVER_TICK.register(FabricPearlMechanics::onEndServerTick);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                FabricPearlMechanics.saveAndRemovePlayerPearls(handler.getPlayer(), server)
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                FabricPearlMechanics.restorePlayerPearls(handler.getPlayer(), server)
        );

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            server.getPlayerManager().getPlayerList()
                    .forEach(p -> FabricPearlMechanics.saveAndRemovePlayerPearls(p, server));
        });
    }
}
