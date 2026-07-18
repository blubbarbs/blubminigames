package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftIronGolem;
import org.bukkit.entity.IronGolem;
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
    protected void onPlayerEquipmentChange(EquipmentSlot slot, ItemStack newStack) {
        if (newStack.getType() == Material.POPPY) {
            entity.playEffect(EntityEffect.IRON_GOLEM_ROSE);
        }
        else {
            entity.playEffect(EntityEffect.IRON_GOLEM_SHEATH);
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
