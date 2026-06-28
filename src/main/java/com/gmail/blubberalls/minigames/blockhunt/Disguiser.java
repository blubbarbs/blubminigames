package com.gmail.blubberalls.minigames.blockhunt;

import org.bukkit.Input;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class Disguiser implements Listener {

    public Vector getRelativeMoveVector(Player player, Input input) {
        int x = 0;
        int z = 0;

        x -= input.isRight() ? 1 : 0;
        x += input.isLeft() ? 1 : 0;
        z += input.isForward() ? 1 : 0;
        z -= input.isBackward() ? 1 : 0;

        if (x == 0 && z == 0)
            return null;

        double angle;

        if (x == -1) {
            if (z == -1)
                angle = 225;
            else if (z == 0)
                angle = 270;
            else
                angle = 315;
        }
        else if (x == 0) {
            if (z == -1)
                angle = 180;
            else
                angle = 0;
        }
        else {
            if (z == -1)
                angle = 135;
            else if (z == 0)
                angle = 90;
            else
                angle = 45;
        }

        angle = Math.toRadians(angle);
        return player.getEyeLocation().getDirection().setY(0).normalize().rotateAroundY(angle);
    }

//    public void disguiseBlock(Player player, BlockData blockData) {
//        Disguise disguise = getDisguise(player);
//
//        if (disguise != null)
//            undisguisePlayer(player);
//
//        BlockDisplay blockDisplay = player.getWorld().spawn(player.getLocation(), BlockDisplay.class);
//        blockDisplay.setRotation(0, 0);
//        Vector3f translation = new Vector3f(-.5f, -1.8f, -.5f);
//        Vector3f scale = new Vector3f(1, 1, 1);
//        AxisAngle4f rightRotation = new AxisAngle4f(0, 0, 0, 1);
//        AxisAngle4f leftRotation = new AxisAngle4f(0, 0, 0, 1);
//        Transformation transformation = new Transformation(translation, leftRotation, scale, rightRotation);
//
//        blockDisplay.setBlock(blockData);
//        blockDisplay.setTransformation(transformation);
//        PDC.set(blockDisplay, Keys.IS_DISGUISE, true);
//
//        player.addPotionEffect(INVISIBILITY_EFFECT);
//        player.addPassenger(blockDisplay);
//        PDC.set(player, Keys.DISGUISE, new Disguise(blockDisplay));
//    }

}
