package com.enjine.enderpearlbackport;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Enderpearlbackport implements ModInitializer {

    public static final String MOD_ID = "enderpearlbackport";

    @Override
    public void onInitialize() {

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            onPlayerDisconnect(player, server);
        });


        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            onPlayerJoin(player, server);
        });
    }



    private void onServerTick(MinecraftServer server) {
        EnderpearlChunkManager.beginTick();

        for (ServerWorld world : server.getWorlds()) {
            List<? extends EnderPearlEntity> pearls = world.getEntitiesByType(
                    EntityType.ENDER_PEARL,
                    pearl -> true
            );


            for (EnderPearlEntity pearl : pearls) {
                EnderpearlChunkManager.trackPearl(world, pearl);
            }
        }

        EnderpearlChunkManager.endTick(server);
    }


    private void onPlayerDisconnect(ServerPlayerEntity player, MinecraftServer server) {
        UUID playerId = player.getUuid();

        List<EnderpearlData> pearlsToSave = new ArrayList<>();

        for (ServerWorld world : server.getWorlds()) {
            List<? extends EnderPearlEntity> pearls =
                    world.getEntitiesByType(EntityType.ENDER_PEARL, pearl -> true);

            for (EnderPearlEntity pearl : pearls) {

                if (pearl.getOwner() instanceof ServerPlayerEntity owner
                        && owner.getUuid().equals(playerId)
                        && !pearl.isRemoved()) {

                    EnderpearlData data = new EnderpearlData(
                            pearl.getUuid(),
                            playerId,
                            world.getRegistryKey(),
                            pearl.getPos(),
                            pearl.getVelocity()
                    );

                    pearlsToSave.add(data);
                    pearl.discard();
                }
            }
        }

        if (!pearlsToSave.isEmpty()) {
            EnderpearlSaveManager.savePearlsForPlayer(server, playerId, pearlsToSave);
        }
    }


    private void onPlayerJoin(ServerPlayerEntity player, MinecraftServer server) {
        UUID playerId = player.getUuid();

        List<EnderpearlData> pearls =
                EnderpearlSaveManager.popPearlsForPlayer(server, playerId);

        if (pearls.isEmpty()) return;

        for (EnderpearlData data : pearls) {
            ServerWorld world = server.getWorld(data.worldKey);
            if (world == null) continue;

            EnderPearlEntity pearl = new EnderPearlEntity(world, player);
            pearl.refreshPositionAndAngles(
                    data.position.x, data.position.y, data.position.z,
                    player.getYaw(), player.getPitch()
            );
            pearl.setVelocity(data.velocity);

            world.spawnEntity(pearl);
        }
    }
}
