package com.enjine.enderpearlbackport.mixin;

import com.enjine.enderpearlbackport.api.ServerPlayerEntityAccessor;
import com.enjine.enderpearlbackport.platform.fabric.bridge.FabricVersionBridge;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerEntityAccessor {

    @Unique
    private final Set<UUID> epb$enderPearls = new HashSet<>();

    @Unique
    private final List<NbtCompound> epb$pendingPearlNbt = new ArrayList<>();

    @Override
    public Set<UUID> epb$getEnderPearls() {
        return epb$enderPearls;
    }

    @Override
    public void epb$registerEnderPearl(EnderPearlEntity pearl) {
        epb$enderPearls.add(pearl.getUuid());
    }

    @Override
    public void epb$deregisterEnderPearl(EnderPearlEntity pearl) {
        epb$enderPearls.remove(pearl.getUuid());
    }

    @Override
    public void epb$addPendingPearlNbt(NbtCompound nbt) {
        epb$pendingPearlNbt.add(nbt);
    }

    @Override
    public List<NbtCompound> epb$getPendingPearlNbt() {
        return epb$pendingPearlNbt;
    }

    @Inject(method = "playerTick", at = @At("TAIL"))
    private void epb$onPlayerTick(CallbackInfo ci) {
        epb$spawnPendingPearls();
    }

    @Override
    public void epb$spawnPendingPearls() {
        if (epb$pendingPearlNbt.isEmpty()) return;

        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        if (self.getServer() == null) return;

        Iterator<NbtCompound> it = epb$pendingPearlNbt.iterator();
        while (it.hasNext()) {
            NbtCompound pearlNbt = it.next();
            String dim = pearlNbt.getString("epb$dim");

            ServerWorld world = FabricVersionBridge.worldLookup.getWorld(self.getServer(), dim);
            if (world == null) {
                it.remove();
                continue;
            }

            UUID pearlId = pearlNbt.getUuid("UUID");

            Entity existing = world.getEntity(pearlId);
            if (existing instanceof EnderPearlEntity pearl && !pearl.isRemoved()) {
                epb$enderPearls.add(pearlId);
                ChunkPos chunkPos = new ChunkPos(pearl.getBlockPos());
                FabricVersionBridge.chunk.addTicket(dim, chunkPos);
                it.remove();
                continue;
            }

            EnderPearlEntity pearl = EntityType.ENDER_PEARL.create(world);
            if (pearl == null) {
                it.remove();
                continue;
            }

            pearl.readNbt(pearlNbt);

            BlockPos pos = BlockPos.ofFloored(pearl.getX(), pearl.getY(), pearl.getZ());
            ChunkPos chunkPos = new ChunkPos(pos);
            world.getChunk(chunkPos.x, chunkPos.z);

            world.spawnEntity(pearl);
            epb$enderPearls.add(pearl.getUuid());
            FabricVersionBridge.chunk.addTicket(dim, chunkPos);

            it.remove();
        }
    }
}
