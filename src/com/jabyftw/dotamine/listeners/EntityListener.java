package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

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
        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Player damaged = (Player) e.getEntity();
            
            if(pl.ingameList.containsKey(damager) && pl.ingameList.containsKey(damaged)) {
                if(pl.ingameList.get(damaged).getTeam() == pl.ingameList.get(damager).getTeam()) {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player) {
            return;
        }
        if(e.getCause().equals(DamageCause.FIRE)) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(e.getEntity() instanceof Player) {
            if(e.getEntity().getKiller() instanceof Player) {
                Player dead = (Player) e.getEntity();
                Player killer = dead.getKiller();
                
                pl.ingameList.get(killer).addKill(pl.ingameList.get(dead));
                pl.ingameList.get(dead).addDeath();
            }
        } else {
            e.getEntity().getEquipment().setArmorContents(null);
            e.getDrops().clear();
            if(e.getEntity().getKiller() instanceof Player) {
                pl.ingameList.get(e.getEntity().getKiller()).addLH();
            }
            for(ControllableMob cm : pl.controlMobs.keySet()) {
                if(cm.getEntity().equals(e.getEntity())) {
                    ControllableMobs.releaseControl(cm);
                    return;
                }
            }
        }
    }
}
