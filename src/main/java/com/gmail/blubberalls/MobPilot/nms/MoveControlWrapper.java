package com.gmail.blubberalls.MobPilot.nms;

import com.gmail.blubberalls.MobPilot.controllers.MobController;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.bukkit.craftbukkit.entity.CraftMob;

import java.lang.reflect.Field;

public class MoveControlWrapper extends MoveControl<Mob> {
    private static Field strafeForwardsField;
    private static Field strafeRightField;
    private static Field operationField;

    static {
        try {
            strafeForwardsField = MoveControl.class.getDeclaredField("strafeForwards");
            strafeRightField = MoveControl.class.getDeclaredField("strafeRight");
            operationField = MoveControl.class.getDeclaredField("operation");

            strafeForwardsField.setAccessible(true);
            strafeRightField.setAccessible(true);
            operationField.setAccessible(true);
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private MoveControl<?> wrapped;
    private MobController<?> controller;
    private net.minecraft.world.entity.Mob mob;

    public MoveControlWrapper(MobController<?> controller) {
        super(((CraftMob) controller.getEntity()).getHandle());
        this.mob = ((CraftMob) controller.getEntity()).getHandle();
        this.wrapped = mob.getMoveControl();
        this.controller = controller;
    }

    public MoveControl<?> getWrapped() {
        return wrapped;
    }

    public float getStrafeForwards() {
        try {
            return strafeForwardsField.getFloat(wrapped);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public float getStrafeRight() {
        try {
            return strafeRightField.getFloat(wrapped);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public MoveControlOperation getOperation() {
        MoveControl.Operation nmsOperation;

        try {
            nmsOperation = (Operation) operationField.get(wrapped);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return switch (nmsOperation) {
            case STRAFE -> MoveControlOperation.STRAFE;
            case JUMPING -> MoveControlOperation.JUMP;
            case MOVE_TO -> MoveControlOperation.MOVE_TO;
            default -> MoveControlOperation.WAIT;
        };
    }

    @Override
    public boolean hasWanted() {
        return wrapped.hasWanted();
    }

    @Override
    public double getSpeedModifier() {
        return wrapped.getSpeedModifier();
    }

    @Override
    public double getWantedX() {
        return wrapped.getWantedX();
    }

    @Override
    public double getWantedY() {
        return wrapped.getWantedY();
    }

    @Override
    public double getWantedZ() {
        return wrapped.getWantedZ();
    }

    @Override
    public void setWantedPosition(final double x, final double y, final double z, final double speedModifier) {
        wrapped.setWantedPosition(x, y, z, speedModifier);
    }

    @Override
    public void strafe(final float forwards, final float right) {
        wrapped.strafe(forwards, right);
    }

    @Override
    public void setWait() {
        wrapped.setWait();
    }

    @Override
    public void tick() {
        controller.onMoveControllerPreTick();
        wrapped.tick();
        controller.onMoveControllerPostTick();
    }
}
