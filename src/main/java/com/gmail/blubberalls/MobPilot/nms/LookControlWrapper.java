package com.gmail.blubberalls.MobPilot.nms;

import com.gmail.blubberalls.MobPilot.MobController;
import net.minecraft.world.entity.ai.control.LookControl;
import org.bukkit.craftbukkit.entity.CraftMob;

public class LookControlWrapper extends LookControl {
    private LookControl wrapped;

    public LookControlWrapper(MobController<?> controller) {
        CraftMob mob = (CraftMob) controller.getEntity();
        super(mob.getHandle());
        wrapped = mob.getHandle().getLookControl();
    }

    public LookControl getWrapped() {
        return wrapped;
    }

    @Override
    public void setLookAt(final double x, final double y, final double z, final float yMaxRotSpeed, final float xMaxRotAngle) {
        wrapped.setLookAt(x, y, z, yMaxRotSpeed, xMaxRotAngle);
    }

    @Override
    public void tick() {}

    @Override
    public boolean isLookingAtTarget() {
        return wrapped.isLookingAtTarget();
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
}
