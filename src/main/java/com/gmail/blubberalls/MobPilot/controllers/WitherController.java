package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.util.Util;
import org.bukkit.craftbukkit.entity.CraftWither;
import org.bukkit.entity.Wither;
import org.bukkit.util.Vector;

public class WitherController extends FlyingMobController<Wither> {
    public WitherController(Wither entity) {
        super(entity);
    }

    @Override
    public void onMoveControllerPreTick() {
        Vector move = Util.getRelativeMoveVector(player);

        if (player.getCurrentInput().isJump())
            move.add(new Vector(0, 1, 0));

        if (!move.isZero()) {
            float speed = ((CraftWither) entity).getHandle().getSpeed();

            move.normalize().multiply(speed);
            entity.setVelocity(entity.getVelocity().setX(move.getX()).setZ(move.getZ()).setY(move.getY()));
            nmsMoveControl.setWantedPosition(entity.getX() + move.getX(), entity.getY() + move.getY(), entity.getZ() + move.getZ(), 1.0);
        }
        else
            nmsMoveControl.setWait();
    }

    @Override
    public void onMoveControllerPostTick() {

    }
}
