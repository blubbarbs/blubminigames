package com.gmail.blubberalls.MobPilot.nms;

import com.gmail.blubberalls.MobPilot.MobController;
import net.minecraft.world.entity.ai.control.JumpControl;
import org.bukkit.craftbukkit.entity.CraftMob;

public class JumpControlWrapper extends JumpControl {
    private JumpControl wrapped;

    public JumpControlWrapper(MobController<?> controller) {
        CraftMob mob = (CraftMob) controller.getEntity();
        super(mob.getHandle());
        wrapped = mob.getHandle().getJumpControl();
    }

    public JumpControl getWrapped() {
        return wrapped;
    }

    @Override
    public void jump() {
        wrapped.jump();
    }

    @Override
    public void tick() {}
}
