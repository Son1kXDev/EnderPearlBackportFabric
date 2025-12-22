package com.enjine.enderpearlbackport.mixin;

import com.enjine.enderpearlbackport.platform.fabric.FabricPearlMechanics;
import com.enjine.enderpearlbackport.platform.fabric.bridge.FabricVersionBridge;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public class EnderPearlEntityMixin {

    @Inject(method = "onCollision", at = @At("HEAD"))
    private void epb$onCollision(HitResult hitResult, CallbackInfo ci) {
        EnderPearlEntity pearl = (EnderPearlEntity) (Object) this;
        if (!(pearl.getOwner() instanceof ServerPlayerEntity player)) return;

        FabricPearlMechanics.ensureCrossDimensionTeleport(player, pearl);
        FabricVersionBridge.hooks.onPearlCollision(player, pearl);

    }
}
