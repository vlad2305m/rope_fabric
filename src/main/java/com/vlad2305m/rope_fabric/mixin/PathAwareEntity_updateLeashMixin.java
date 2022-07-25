package com.vlad2305m.rope_fabric.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PathAwareEntity.class)
public class PathAwareEntity_updateLeashMixin extends MobEntity {         protected PathAwareEntity_updateLeashMixin(EntityType<? extends MobEntity> entityType, World world) {super(entityType, world);}

	private  float length = 6f;
	@Inject(at = @At("HEAD"), method = "updateLeash()V")
	private void reel(CallbackInfo info) {
		if(getHoldingEntity() instanceof PlayerEntity player && player.isSneaking()){
			float f = distanceTo(player);
			if (length > 1f && f-length<1 ) length -= 0.05;
			if (f < 1f) return;
			Vec3d d = player.getPos().subtract(getPos()).multiply(1./f);
			dv = d.multiply((f-length)*0.2);
			if (f > length)setVelocity(getVelocity().multiply(0.9).add(dv));
		} else length = Math.max(Math.min(6f, getHoldingEntity() instanceof PlayerEntity player ? distanceTo(player) : length), length);
		if(getHoldingEntity() instanceof PlayerEntity player) player.sendMessage(Text.literal(String.valueOf(length)),true);
	}

	private Vec3d dv = new Vec3d(0,0,0);
	@Inject(method = "updateLeash()V", at = @At(value = "INVOKE", target = "net/minecraft/entity/mob/PathAwareEntity.setVelocity(Lnet/minecraft/util/math/Vec3d;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
	private void stealVelocity(CallbackInfo ci, Entity entity, float f, double d, double e, double g) {
		dv = new Vec3d(d, e, g).multiply((f-length)*0.2);
	}

	@ModifyArg(method = "updateLeash()V", at = @At(value = "INVOKE", target = "net/minecraft/entity/mob/PathAwareEntity.setVelocity(Lnet/minecraft/util/math/Vec3d;)V"), index = 0)
	private Vec3d settVelocity(Vec3d par1) {
		if(getHoldingEntity() instanceof PlayerEntity player) {player.setVelocity(player.getVelocity().multiply(0.9).add(dv.multiply(-0.5))); player.velocityModified = true;}
		return getVelocity().add(dv); }

}
