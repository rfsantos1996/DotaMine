package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
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
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Player damaged = (Player) e.getEntity();

            if (pl.ingameList.containsKey(damager) && pl.ingameList.containsKey(damaged)) {
                if (pl.ingameList.get(damaged).getTeam() == pl.ingameList.get(damager).getTeam()) {
                    e.setCancelled(true);
                } else if (e.getCause().equals(DamageCause.PROJECTILE) && pl.ingameList.get(damager).getAttackType() == 1) {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        } else if (e.getDamager() instanceof Player) {
            if (pl.spectators.contains((Player) e.getDamager())) {
                e.setCancelled(true);
            } else if (e.getCause().equals(DamageCause.PROJECTILE) && pl.ingameList.get((Player) e.getDamager()).getAttackType() == 1) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            return;
        }
        if (e.getCause().equals(DamageCause.FIRE_TICK) || e.getCause().equals(DamageCause.FIRE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) {
            Player dead = (Player) e.getEntity();
            if (e.getEntity().getKiller() instanceof Player) {
                Player killer = dead.getKiller();
                pl.ingameList.get(killer).addKill(pl.ingameList.get(dead));
                pl.ingameList.get(dead).addDeath();
            } else {
                pl.ingameList.get(dead).addNeutralDeath();
            }
        } else {
            e.getEntity().getEquipment().setArmorContents(null);
            e.getDrops().clear();
            for (ControllableMob cm : pl.controlMobs) {
                if (cm.getEntity().equals(e.getEntity())) {
                    if (pl.laneCreeps.contains(cm)) {
                        pl.laneCreeps.remove(cm);
                        if (e.getEntity().getKiller() != null) {
                            pl.ingameList.get(e.getEntity().getKiller()).addLH();
                        }
                    } else if (pl.jungleCreeps.contains(cm)) {
                        pl.jungleCreeps.remove(cm);
                        if (e.getEntity().getKiller() != null) {
                            pl.ingameList.get(e.getEntity().getKiller()).addJungleLH();
                            pl.getServer().getWorld(pl.worldName).dropItemNaturally(e.getEntity().getLocation(), new ItemStack(Material.ARROW, 1));
                        }
                    }
                    ControllableMobs.releaseControl(cm);
                    pl.controlMobs.remove(cm);
                    return;
                }
            }
        }
    }
}
