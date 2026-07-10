package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.entity.Blaze;

public class BlazeController extends MobController<Blaze> {
    public BlazeController(Blaze mob) {
        super(mob, Capability.ATTACK);
    }

}