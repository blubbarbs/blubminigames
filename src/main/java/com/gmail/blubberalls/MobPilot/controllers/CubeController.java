package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.MobPilot.nms.MoveControlOperation;
import org.bukkit.entity.AbstractCubeMob;

import java.lang.reflect.Field;


public class CubeController extends MobController<AbstractCubeMob> {
    private static final Class<?> cubeMovementControllerClass;
    private static final Field cubeMovementControllerYRot;
    private static final Field jumpDelayField;

    static {
        try {
            cubeMovementControllerClass = Class.forName(net.minecraft.world.entity.monster.cubemob.AbstractCubeMob.class.getName() + "$CubeMobMoveControl");
            cubeMovementControllerYRot = cubeMovementControllerClass.getDeclaredField("yRot");
            cubeMovementControllerYRot.setAccessible(true);
            jumpDelayField = cubeMovementControllerClass.getDeclaredField("jumpDelay");
            jumpDelayField.setAccessible(true);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CubeController(AbstractCubeMob mob) {
        super(mob, 0.0, 10.0);
    }

    private void setJumpDelay(int jumpDelay) {
        try {
            jumpDelayField.set(nmsMoveControl.getWrapped(), jumpDelay);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMoveControllerPreTick() {
        try {
            if (entity.isOnGround() && player.getCurrentInput().isJump())
                setJumpDelay(0);

            cubeMovementControllerYRot.setFloat(nmsMoveControl.getWrapped(), player.getYaw());
            nmsMoveControl.setOperation(MoveControlOperation.MOVE_TO);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMoveControllerPostTick() {
        setJumpDelay(2);
    }
}
