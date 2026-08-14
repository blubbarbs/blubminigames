package com.gmail.blubberalls.MobPilot.nms;

import com.gmail.blubberalls.MobPilot.MobController;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.bukkit.craftbukkit.entity.CraftMob;

public class MoveControlWrapper extends MoveControl<Mob> {
    private MoveControl<?> wrapped;

    public MoveControlWrapper(MobController<?> controller) {
        super(((CraftMob) controller.getEntity()).getHandle());
        this.wrapped = ((CraftMob) controller.getEntity()).getHandle().getMoveControl();
    }

    public MoveControl<?> getWrapped() {
        return wrapped;
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
    }
}
