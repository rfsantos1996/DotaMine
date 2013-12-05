package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
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
public class EntityCreepSpawnRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private double LhealthR, LhealthM;

    public EntityCreepSpawnRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Location spawnloc : pl.botCreepSpawn) {
            spawn(spawnloc);
        }
        for (Location spawnloc : pl.topCreepSpawn) {
            spawn(spawnloc);
        }
        for (Location spawnloc : pl.midCreepSpawn) {
            spawn(spawnloc);
        }
    }

    private void spawn(Location spawnloc) {
        spawnloc.getChunk().load();
        boolean playernear = false;
        int spawn = 0;
        for (Player p : pl.ingameList.keySet()) {
            if (p.getLocation().distanceSquared(spawnloc) < (32*32)) {
                playernear = true;
            }
        }
        if (pl.laneEntityCreeps.size() > 0 && playernear) {
            for (Entity e : pl.laneEntityCreeps) {
                if (e.getLocation().distanceSquared(spawnloc) < (22*22)) {
                    spawn++;
                }
            }
        }
        if (spawn < 3 && playernear) {
            spawnCreeps(spawnloc);
        }
    }

    private void spawnCreeps(Location spawnloc) {
        if (pl.megaCreeps) {
            LhealthM = 6;
            LhealthR = 4;
        } else {
            LhealthM = 3;
            LhealthR = 2;
        }
        for (int i = 0; i < 4; i++) {
            Zombie z = pl.getServer().getWorld(pl.worldName).spawn(spawnloc, Zombie.class);
            if (pl.megaCreeps) {
                EntityEquipment eq = z.getEquipment();
                ItemStack sword = new ItemStack(Material.STONE_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 4);
                eq.setItemInHand(sword);
                eq.setItemInHandDropChance(0);
                eq.setChestplateDropChance(0);
                eq.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                eq.setLeggingsDropChance(0);
                eq.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));

            }
            z.setRemoveWhenFarAway(false);
            z.setCanPickupItems(false);
            z.setMaxHealth(20 + LhealthM);
            z.setHealth(z.getMaxHealth());
            pl.laneEntityCreeps.add(z);
            pl.spawnedMobs.add(z);
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
            s.setMaxHealth(20 + LhealthR);
            s.setHealth(s.getMaxHealth());
            pl.laneEntityCreeps.add(s);
            pl.spawnedMobs.add(s);
        }
    }
}
