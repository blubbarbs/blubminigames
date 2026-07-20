package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import com.gmail.blubberalls.util.Util;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

public class FlyingMobController<T extends Mob> extends MobController<T> {
    public FlyingMobController(T entity, String... capabilities) {
        super(entity);
    }

    @Override
    public void onMoveControllerPreTick() {
        boolean isMoving = !isImmobile && canStrafe && (player.getForwardsMovement() != 0 || player.getSidewaysMovement() != 0);
         if (isMoving || player.getCurrentInput().isSneak() || player.getCurrentInput().isJump()) {
             Vector move = Util.getRelativeMoveVector(player);
             if (player.getCurrentInput().isSneak())
                 move.setY(-1);
             else if (player.getCurrentInput().isJump())
                 move.setY(1);

             move.normalize().multiply(30);
             double wantedX = entity.getX() + move.getX();
             double wantedY = entity.getY() + move.getY();
             double wantedZ = entity.getZ() + move.getZ();

             nmsMoveControl.setWantedPosition(0, 0, 0, 1.0);
         }
         else
             nmsMoveControl.setWait();
    }

    @Override
    public void onMoveControllerPostTick() {
        CraftMob craftMob = ((CraftMob) entity);

        craftMob.getHandle().setZza(player.getForwardsMovement());
//        craftMob.getHandle().setXxa(player.getSidewaysMovement());
    }
}
