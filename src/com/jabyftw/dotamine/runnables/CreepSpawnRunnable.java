package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackMelee;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackRanged;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AILookAtEntity;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIRandomLookaround;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AITargetNearest;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.AttributeModifierFactory;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.ModifyOperation;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class CreepSpawnRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private double LknockM, LdmgM, LhealthM;
    private double LknockR, LdmgR, LhealthR;

    public CreepSpawnRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Location spawnloc : pl.botCreepSpawn) {
            spawn(spawnloc);
            pl.debug("creepspawn bot");
        }
        for (Location spawnloc : pl.topCreepSpawn) {
            spawn(spawnloc);
            pl.debug("creepspawn top");
        }
        for (Location spawnloc : pl.midCreepSpawn) {
            spawn(spawnloc);
            pl.debug("creepspawn mid");
        }
    }

    private void spawn(Location spawnloc) {
        spawnloc.getChunk().load();
        boolean playernear = false;
        int spawn = 0;
        pl.debug("spawn method");
        for (Player p : pl.ingameList.keySet()) {
            if (p.getLocation().distanceSquared(spawnloc) < (32 * 32)) {
                playernear = true;
                pl.debug("player is near");
            } else {
                pl.debug("player is far");
            }
        }
        if (pl.laneCreeps.size() > 0 && playernear) {
            for (Entity en : pl.laneCreeps.keySet()) {
                if (en.getLocation().distanceSquared(spawnloc) < (22 * 22)) {
                    spawn++;
                    pl.debug("spawn++");
                }
            }
        }
        if (spawn < 3 && playernear) {
            spawnLaneCreeps(spawnloc);
            pl.debug("creepspawn spawn");
        }
    }

    private void spawnLaneCreeps(Location spawnloc) {
        if (pl.megaCreeps) {
            LknockM = 0.6;
            LknockR = 0.4;
            LdmgM = 3;
            LdmgR = 7;
            LhealthM = 6;
            LhealthR = 4;
        } else {
            LknockM = 0.4;
            LknockR = 0.2;
            LdmgM = 2;
            LdmgR = 4;
            LhealthM = 3;
            LhealthR = 2;
        }
        for (int i = 0; i < 4; i++) {
            Zombie z = pl.getServer().getWorld(pl.worldName).spawn(spawnloc, Zombie.class);
            if (pl.megaCreeps) {
                EntityEquipment eq = z.getEquipment();
                ItemStack sword = new ItemStack(Material.STONE_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 4);
                eq.setItemInHandDropChance(0);
                eq.setItemInHand(sword);
                eq.setChestplateDropChance(0);
                eq.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                eq.setLeggingsDropChance(0);
                eq.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
            }
            z.setRemoveWhenFarAway(false);
            z.setCanPickupItems(false);
            ControllableMob<Zombie> cz = ControllableMobs.putUnderControl(z, true);
            cz.getAttributes().setMaximumNavigationDistance(16);
            cz.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", LknockM, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getAttackDamageAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "attack dmg", LdmgM, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "max health", LhealthM, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getEntity().setHealth(cz.getEntity().getMaxHealth());
            cz.getAI().addBehavior(new AIAttackMelee(1, 1.1));
            cz.getAI().addBehavior(new AITargetNearest(2, 14, false, 20 * 4));
            cz.getAI().addBehavior(new AILookAtEntity(4, (float) 8));
            cz.getAI().addBehavior(new AIRandomLookaround(5));
            pl.laneCreeps.put(z, cz);
            pl.controlMobs.put(z, cz);
        }
        for (int i = 0; i < 2; i++) {
            Skeleton s = pl.getServer().getWorld(pl.worldName).spawn(spawnloc, Skeleton.class);
            ItemStack bow = new ItemStack(Material.BOW);
            if (pl.megaCreeps) {
                EntityEquipment eq = s.getEquipment();
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 2);
                eq.setChestplateDropChance(0);
                eq.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                eq.setLeggingsDropChance(0);
                eq.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
            }
            s.getEquipment().setItemInHand(bow);
            s.getEquipment().setItemInHandDropChance(0);
            s.setRemoveWhenFarAway(false);
            s.setCanPickupItems(false);
            ControllableMob<Skeleton> cs = ControllableMobs.putUnderControl(s, true);
            cs.getAttributes().setMaximumNavigationDistance(16);
            cs.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", LknockR, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getAttributes().getAttackDamageAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "attack dmg", LdmgR, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "max health", LhealthR, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getEntity().setHealth(cs.getEntity().getMaxHealth());
            cs.getAI().addBehavior(new AIAttackRanged(1, 1.2, 14, 40));
            cs.getAI().addBehavior(new AITargetNearest(2, 16, false, 20 * 3));
            cs.getAI().addBehavior(new AILookAtEntity(4, (float) 20));
            cs.getAI().addBehavior(new AIRandomLookaround(5));
            pl.laneCreeps.put(s, cs);
            pl.controlMobs.put(s, cs);
        }
    }
}
