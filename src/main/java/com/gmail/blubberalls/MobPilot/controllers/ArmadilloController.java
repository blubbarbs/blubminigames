package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.craftbukkit.entity.CraftArmadillo;
import org.bukkit.entity.Armadillo;

public class ArmadilloController extends MobController<Armadillo> {
    public ArmadilloController(Armadillo entity) {
        super(entity);
    }

    @Override
    public void onStartSprint() {
        entity.rollUp();
        setImmobile(true);
    }

    @Override
    public void onStopSprint() {
        CraftArmadillo craftArmadillo = (CraftArmadillo) entity;

        craftArmadillo.getHandle().rollOut();
        setImmobile(false);
    }
}
