package com.enjine.enderpearlbackport.mixin;

import com.enjine.enderpearlbackport.api.ServerPlayerEntityAccessor;
import com.enjine.enderpearlbackport.platform.fabric.FabricPearlMechanics;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "discard", at = @At("TAIL"))
    private void epb$onDiscard(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof EnderPearlEntity pearl)) return;

        FabricPearlMechanics.removePearlTicket(pearl);

        UUID ownerUuid = ((ProjectileEntityAccessor) pearl).getOwnerUuid();
        if (ownerUuid == null) return;

        MinecraftServer server = pearl.getWorld().getServer();
        if (server == null) return;

        ServerPlayerEntity player = server.getPlayerManager().getPlayer(ownerUuid);
        if (player != null) {
            ((ServerPlayerEntityAccessor) player).epb$deregisterEnderPearl(pearl);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void epb$onTick(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;

        if (self instanceof EnderPearlEntity pearl && !pearl.isRemoved()
                && pearl.getWorld() instanceof ServerWorld) {
            FabricPearlMechanics.updatePearlTicket(pearl);
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void epb$saveEnderPearls(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof ServerPlayerEntityAccessor accessor)) return;
        if (!(self instanceof ServerPlayerEntity player)) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        NbtList list = new NbtList();

        List<NbtCompound> pending = accessor.epb$getPendingPearlNbt();
        if (!pending.isEmpty()) {
            for (NbtCompound pearlNbt : pending) {
                list.add(pearlNbt.copy());
            }
        } else {
            Set<UUID> pearlIds = accessor.epb$getEnderPearls();
            if (pearlIds.isEmpty()) return;

            for (UUID pearlId : new ArrayList<>(pearlIds)) {
                EnderPearlEntity pearl = epb$findPearl(server, pearlId);
                if (pearl == null || pearl.isRemoved()) {
                    pearlIds.remove(pearlId);
                    continue;
                }

                NbtCompound pearlNbt = new NbtCompound();
                pearl.writeNbt(pearlNbt);
                pearlNbt.putString("epb$dim", pearl.getWorld().getRegistryKey().getValue().toString());
                list.add(pearlNbt);
            }
        }

        if (!list.isEmpty()) {
            nbt.put("epb$ender_pearls", list);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void epb$loadEnderPearls(NbtCompound nbt, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof ServerPlayerEntityAccessor accessor)) return;

        if (!nbt.contains("epb$ender_pearls", NbtElement.LIST_TYPE)) return;

        NbtList list = nbt.getList("epb$ender_pearls", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            accessor.epb$addPendingPearlNbt(list.getCompound(i));
        }
    }

    @Unique
    private static EnderPearlEntity epb$findPearl(MinecraftServer server, UUID pearlId) {
        for (ServerWorld world : server.getWorlds()) {
            Entity entity = world.getEntity(pearlId);
            if (entity instanceof EnderPearlEntity pearl && !pearl.isRemoved()) {
                return pearl;
            }
        }
        return null;
    }
}
