package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import net.minecraft.world.phys.Vec3;
import org.bukkit.EntityEffect;
import org.bukkit.craftbukkit.entity.CraftSquid;
import org.bukkit.entity.Squid;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

public class SquidController extends MobController<Squid> {
    static Field movementVectorField;
    static Field tentacleSpeedField;

    static {
        try {
            movementVectorField = net.minecraft.world.entity.animal.squid.Squid.class.getDeclaredField("movementVector");
            movementVectorField.setAccessible(true);
            tentacleSpeedField = net.minecraft.world.entity.animal.squid.Squid.class.getDeclaredField("tentacleSpeed");
            tentacleSpeedField.setAccessible(true);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean allowMovement = false;

    public SquidController(Squid entity) {
        super(entity);
        setCanStrafe(false);
        setCanJump(false);
        setSyncRotation(false);
    }

    protected void setSquidAnimationPhase(float phase) {
        CraftSquid craftSquid = (CraftSquid) entity;
        craftSquid.getHandle().tentacleMovement = (float) (phase * 2 * Math.PI);

        if (phase == 0)
            entity.playEffect(EntityEffect.SQUID_ROTATE);
    }

    protected void setSquidTentacleSpeed(float speed) {
        CraftSquid craftSquid = (CraftSquid) entity;

        try {
            tentacleSpeedField.setFloat(craftSquid.getHandle(), speed);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected int getTicksUntilBurst() {
        CraftSquid craftSquid = (CraftSquid) entity;
        float speed;

        try {
            speed = tentacleSpeedField.getFloat(craftSquid.getHandle());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        float currentMovementPhase = craftSquid.getHandle().tentacleMovement;
        float phaseUntilBurst = (3f * (float) Math.PI / 4f) - currentMovementPhase;

        if (phaseUntilBurst <= 0)
            return 0;

        return (int) Math.ceil(phaseUntilBurst / speed);
    }

    protected void setMovementVector(Vector vector) {
        Vec3 nmsVector = new Vec3(vector.getX(), vector.getY(), vector.getZ());
        CraftSquid craftSquid = (CraftSquid) entity;

        try {
            movementVectorField.set(craftSquid.getHandle(), nmsVector);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onStartJump() {
        if (allowMovement)
            return;

        Vector direction = player.getEyeLocation().getDirection();

        setSquidAnimationPhase(0);
        long ticksUntilBurst = getTicksUntilBurst();
        entity.setVelocity(direction.clone().multiply(.05));
        setMovementVector(direction);
        allowMovement = true;

        registerRunnable(() -> {
            allowMovement = false;
        }, ticksUntilBurst);
    }

    @Override
    protected void tick() {
        super.tick();

        if (!allowMovement) {
            setSquidAnimationPhase(0f);
            entity.setVelocity(entity.getVelocity().multiply(.9f));
        }
    }
}
