package com.gmail.blubberalls.MobPilot.controllers;

import com.gmail.blubberalls.MobPilot.MobController;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spellcaster;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IllusionerController extends MobController<Illusioner> {
    static PotionEffect BLINDNESS_EFFECT = new PotionEffect(PotionEffectType.BLINDNESS, 400, 0);
    static PotionEffect INVISIBILITY_EFFECT = new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0);

    public IllusionerController(Illusioner entity) {
        super(entity);
        registerAbility("Cast Blindness", ItemStack.of(Material.ENCHANTED_BOOK), this::castBlindness, 10);
        registerAbility("Cast Mirror", ItemStack.of(Material.ENCHANTED_BOOK), this::castMirror, 10);
    }

    @Override
    protected void onInitialize() {
        entity.setSpell(Spellcaster.Spell.NONE);
    }

    @Override
    protected void onDeinitialize() {
        entity.setSpell(Spellcaster.Spell.NONE);
    }

    @Override
    public void tick() {
        super.tick();

        if (entity.hasActiveItem() && entity.getActiveItem().getType() == Material.BOW)
            entity.setAggressive(true);
        else if (!entity.hasActiveItem() && entity.isAggressive())
            entity.setAggressive(false);
    }

    protected boolean castBlindness() {
        setImmobile(true);
        entity.setSpell(Spellcaster.Spell.BLINDNESS);
        entity.getWorld().playSound(entity, Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1, 1);

        registerRunnable(() -> {
            for (Entity e : entity.getNearbyEntities(10, 3, 10)) {
                if (!(e instanceof LivingEntity le))
                    continue;

                le.addPotionEffect(BLINDNESS_EFFECT);
            }
            entity.setSpell(Spellcaster.Spell.NONE);
            setImmobile(false);
        }, 20L);

        return true;
    }

    protected boolean castMirror() {
        setImmobile(true);
        entity.setSpell(Spellcaster.Spell.DISAPPEAR);
        entity.getWorld().playSound(entity, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1, 1);

        registerRunnable(() -> {
            entity.addPotionEffect(INVISIBILITY_EFFECT);
            entity.setSpell(Spellcaster.Spell.NONE);
            setImmobile(false);
        }, 20L);

        return true;
    }
}
