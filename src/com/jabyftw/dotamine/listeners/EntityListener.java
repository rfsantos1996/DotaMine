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
        if (e.getEntity() instanceof Player) { // Hit player
            Player damaged = (Player) e.getEntity();
            if (!checkIngame(damaged)) {
                e.setCancelled(true);
                return;
            }
            if (e.getDamager() instanceof Player) { // Player
                Player damager = (Player) e.getDamager();
                if (!checkIngame(damager)) {
                    e.setCancelled(true);
                }
                if (checkForShadowBlade(damager)) {
                    e.setCancelled(true);
                    return;
                }
                if (pl.ingameList.get(damager).getAttackType() == 2) {
                    if (damager.getItemInHand().getType().equals(Material.IRON_SWORD) || damager.getItemInHand().getType().equals(Material.DIAMOND_SWORD) || damager.getItemInHand().getType().equals(Material.GOLD_SWORD)) {
                        e.setCancelled(true);
                        return;
                    }
                }

                checkTeleport(damager);
            } else if (e.getDamager() instanceof Projectile) { // Arrow
                Projectile proj = (Projectile) e.getDamager();
                if (proj.getShooter() instanceof Player) {
                    Player shooter = (Player) proj.getShooter();
                    if (!checkIngame(shooter)) {
                        e.setCancelled(true);
                        return;
                    }
                    if (pl.ingameList.get(shooter).getAttackType() == 1) {
                        e.setCancelled(true);
                        return;
                    }
                    if (checkForShadowBlade(shooter)) {
                        e.setCancelled(true);
                        return;
                    }

                    checkTeleport(shooter);
                    if (!e.isCancelled()) {
                        damaged.damage(e.getDamage());
                        checkTarrasque(damaged);
                        checkTeleport(damaged);
                        pl.breakEffect(damaged.getLocation(), 3, 11);
                        e.setCancelled(true);
                        return;
                    }
                }

                if (!e.isCancelled()) {
                    checkTarrasque(damaged);
                    checkTeleport(damaged);
                    pl.breakEffect(damaged.getLocation(), 3, 11);
                }
            }
        } else { // Hit a non-player
            if (e.getDamager() instanceof Player) { // Player
                Player damager = (Player) e.getDamager();
                if (!checkIngame(damager)) {
                    e.setCancelled(true);
                }
                if (checkForShadowBlade(damager)) {
                    e.setCancelled(true);
                    return;
                }
                if (pl.ingameList.get(damager).getAttackType() == 2) {
                    if (damager.getItemInHand().getType().equals(Material.IRON_SWORD) || damager.getItemInHand().getType().equals(Material.DIAMOND_SWORD) || damager.getItemInHand().getType().equals(Material.GOLD_SWORD)) {
                        e.setCancelled(true);
                        return;
                    }
                }

                checkTeleport(damager);
            } else if (e.getDamager() instanceof Projectile) { // Arrow
                Projectile proj = (Projectile) e.getDamager();
                if (proj.getShooter() instanceof Player) {
                    Player shooter = (Player) proj.getShooter();
                    if (!checkIngame(shooter)) {
                        e.setCancelled(true);
                        return;
                    }
                    if (pl.ingameList.get(shooter).getAttackType() == 1) {
                        e.setCancelled(true);
                        return;
                    }
                    if (checkForShadowBlade(shooter)) {
                        e.setCancelled(true);
                        return;
                    }

                    checkTeleport(shooter);
                }
            }
            if (!e.isCancelled()) {
                pl.breakEffect(e.getEntity().getLocation(), 2, 55);
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
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.getTarget() != null) {
            if (e.getTarget().getType().equals(EntityType.PLAYER)) {
                Player target = (Player) e.getTarget();
                if (!pl.ingameList.containsKey(target)) {
                    e.setCancelled(true);
                } else {
                    if (pl.invisible.containsKey(target)) {
                        e.setCancelled(true);
                    }
                }
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
        if (pl.invisible.containsKey(p)) {
            if (pl.invisible.get(p) == 1) {
                pl.getServer().getScheduler().cancelTask(pl.invisibleSB.get(p));
                pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new ShowRunnable(pl, p));
            } else {
                pl.getServer().getScheduler().cancelTask(pl.invisibleW.get(p));
                pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new ShowRunnable(pl, p));
            }
            return true;
        }
        return false;
    }

    private void cancelTp(Player p) {
        pl.getServer().getScheduler().cancelTask(pl.teleporting.get(p));
        pl.teleporting.remove(p);
        p.sendMessage(pl.getLang("lang.tpCancelled"));
    }

    private void checkTarrasque(Player damaged) {
        if (pl.hasTarrasque.contains(damaged)) {
            pl.removeTarrasque(damaged);
        }
    }

    private boolean checkIngame(Player p) {
        return pl.ingameList.containsKey(p);
    }

    private void checkTeleport(Player p) {
        if (pl.teleporting.containsKey(p)) {
            cancelTp(p);
        }
    }
}
