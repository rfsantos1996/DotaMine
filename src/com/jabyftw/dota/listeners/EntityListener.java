package com.jabyftw.dota.listeners;

import com.jabyftw.dota.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Rafael
 */
public class EntityListener implements Listener {

    private final DotaMine pl;

    public EntityListener(DotaMine pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if ((e.getDamager() instanceof Player) && (e.getEntity() instanceof Player)) {
            Player damager = (Player) e.getDamager();
            Player damaged = (Player) e.getEntity();

            if ((pl.players.containsKey(damager) && (pl.players.containsKey(damaged)))) {
                if (pl.players.get(damager).getTeam() == pl.players.get(damaged).getTeam()) {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            return;
        }
        if (e.getCause() == DamageCause.FIRE || e.getCause() == DamageCause.FIRE_TICK) {
            //e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getEntity().getKiller() instanceof Player) {
                Player dead = (Player) e.getEntity();
                Player killer = e.getEntity().getKiller();

                pl.players.get(killer).addKill(pl.players.get(dead));
                pl.players.get(dead).addDeath();
            }
        } else {
            EntityEquipment eq = e.getEntity().getEquipment();
            eq.setHelmet(null);
            eq.setChestplate(null);
            eq.setLeggings(null);
            eq.setBoots(null);
            if (e.getEntity().getKiller() instanceof Player) {
                Player killer = e.getEntity().getKiller();
                pl.players.get(killer).addLH();
            }
            if (pl.controllablemobs.contains(e.getEntity())) {
                e.getDrops().clear();
                return;
            }
            if (e.getEntity() instanceof Skeleton) {
                e.getDrops().clear();
                Location loc = e.getEntity().getLocation();
                pl.getServer().getWorld(pl.worldName).dropItem(loc, new ItemStack(Material.ARROW, 1));
            }

        }
    }
}
