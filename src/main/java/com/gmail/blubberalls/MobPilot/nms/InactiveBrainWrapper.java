package com.gmail.blubberalls.MobPilot.nms;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class InactiveBrainWrapper<E extends LivingEntity> extends Brain<E> {
    private Brain<E> wrapped;

    public InactiveBrainWrapper(Brain<E> wrapped) {
        this.wrapped = wrapped;
        super();
    }

    public Brain<E> getWrapped() {
        return wrapped;
    }

    @Override
    public Brain.Packed pack() {
        return wrapped.pack();
    }

    @Override
    public boolean hasMemoryValue(final MemoryModuleType<?> type) {
        return wrapped.hasMemoryValue(type);
    }

    @Override
    public void clearMemories() {
        wrapped.clearMemories();
    }

    @Override
    public <U> void eraseMemory(final MemoryModuleType<U> type) {
        wrapped.eraseMemory(type);
    }

    @Override
    public <U> void setMemory(final MemoryModuleType<U> type, final @Nullable U value) {
        wrapped.setMemory(type, value);
    }

    @Override
    public <U> void setMemoryWithExpiry(final MemoryModuleType<U> type, final U value, final long timeToLive) {
        wrapped.setMemoryWithExpiry(type, value, timeToLive);
    }

    @Override
    public <U> void setMemory(final MemoryModuleType<U> type, final Optional<? extends U> optionalValue) {
        wrapped.setMemory(type, optionalValue.orElse(null));
    }

    @Override
    public <U> Optional<U> getMemory(final MemoryModuleType<U> type) {
        return wrapped.getMemory(type);
    }

    @Override
    public <U> @Nullable Optional<U> getMemoryInternal(final MemoryModuleType<U> type) {
        return wrapped.getMemoryInternal(type);
    }

    @Override
    public <U> long getTimeUntilExpiry(final MemoryModuleType<U> type) {
        return wrapped.getTimeUntilExpiry(type);
    }

    @Override
    public void forEach(final Brain.Visitor visitor) {
        wrapped.forEach(visitor);
    }

    @Override
    public <U> boolean isMemoryValue(final MemoryModuleType<U> memoryType, final U value) {
        return wrapped.isMemoryValue(memoryType, value);
    }

    @Override
    public boolean checkMemory(final MemoryModuleType<?> type, final MemoryStatus status) {
        return wrapped.checkMemory(type, status);
    }

    @Override
    public void setSchedule(final EnvironmentAttribute<Activity> schedule) {
        wrapped.setSchedule(schedule);
    }

    @Override
    public void setCoreActivities(final Set<Activity> activities) {
        wrapped.setCoreActivities(activities);
    }

    @Override
    public Set<Activity> getActiveActivities() {
        return wrapped.getActiveActivities();
    }

    @Override
    public List<BehaviorControl<? super E>> getRunningBehaviors() {
        return wrapped.getRunningBehaviors();
    }

    @Override
    public void useDefaultActivity() {
        wrapped.useDefaultActivity();
    }

    @Override
    public Optional<Activity> getActiveNonCoreActivity() {
        return wrapped.getActiveNonCoreActivity();
    }

    @Override
    public void setActiveActivityIfPossible(final Activity activity) {
        wrapped.setActiveActivityIfPossible(activity);
    }

    @Override
    public void updateActivityFromSchedule(final EnvironmentAttributeSystem environmentAttributes, final long gameTime, final Vec3 pos) {
        wrapped.updateActivityFromSchedule(environmentAttributes, gameTime, pos);
    }

    @Override
    public void setActiveActivityToFirstValid(final List<Activity> activities) {
        wrapped.setActiveActivityToFirstValid(activities);
    }

    @Override
    public void setDefaultActivity(final Activity activity) {
        wrapped.setDefaultActivity(activity);
    }

    @Override
    public void addActivity(
            final Activity activity,
            final ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> behaviorPriorityPairs,
            final Set<Pair<MemoryModuleType<?>, MemoryStatus>> conditions,
            final Set<MemoryModuleType<?>> memoriesToEraseWhenStopped
    ) {
        wrapped.addActivity(activity, behaviorPriorityPairs, conditions, memoriesToEraseWhenStopped);
    }

    @Override
    public void removeAllBehaviors() {
        wrapped.removeAllBehaviors();
    }

    @Override
    public boolean isActive(final Activity activity) {
        return wrapped.isActive(activity);
    }

    @Override
    public void tick(final ServerLevel level, final E body) { }

    @Override
    public void stopAll(final ServerLevel level, final E body) {
        wrapped.stopAll(level, body);
    }

    @Override
    public boolean isBrainDead() {
        return wrapped.isBrainDead();
    }
}
