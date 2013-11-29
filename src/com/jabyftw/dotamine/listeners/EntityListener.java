package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.runnables.item.ShowRunnable;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
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
        if (e.getDamager() instanceof Projectile) {
            Projectile damager = (Projectile) e.getDamager();
            if (damager.getShooter() instanceof Player) {
                Player shooter = (Player) damager.getShooter();
                if (pl.ingameList.get(shooter).getAttackType() == 1) {
                    e.setCancelled(true);
                    return;
                }
                if (pl.teleporting.containsKey(shooter)) {
                    cancelTp(shooter);
                    return;
                }
                if (checkForShadowBlade(shooter)) {
                    e.setCancelled(true);
                    return;
                }
                if (e.getEntity() instanceof Player) {
                    Player damaged = (Player) e.getEntity();
                    if (pl.spectators.containsKey(damaged)) {
                        e.setCancelled(true);
                        return;
                    }
                    if (pl.hasTarrasque.contains(damaged)) {
                        pl.removeTarrasque(damaged);
                    }
                    if (e.getDamage() > 0) {
                        pl.breakEffect(damaged.getLocation(), 3, 11);
                    }
                    damaged.damage(e.getDamage(), damager);
                    e.setCancelled(true); // remove knockback
                    return;
                }
            }
            if (e.getDamage() > 0) {
                pl.breakEffect(e.getEntity().getLocation(), 2, 55);
            }

        }
        if (e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            if (pl.teleporting.containsKey(damager)) {
                cancelTp(damager);
                return;
            }
            if (pl.spectators.containsKey(damager)) {
                e.setCancelled(true);
                return;
            }
            if (pl.invisibleSB.containsKey(damager)) {
                if (checkForShadowBlade(damager)) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e.getEntity() instanceof Player) {
                Player damaged = (Player) e.getEntity();
                if (pl.ingameList.containsKey(damager) && pl.ingameList.containsKey(damaged)) {
                    if (pl.ingameList.get(damaged).getTeam() == pl.ingameList.get(damager).getTeam()) {
                        e.setCancelled(true);
                        return;
                    }
                    if (pl.ingameList.get(damager).getAttackType() == 2) {
                        if (damager.getItemInHand().getType().equals(Material.IRON_SWORD) || damager.getItemInHand().getType().equals(Material.DIAMOND_SWORD) || damager.getItemInHand().getType().equals(Material.GOLD_SWORD)) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (pl.hasTarrasque.contains(damaged)) {
                        pl.removeTarrasque(damaged);
                        if (e.getDamage() > 0) {
                            pl.breakEffect(damaged.getLocation(), 3, 11);
                        }
                        return;
                    }
                }
            }
            if (e.getDamage() > 0) {
                pl.breakEffect(e.getEntity().getLocation(), 2, 55);
            }
        }
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (pl.hasTarrasque.contains(p)) {
                pl.removeTarrasque(p);
            }
            if (pl.teleporting.containsKey(p)) {
                cancelTp(p);
            }
            if (e.getDamage() > 0) {
                pl.breakEffect(p.getLocation(), 3, 11);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getCause().equals(DamageCause.FIRE_TICK) || e.getCause().equals(DamageCause.FIRE)) {
            e.setCancelled(true);
            return;
        }
        if (e.getEntity() instanceof Player) {
            if (pl.spectators.containsKey((Player) e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarged(EntityTargetEvent e) {
        if (e.getEntityType().equals(EntityType.PLAYER)) {
            if (pl.invisibleSB.containsKey((Player) e.getTarget())) {
                e.setCancelled(true);
                return;
            }
            if (pl.spectators.containsKey((Player) e.getEntity())) {
                e.setCancelled(true);
            }
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
            if (pl.useControllableMobs) {
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
                                pl.getServer().getWorld(pl.worldName).dropItemNaturally(e.getEntity().getLocation(), new ItemStack(Material.ARROW, pl.getRandom(0, 3)));
                                e.setDroppedExp(pl.getRandom(6, 8)); // normal = 5
                            }
                        }
                        ControllableMobs.releaseControl(cm);
                        pl.controlMobs.remove(cm);
                        return;
                    }
                }
            } else {
                for (Entity en : pl.spawnedMobs) {
                    if (en.equals(e.getEntity())) {
                        if (pl.laneEntityCreeps.contains(en)) {
                            pl.laneEntityCreeps.remove(en);
                            if (e.getEntity().getKiller() != null) {
                                pl.ingameList.get(e.getEntity().getKiller()).addLH();
                            }
                        } else if (pl.jungleEntityCreeps.contains(en)) {
                            pl.jungleEntityCreeps.remove(en);
                            if (e.getEntity().getKiller() != null) {
                                pl.ingameList.get(e.getEntity().getKiller()).addJungleLH();
                                pl.getServer().getWorld(pl.worldName).dropItemNaturally(e.getEntity().getLocation(), new ItemStack(Material.ARROW, pl.getRandom(0, 3)));
                                e.setDroppedExp(pl.getRandom(6, 8)); // normal = 5
                            }
                        }
                        pl.spawnedMobs.remove(en);
                        return;
                    }
                }
            }
        }
    }

    private boolean checkForShadowBlade(Player p) {
        if (pl.invisibleSB.containsKey(p) && pl.ingameList.size() > 0) {
            pl.getServer().getScheduler().cancelTask(pl.invisibleSB.get(p));
            pl.getServer().getScheduler().runTask(pl, new ShowRunnable(pl, p, 1));
            return true;
        }
        return false;
    }

    private void cancelTp(Player p) {
        pl.getServer().getScheduler().cancelTask(pl.teleporting.get(p));
        pl.teleporting.remove(p);
        p.sendMessage(pl.getLang("lang.tpCancelled"));
    }
}
