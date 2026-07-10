package com.gmail.blubberalls.MobPilot.nms;

import com.gmail.blubberalls.MobPilot.MobController;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.Set;
import java.util.function.Predicate;

public class InactiveGoalSelectorWrapper extends GoalSelector {
    private GoalSelector wrapped;
    private MobController<?> controller;

    public InactiveGoalSelectorWrapper(MobController<?> controller, GoalSelector wrapped) {
        this.wrapped = wrapped;
        this.controller = controller;

        for (WrappedGoal goal : getAvailableGoals()) {
            goal.stop();
        }
    }

    public GoalSelector getWrapped() {
        return wrapped;
    }

    @Override
    public void addGoal(final int prio, final Goal goal) {
        wrapped.addGoal(prio, goal);
    }

    @Override
    public void removeAllGoals(final Predicate<Goal> predicate) {
        wrapped.removeAllGoals(predicate);
    }

    @Override
    public boolean inactiveTick() {
        return wrapped.inactiveTick();
    }

    @Override
    public boolean hasTasks() {
        return wrapped.hasTasks();
    }

    @Override
    public void tick() {}

    @Override
    public void tickRunningGoals(final boolean forceTickAllRunningGoals) {}

    @Override
    public Set<WrappedGoal> getAvailableGoals() {
        return wrapped.getAvailableGoals();
    }

    @Override
    public void disableControlFlag(final Goal.Flag flag) {
        wrapped.disableControlFlag(flag);
    }

    @Override
    public void enableControlFlag(final Goal.Flag flag) {
        wrapped.enableControlFlag(flag);
    }

    @Override
    public void setControlFlag(final Goal.Flag flag, final boolean enabled) {
        wrapped.setControlFlag(flag, enabled);
    }
}
