package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class EntityJungleSpawnRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public EntityJungleSpawnRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Location spawnloc : pl.jungleSpawn) {
            spawn(spawnloc);
        }
    }

    private void spawn(Location spawnloc) {
        spawnloc.getChunk().load();
        boolean playernear = false;
        int spawn = 0;
        for (Player p : pl.ingameList.keySet()) {
            if (p.getLocation().distance(spawnloc) < 32) {
                playernear = true;
            }
        }
        if (pl.jungleEntityCreeps.size() > 0 && playernear) {
            for (Entity e : pl.jungleEntityCreeps) {
                if (e.getLocation().distance(spawnloc) < 22) {
                    spawn++;
                }
            }
        }
        if (spawn < 3 && playernear) {
            spawnCreeps(spawnloc);
        }
    }

    private void spawnCreeps(Location spawnloc) {
        for (int i = 0; i < 2; i++) {
            Zombie z = pl.getServer().getWorld(pl.worldName).spawn(spawnloc, Zombie.class);
            if (pl.megaCreeps) {
                ItemStack sword = new ItemStack(Material.STONE_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 4);
                z.getEquipment().setItemInHand(sword);
                z.getEquipment().setItemInHandDropChance(0);
            }
            z.setRemoveWhenFarAway(false);
            z.setCanPickupItems(false);
            z.setMaxHealth(32);
            pl.jungleEntityCreeps.add(z);
            pl.spawnedMobs.add(z);
        }
    }
}
