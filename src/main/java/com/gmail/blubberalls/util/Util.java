package com.gmail.blubberalls.util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Random;

public class Util {
    private static Random random = new Random();

    public static Vector getRelativeMoveVector(LivingEntity entity) {
        Vector vector = entity.getEyeLocation().getDirection().normalize();

        vector.setY(0);
        int x = (int) entity.getSidewaysMovement();
        int z = (int) entity.getForwardsMovement();
        double angle = 0;

        if (x == -1) {
            if (z == -1) {
                angle = 225;
            }
            else if (z == 0) {
                angle = 270;
            }
            else if (z == 1) {
                angle = 315;
            }
        }
        else if (x == 0) {
            if (z == -1) {
                angle = 180;
            }
            else if (z == 0) {
                return new Vector(0, 0, 0);
            }
            else if (z == 1) {
                angle = 0;
            }
        }
        else if (x == 1) {
            if (z == -1) {
                angle = 135;
            }
            else if (z == 0) {
                angle = 90;
            }
            else if (z == 1) {
                angle = 45;
            }
        }

        return vector.rotateAroundY(Math.toRadians(angle));
    }

    public static double randomDouble(double min, double max) {
        return random.nextDouble() * (max - min) + min;
    }
}
