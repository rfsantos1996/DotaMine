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
        for (Location spawnloc : pl.jungleSpawn.keySet()) {
            if (!pl.jungleSpawn.get(spawnloc)) {
                spawn(spawnloc);
            }
        }
    }

    private void spawn(Location spawnloc) {
        spawnloc.getChunk().load();
        boolean playernear = false;
        boolean spawn = true;
        for (Player p : pl.ingameList.keySet()) {
            if (p.getLocation().distanceSquared(spawnloc) < (32*32)) {
                playernear = true;
            }
        }
        if (pl.jungleEntityCreeps.size() > 0 && playernear) {
            for (Entity e : pl.jungleEntityCreeps) {
                if (e.getLocation().distanceSquared(spawnloc) < (22*22)) {
                    spawn = true;
                }
            }
        }
        if (spawn && playernear) {
            spawnCreeps(spawnloc);
            pl.jungleSpawn.put(spawnloc, true);
            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new RecentlyRunnable(pl, spawnloc, 3), 20 * 90);
        }
    }

    private void spawnCreeps(Location spawnloc) {
        for (int i = 0; i < 2; i++) {
            Zombie z = pl.getServer().getWorld(pl.worldName).spawn(spawnloc, Zombie.class);
            ItemStack sword = new ItemStack(Material.STONE_SWORD);
            sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);
            z.getEquipment().setItemInHand(sword);
            z.getEquipment().setItemInHandDropChance(0);
            z.setRemoveWhenFarAway(false);
            z.setCanPickupItems(false);
            z.setMaxHealth(32);
            z.setHealth(z.getMaxHealth());
            pl.jungleEntityCreeps.add(z);
            pl.spawnedMobs.add(z);
        }
    }
}
