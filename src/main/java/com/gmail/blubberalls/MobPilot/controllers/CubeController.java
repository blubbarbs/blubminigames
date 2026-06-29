package com.gmail.blubberalls.MobPilot.controllers;

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
    }

    @Override
    public void onMoveControllerPreTick() {
        try {
            cubeMovementControllerYRot.setFloat(nmsMoveControl.getWrapped(), player.getYaw());
            nmsMoveControl.setOperation(MoveControlOperation.MOVE_TO);

            if (player.getCurrentInput().isJump()) {
                jumpDelayField.set(nmsMoveControl.getWrapped(), 0);
            }
            else {
                jumpDelayField.set(nmsMoveControl.getWrapped(), 2);
            }

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMoveControllerPostTick() {
        CraftMob mob = (CraftMob) entity;

        mob.getHandle().setZza(1.0f);
    }
}
