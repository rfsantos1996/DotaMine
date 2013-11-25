package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
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
                }
            } else {
                e.setCancelled(true);
            }
        } else if(e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Entity damaged = e.getEntity();
            for(ControllableMob cm : pl.controlMobs.keySet()) {
                if(damaged.equals(cm.getEntity())) {
                    int team = pl.controlMobs.get(cm);
                    if(pl.ingameList.get(damager).getTeam() == team) {
                        e.setCancelled(true);
                    }
                }
            }
            if(pl.spectators.contains(damager)) {
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
            if (e.getEntity().getKiller() instanceof Player) {
                Player dead = (Player) e.getEntity();
                Player killer = dead.getKiller();

                pl.ingameList.get(killer).addKill(pl.ingameList.get(dead));
                pl.ingameList.get(dead).addDeath();
            }
        } else {
            e.getEntity().getEquipment().setArmorContents(null);
            e.getDrops().clear();
            if (e.getEntity().getKiller() instanceof Player) {
                pl.ingameList.get(e.getEntity().getKiller()).addLH();
            }
            for (ControllableMob cm : pl.controlMobs.keySet()) {
                if (cm.getEntity().equals(e.getEntity())) {
                    if (pl.blueCreeps.contains(cm)) {
                        pl.blueCreeps.remove(cm);
                    } else if (pl.blueRangedCreeps.contains(cm)) {
                        pl.blueRangedCreeps.remove(cm);
                        pl.getServer().getWorld(pl.worldName).dropItemNaturally(cm.getEntity().getLocation(), new ItemStack(Material.ARROW, 2));
                    } else if (pl.redCreeps.contains(cm)) {
                        pl.redCreeps.remove(cm);
                    } else if (pl.redRangedCreeps.contains(cm)) {
                        pl.redRangedCreeps.remove(cm);
                        pl.getServer().getWorld(pl.worldName).dropItemNaturally(cm.getEntity().getLocation(), new ItemStack(Material.ARROW, 2));
                    } else if(pl.jungleCreeps.contains(cm)) {
                        pl.jungleCreeps.remove(cm);
                        if(e.getEntity().getKiller() != null) {
                            pl.ingameList.get(e.getEntity().getKiller()).addJungleLH(); // add double ammount for killing jungle
                            pl.getServer().getWorld(pl.worldName).dropItemNaturally(cm.getEntity().getLocation(), new ItemStack(Material.ARROW, 4));
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
