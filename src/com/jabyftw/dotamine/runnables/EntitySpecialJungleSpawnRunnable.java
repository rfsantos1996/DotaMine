package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class EntitySpecialJungleSpawnRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public EntitySpecialJungleSpawnRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Location loc : pl.jungleRedSpawn.keySet()) {
            if (!pl.jungleRedSpawn.get(loc)) {
                spawn(loc, 1);
            }
        }
        for (Location loc : pl.jungleBlueSpawn.keySet()) {
            if (!pl.jungleBlueSpawn.get(loc)) {
                spawn(loc, 2);
            }
        }
    }

    private void spawn(Location spawnloc, int type) {
        spawnloc.getChunk().load();
        boolean playernear = false;
        boolean spawn = true;
        for (Player p : pl.ingameList.keySet()) {
            if (p.getLocation().distanceSquared(spawnloc) < (32*32)) {
                playernear = true;
            }
        }
        if (pl.jungleEntityCreeps.size() > 0 && playernear) {
            for (Entity e : pl.jungleEntityCreeps.keySet()) {
                if (e.getLocation().distanceSquared(spawnloc) < (22*22)) {
                    spawn = false;
                }
            }
        }
        if (spawn && playernear) {
            spawnJungle(spawnloc, type);
        }
    }

    private void spawnJungle(Location spawnloc, int type) {
        Zombie z = pl.getServer().getWorld(pl.worldName).spawn(spawnloc, Zombie.class);
        if (type == 1 || type == 2) {
            ItemStack sword = new ItemStack(Material.STONE_SWORD);
            sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);
            z.getEquipment().setItemInHand(sword);
            z.getEquipment().setItemInHandDropChance(0);
            ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
            LeatherArmorMeta lam = (LeatherArmorMeta) chest.getItemMeta();
            if (type == 1) {
                lam.setColor(Color.RED);
                pl.jungleRedSpawn.put(spawnloc, true);
            } else {
                lam.setColor(Color.BLUE);
                pl.jungleRedSpawn.put(spawnloc, true);
            }
            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new RecentlyRunnable(pl, spawnloc, type), 20 * 180);
            chest.setItemMeta(lam);
            z.getEquipment().setChestplate(chest);
            z.getEquipment().setChestplateDropChance(0);
        }
        z.setRemoveWhenFarAway(false);
        z.setCanPickupItems(false);
        z.setMaxHealth(48);
        z.setHealth(z.getMaxHealth());
        pl.jungleEntityCreeps.put(z, type);
        pl.spawnedMobs.add(z);
    }
}
