package dev.aether.optimizer.mixin;

import dev.aether.optimizer.AetherOptimizer;
import dev.aether.optimizer.OptimizerConfig;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Experimental server-side AI cadence reduction for low-risk, distant mobs. */
@Mixin(Mob.class)
abstract class MobAiThrottleMixin {
    @Unique private int aether$nextPlayerCheck = Integer.MIN_VALUE;
    @Unique private boolean aether$farFromPlayer;
    @Unique private int aether$phase = -1;
    @Unique private int aether$consecutiveSkips;

    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true, require = 1)
    private void aether$throttleDistantMob(CallbackInfo ci) {
        final OptimizerConfig cfg = AetherOptimizer.config();
        if (!cfg.enabled()) return;
        final Mob mob = (Mob) (Object) this;
        final Level level = mob.level();
        if (level.isClientSide() || !mob.isAlive()) return;
        if (mob.tickCount < cfg.minimumAgeTicks()) {
            aether$consecutiveSkips = 0;
            return;
        }
        if (mob.getTarget() != null || mob.hasCustomName() || mob.isLeashed()
                || mob.isPassenger() || mob.isVehicle() || mob.isOnFire()
                || mob.hurtTime > 0 || mob.isInWater()
                || (mob instanceof AgeableMob ageable && ageable.isBaby()) || mob.isNoAi()) {
            aether$consecutiveSkips = 0;
            return;
        }
        final int tick = mob.tickCount;
        if (aether$nextPlayerCheck == Integer.MIN_VALUE || tick >= aether$nextPlayerCheck) {
            Player nearest = level.getNearestPlayer(mob, (double) cfg.distance());
            aether$farFromPlayer = nearest == null;
            aether$nextPlayerCheck = tick + cfg.playerCheckTicks();
        }
        if (!aether$farFromPlayer) {
            aether$consecutiveSkips = 0;
            return;
        }
        if (aether$consecutiveSkips >= cfg.maxConsecutiveSkips()) {
            aether$consecutiveSkips = 0;
            return;
        }
        if (aether$phase < 0) aether$phase = Math.floorMod(mob.getId(), cfg.interval());
        if (Math.floorMod(tick + aether$phase, cfg.interval()) != 0) {
            aether$consecutiveSkips++;
            ci.cancel();
        } else {
            aether$consecutiveSkips = 0;
        }
    }
}
