package com.kahzerx.kahzerxmod.mixin.lobotomizeVillagersExtension;

import com.kahzerx.kahzerxmod.extensions.lobotomizeVillagersExtension.LobotomizeVillagersExtension;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(Villager.class)
public abstract class VillagerLobotomyMixin extends AbstractVillager {
    @Unique private static final int CHECK_INTERVAL = 100;
    @Unique private static final int PLAYER_INTERVAL = 20;
    @Unique private static final double PLAYER_RADIUS = 8.0;
    @Unique private static final int WORK_CHECK_COOLDOWN = 300;
    @Unique private static final long SLEEP_MEMORY_TICKS = 24000L;

    @Unique private boolean kahzerx$parked = false;
    @Unique private boolean kahzerx$playerNear = false;
    @Unique private long kahzerx$lastWorkCheck = 0L;

    public VillagerLobotomyMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @WrapWithCondition(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/Brain;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private boolean kahzerx$tickBrain(Brain<?> brain, ServerLevel level, LivingEntity entity) {
        if (!LobotomizeVillagersExtension.isExtensionEnabled) {
            return true;
        }
        Villager self = (Villager) (Object) this;
        long phase = level.getGameTime() + self.getId();
        if (phase % CHECK_INTERVAL == 0) {
            this.kahzerx$parked = this.kahzerx$isParked(self, level);
        }
        if (!this.kahzerx$parked) {
            return true;
        }
        if (phase % PLAYER_INTERVAL == 0) {
            this.kahzerx$playerNear = !level.getEntitiesOfClass(Player.class, self.getBoundingBox().inflate(PLAYER_RADIUS), EntitySelector.NO_SPECTATORS).isEmpty();
        }
        if (this.kahzerx$playerNear) {
            return true;
        }
        this.kahzerx$workAtJobSite(self, level);
        return false;
    }

    @Unique
    private boolean kahzerx$isParked(Villager self, ServerLevel level) {
        if (self.isBaby() || self.getVillagerXp() <= 0 || self.isSleeping()) {
            return false;
        }
        Brain<Villager> brain = self.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.HOME)) {
            return false;
        }
        Optional<Long> lastSlept = brain.getMemory(MemoryModuleType.LAST_SLEPT);
        if (lastSlept.isPresent() && level.getGameTime() - lastSlept.get() < SLEEP_MEMORY_TICKS) {
            return false;
        }
        if (self.isPassenger()) {
            return true;
        }
        BlockPos pos = self.blockPosition();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (kahzerx$canWalkInto(level, pos.relative(dir))) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private static boolean kahzerx$canWalkInto(ServerLevel level, BlockPos feet) {
        if (!level.isLoaded(feet) || !level.isLoaded(feet.above())) {
            return true;
        }
        return kahzerx$passable(level.getBlockState(feet)) && kahzerx$passable(level.getBlockState(feet.above()));
    }

    @Unique
    private static boolean kahzerx$passable(BlockState state) {
        return state.isPathfindable(PathComputationType.LAND) || DoorBlock.isWoodenDoor(state);
    }

        @Unique
    private void kahzerx$workAtJobSite(Villager self, ServerLevel level) {
        long now = level.getGameTime();
        if (now - this.kahzerx$lastWorkCheck < WORK_CHECK_COOLDOWN || level.getRandom().nextInt(2) != 0) {
            return;
        }
        this.kahzerx$lastWorkCheck = now;
        Brain<Villager> brain = self.getBrain();
        brain.updateActivityFromSchedule(level.environmentAttributes(), now, self.position());
        if (!brain.isActive(Activity.WORK)) {
            return;
        }
        Optional<GlobalPos> jobSite = brain.getMemory(MemoryModuleType.JOB_SITE);
        if (jobSite.isEmpty() || jobSite.get().dimension() != level.dimension() || !jobSite.get().pos().closerToCenterThan(self.position(), 1.73)) {
            return;
        }
        brain.setMemory(MemoryModuleType.LAST_WORKED_AT_POI, now);
        self.playWorkSound();
        if (self.shouldRestock(level)) {
            self.restock();
        }
    }
}
