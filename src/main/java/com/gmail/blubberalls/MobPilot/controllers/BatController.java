package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.entity.Bat;

public class BatController extends MobController<Bat> {
    public BatController(Bat entity) {
        super(entity);
    }

    @Override
    public void onInitialize() {
        entity.setAI(false);
    }

    @Override
    public void onDeinitialize() {
        entity.setAI(true);
    }
}
