package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftIronGolem;
import org.bukkit.entity.IronGolem;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class IronGolemController extends MobController<IronGolem> {
    public IronGolemController(IronGolem mob) {
        super(mob, Capability.ATTACK);
    }

    @Override
    public void initializePlayerEquipment() {
        super.initializePlayerEquipment();

        CraftIronGolem craftIronGolem = (CraftIronGolem) this.getEntity();

        if (craftIronGolem.getHandle().getOfferFlowerTick() > 0) {
            ItemStack poppy = ItemStack.of(Material.POPPY);
            player.getInventory().setItem(0, poppy);
        }
    }

    @Override
    public void swingAnimation() {
        entity.playEffect(EntityEffect.ENTITY_ATTACK);
    }

    @Override
    public void tick() {
        CraftIronGolem craftIronGolem = (CraftIronGolem) this.getEntity();

        if (craftIronGolem.getHandle().getOfferFlowerTick() == 0) {
            player.getInventory().setItem(0, null);
        }
    }
}
