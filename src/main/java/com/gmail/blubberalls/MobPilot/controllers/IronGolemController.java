package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.IronGolem;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class IronGolemController extends MobController<IronGolem> {
    public IronGolemController(IronGolem mob) {
        super(mob, Capability.ATTACK);
    }

    @Override
    public void swingAnimation() {
        entity.playEffect(EntityEffect.ENTITY_ATTACK);
    }

    @EventHandler
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (event.getEntity() != player)
            return;

        super.onEquipmentChange(event);

        if (!event.getEquipmentChanges().containsKey(EquipmentSlot.HAND))
            return;

        ItemStack item = event.getEquipmentChanges().get(EquipmentSlot.HAND).newItem();

        if (item.getType() == Material.POPPY)
            entity.playEffect(EntityEffect.IRON_GOLEM_ROSE);
        else
            entity.playEffect(EntityEffect.IRON_GOLEM_SHEATH);
    }
}
