package com.gmail.blubberalls.MobPilot.controllers;

import org.bukkit.entity.AbstractCubeMob;


public class CubeController extends MobController<AbstractCubeMob> {
    public CubeController(AbstractCubeMob mob) {
        super(mob);
    }

    @Override
    public void onMoveControllerPreTick() {
        entity.setJumping(player.getCurrentInput().isJump());
    }
}
