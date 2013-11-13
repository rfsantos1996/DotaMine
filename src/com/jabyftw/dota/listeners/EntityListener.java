package com.jabyftw.dota.listeners;

import com.jabyftw.dota.DotaMine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EntityEquipment;

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
    public void onEntityDamage(EntityDamageByEntityEvent e) {
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
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getEntity().getKiller() instanceof Player) {
                Player dead = (Player) e.getEntity();
                Player killer = e.getEntity().getKiller();

                pl.players.get(killer).addKill(pl.players.get(dead).getKillstreak());
                pl.players.get(dead).addDeath();
            }
        } else {
            EntityEquipment eq = e.getEntity().getEquipment();
            eq.setHelmet(null);
            eq.setChestplate(null);
            eq.setLeggings(null);
            eq.setBoots(null);
            if(e.getEntity().getKiller() instanceof Player) {
                Player killer = e.getEntity().getKiller();
                pl.players.get(killer).addLH();
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        //TODO: tower destroy
    }
}
