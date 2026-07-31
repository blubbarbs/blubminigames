package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.MobPilot.nms.MoveControlOperation;
import org.bukkit.craftbukkit.entity.CraftMob;
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
        super(mob);
        setReach(10.0f);
        setCanJump(false);
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
            if (player.getCurrentInput().isJump()) {
                if (entity.isOnGround())
                    setJumpDelay(0);
                else
                    ((CraftMob) entity).getHandle().getJumpControl().jump();
            }

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
