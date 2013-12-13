package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.runnables.item.ShowRunnable;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 *
 * @author Rafael
 */
public class EntityListener implements Listener {

    private final DotaMine pl;

    public EntityListener(DotaMine pl) {
        this.pl = pl;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) { // Hit player
            Player damaged = (Player) e.getEntity();
            if (e.getDamager() instanceof Player) { // Player
                Player damager = (Player) e.getDamager();
                if (cancelBothIngame(damaged, damager)) {
                    e.setCancelled(true);
                    return;
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
                if (!e.isCancelled()) {
                    checkTeleport(damager);
                }
            } else if (e.getDamager() instanceof Projectile) { // Arrow
                Projectile proj = (Projectile) e.getDamager();
                if (proj.getShooter() instanceof Player) {
                    Player shooter = (Player) proj.getShooter();
                    if (cancelBothIngame(damaged, shooter)) {
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
                        if (!checkIngame(damaged) && checkIngame(shooter)) { // spectator wont get hurt, and the arrow wont stop
                            proj.setBounce(false);
                            Vector vel = proj.getVelocity();
                            damaged.teleport(damaged.getLocation().add(0, 3, 0));
                            damaged.setFlying(true);
                            Arrow newArrow = shooter.launchProjectile(Arrow.class);
                            newArrow.setShooter(shooter);
                            newArrow.setVelocity(vel);
                            e.setCancelled(true);
                            proj.remove();
                        } else if (checkIngame(damaged) && checkIngame(shooter)) {
                            //e.setCancelled(true);
                            //damaged.damage(e.getDamage());
                            checkTarrasque(damaged);
                            checkTeleport(damaged);
                            checkTeleport(shooter);
                            pl.breakEffect(damaged.getLocation(), 3, 11);
                        }
                    }
                }
            }
        } else { // Hit a non-player
            if (e.getDamager() instanceof Player) { // Player
                Player damager = (Player) e.getDamager();
                if (checkSpectator(damager)) {
                    e.setCancelled(true);
                    return;
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
                        if (checkSpectator(shooter)) {
                            e.setCancelled(true);
                            return;
                        }
                    } else {
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
            }
            if (!e.isCancelled()) {
                pl.breakEffect(e.getEntity().getLocation(), 2, 55);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().getLocation().getWorld().getName().equalsIgnoreCase(pl.worldName)) {
            if (e.getCause().equals(DamageCause.FIRE_TICK) || e.getCause().equals(DamageCause.FIRE)) {
                e.getEntity().setFireTicks(0);
                pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StopFireRunnable(e.getEntity()), 1);
                e.setCancelled(true);
                return;
            }
            if (e.getEntity() instanceof Player) {
                if (pl.spectators.containsKey((Player) e.getEntity())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.getTarget() != null) {
            if (e.getTarget().getType().equals(EntityType.PLAYER)) {
                Player target = (Player) e.getTarget();
                if (pl.invisible.containsKey(target)) {
                    e.setCancelled(true);
                } else if (checkSpectator(target)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) {
            Player dead = (Player) e.getEntity();
            if (checkSpectator(dead)) {
                e.setDroppedExp(0);
                e.getDrops().clear();
                return;
            }
            if (checkIngame(dead)) {
                Player killer = null;
                if (dead.getKiller() instanceof Player) {
                    killer = dead.getKiller();
                } else {
                    if (dead.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                        EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) dead.getLastDamageCause();
                        if (ev.getDamager() instanceof Projectile) {
                            Projectile proj = (Projectile) ev.getDamager();
                            if (proj.getShooter() instanceof Player) {
                                killer = (Player) proj.getShooter();
                            }
                        }
                    }
                }
                if (killer != null) {
                    if (checkIngame(killer)) {
                        pl.ingameList.get(killer).addKill(pl.ingameList.get(dead));
                    } else {
                        killer.kickPlayer("killed player playing dota");
                    }
                    pl.ingameList.get(dead).addDeath();
                } else {
                    pl.ingameList.get(dead).addNeutralDeath();
                }
            }
        } else {
            e.getEntity().getEquipment().setArmorContents(null);
            e.getDrops().clear();
            if (pl.useControllableMobs) {
                LivingEntity en = e.getEntity();
                if (pl.laneCreeps.containsKey(en)) {
                    pl.laneCreeps.remove(en);
                    if (e.getEntity().getKiller() != null) {
                        pl.ingameList.get(e.getEntity().getKiller()).addLH();
                    }
                } else if (pl.jungleSpecialCreeps.containsKey(en)) {
                    if (e.getEntity().getKiller() != null) {
                        Player killer = e.getEntity().getKiller();
                        pl.ingameList.get(killer).addJungleLH();
                        int n = pl.jungleSpecialCreeps.get(en);
                        if (n == 1) { // red
                            killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 90, 0), true);
                        } else if (n == 2) { // blue
                            killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 90, 1), true);
                        }
                    }
                    e.setDroppedExp(pl.getRandom(12, 14));
                    pl.jungleSpecialCreeps.remove(en);
                } else if (pl.jungleCreeps.containsKey(en)) {
                    pl.jungleCreeps.remove(en);
                    if (e.getEntity().getKiller() != null) {
                        pl.ingameList.get(e.getEntity().getKiller()).addJungleLH();
                    }
                    e.setDroppedExp(pl.getRandom(7, 9));
                }
                ControllableMobs.releaseControl(pl.controlMobs.get(en));
            } else {
                LivingEntity en = e.getEntity();
                if (pl.spawnedMobs.contains(en)) {
                    if (pl.laneEntityCreeps.contains(en)) {
                        pl.laneEntityCreeps.remove(en);
                        if (e.getEntity().getKiller() != null) {
                            pl.ingameList.get(en.getKiller()).addLH();
                        }
                    } else if (pl.jungleEntitySpecialCreeps.containsKey(en)) {
                        if (e.getEntity().getKiller() != null) {
                            Player killer = e.getEntity().getKiller();
                            pl.ingameList.get(killer).addJungleLH();
                            int n = pl.jungleEntitySpecialCreeps.get(en);
                            if (n == 1) { // red
                                killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 90, 0), true);
                            } else if (n == 2) { // blue
                                killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 90, 1), true);
                            }
                        }
                        e.setDroppedExp(pl.getRandom(7, 9));
                        pl.jungleEntitySpecialCreeps.remove(en);
                    } else if (pl.jungleEntityCreeps.contains(en)) {
                        if (e.getEntity().getKiller() != null) {
                            Player killer = e.getEntity().getKiller();
                            pl.ingameList.get(killer).addJungleLH();
                        }
                        e.setDroppedExp(pl.getRandom(7, 9));
                        pl.jungleEntityCreeps.remove(en);
                    }
                    pl.spawnedMobs.remove(en);
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

    private boolean checkSpectator(Player p) {
        return pl.spectators.containsKey(p);
    }

    private void checkTeleport(Player p) {
        if (pl.teleporting.containsKey(p)) {
            cancelTp(p);
        }
    }

    private boolean cancelBothIngame(Player damaged, Player damager) {
        if (!checkIngame(damager) && !checkIngame(damaged)) { // if both arent ingame, execute
            if (damager.getLocation().distanceSquared(pl.normalSpawn) < (15 * 15) || damaged.getLocation().distanceSquared(pl.normalSpawn) < (15 * 15)) { // Anti PVP on lobby
                return true;
            }
        } else if (!checkIngame(damager) || !checkIngame(damaged)) {
            return true;
        }
        return false;
    }

    private class StopFireRunnable implements Runnable {

        private final Entity entity;

        public StopFireRunnable(Entity entity) {
            this.entity = entity;
        }

        @Override
        public void run() {
            entity.setFireTicks(0);
        }
    }
}
