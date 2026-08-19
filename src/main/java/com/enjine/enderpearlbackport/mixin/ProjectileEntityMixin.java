package com.enjine.enderpearlbackport.mixin;

import com.enjine.enderpearlbackport.api.ServerPlayerEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void epb$onSetOwner(Entity owner, CallbackInfo ci) {
        ProjectileEntity self = (ProjectileEntity) (Object) this;
        if (!(self instanceof EnderPearlEntity pearl)) return;

        if (owner instanceof ServerPlayerEntity player) {
            ((ServerPlayerEntityAccessor) player).epb$registerEnderPearl(pearl);
            com.enjine.enderpearlbackport.platform.fabric.FabricPearlMechanics.updatePearlTicket(pearl);
        }
    }
}
