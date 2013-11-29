package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Spectator;
import com.jabyftw.dotamine.runnables.item.ShadowShowRunnable;
import com.jabyftw.dotamine.runnables.StopRunnable;
import com.jabyftw.dotamine.runnables.item.ForceEffectRunnable;
import com.jabyftw.dotamine.runnables.item.ForceRunnable;
import com.jabyftw.dotamine.runnables.item.ForceStopRunnable;
import com.jabyftw.dotamine.runnables.item.ItemCDRunnable;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Rafael
 */
public class PlayerListener implements Listener {

    private final DotaMine pl;

    public PlayerListener(DotaMine pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (pl.state == pl.RESTARTING) {
            p.kickPlayer("RESTARTING");
            return;
        }
        pl.cleanPlayer(p, false, false);
        e.setJoinMessage(pl.getLang("lang.joinMessage").replaceAll("%name", p.getName()));
        p.teleport(pl.normalSpawn);
        if (pl.useVault) {
            if (p.hasPermission("dotamine.meele")) {
                pl.permission.playerRemove(p, "dotamine.meele");
            }
            if (p.hasPermission("dotamine.ranged")) {
                pl.permission.playerRemove(p, "dotamine.ranged");
            }
        }
        if (p.hasPermission("dotamine.play")) {
            if (pl.state == pl.WAITING) {
                p.sendMessage(pl.getLang("lang.youCanPlay"));
            } else if (pl.state == pl.WAITING_QUEUE) {
                p.sendMessage(pl.getLang("lang.youCanJoin"));
            }
        }
        if (p.hasPermission("dotamine.spectate")) {
            if (pl.state == pl.SPAWNING || pl.state == pl.PLAYING) {
                p.sendMessage(pl.getLang("lang.youCanSpectate"));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        e.setQuitMessage(pl.getLang("lang.quitMessage").replaceAll("%name", p.getName()));
        checkIngameThings(p);
        if (pl.ingameList.size() < 2 && (pl.state == pl.SPAWNING || pl.state == pl.PLAYING)) { // 1 player left
            pl.broadcast(pl.getLang("lang.onePlayerLeft"));
            pl.state = pl.RESTARTING;
            pl.getServer().setWhitelist(true);
            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StopRunnable(pl), 20 * 10);
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        Player p = e.getPlayer();
        e.setLeaveMessage(pl.getLang("lang.quitMessage").replaceAll("%name", p.getName()));
        checkIngameThings(p);
    }

    @EventHandler
    public void onPickUp(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        if (!pl.ingameList.containsKey(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (pl.ingameList.containsKey(p)) {
            if (p.getLocation().distance(pl.blueDeploy) < 5) {
                if (pl.ingameList.get(p).getTeam() == 1) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 2), true);
                } else {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 10, 1), true);
                }
            } else if (p.getLocation().distance(pl.redDeploy) < 5) {
                if (pl.ingameList.get(p).getTeam() == 2) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 2), true);
                } else {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 10, 1), true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getSlotType().equals(SlotType.ARMOR)) {
            if (e.getCurrentItem().getType().equals(Material.WOOL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (pl.ingameList.containsKey(p)) {
            BukkitScheduler bs = pl.getServer().getScheduler();
            if (e.getItem() != null && e.getItem().getType().equals(Material.BLAZE_ROD)) { // Shadow Blade
                if ((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    if (pl.shadowCD.contains(p)) {
                        p.sendMessage(pl.getLang("lang.itemCDMessage").replaceAll("%item", "Shadow Blade"));
                    } else {
                        p.sendMessage(pl.getLang("lang.itemUseMessage").replaceAll("%item", "Shadow Blade").replaceAll("%cd", "45"));
                        bs.scheduleSyncDelayedTask(pl, new ItemCDRunnable(pl, p, pl.SHADOW_BLADE), 20 * 45);
                        pl.shadowCD.add(p);
                        BukkitTask bt = bs.runTaskLater(pl, new ShadowShowRunnable(pl, p), 20 * 15);
                        pl.invisibleSB.put(p, bt);
                        pl.smokeEffect(p.getLocation(), 10);
                        pl.hidePlayerFromTeam(p, pl.getOtherTeam(p));
                    }
                }
            } else if (e.getItem() != null && e.getItem().getType().equals(Material.DIAMOND_HOE)) { // Force Staff
                if ((e.getAction() == Action.LEFT_CLICK_AIR) || (e.getAction() == Action.LEFT_CLICK_BLOCK)) {
                    if (pl.forceCD.contains(p)) {
                        p.sendMessage(pl.getLang("lang.itemCDMessage").replaceAll("%item", "Force Staff"));
                    } else {
                        p.sendMessage(pl.getLang("lang.itemUseMessage").replaceAll("%item", "Force Staff").replaceAll("%cd", "60"));
                        bs.scheduleSyncDelayedTask(pl, new ItemCDRunnable(pl, p, pl.FORCE_STAFF), 20 * 60);
                        pl.forceCD.add(p);
                        bs.runTask(pl, new ForceRunnable(p));
                        if (pl.useEffects) {
                            int run = bs.scheduleSyncRepeatingTask(pl, new ForceEffectRunnable(pl, p), 2, 1);
                            pl.forcingStaff.put(p, run);
                            bs.scheduleSyncDelayedTask(pl, new ForceStopRunnable(pl, p), 15);
                        }
                    }
                }
            } else if (e.getItem() != null && e.getItem().getType().equals(Material.FERMENTED_SPIDER_EYE)) { // Tarrasque
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    pl.checkForTarrasque(p);
                }
            } else if (e.getItem() != null && e.getItem().getType().equals(Material.WEB)) {
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (p.getItemInHand().getType().equals(Material.WEB)) {
                        p.getInventory().remove(p.getItemInHand());
                        List<Entity> list = p.getNearbyEntities(15 / 2, 13 / 2, 15 / 2);
                        list.add((Entity) p);
                        for (Entity en : list) {
                            if (en instanceof Player) {
                                Player pla = (Player) en;
                                if (pl.ingameList.containsKey(pla)) {
                                    if (pl.ingameList.get(pla).getTeam() == pl.ingameList.get(p).getTeam()) {
                                        pl.hidePlayerFromTeam(pla, pl.getOtherTeam(pla));
                                        BukkitTask bt = bs.runTaskLater(pl, new ShadowShowRunnable(pl, pla), 20 * 40);
                                        bs.scheduleSyncRepeatingTask(pl, new EffectRunnable(pla), 2, 2);
                                        bs.scheduleSyncDelayedTask(pl, new EffectStopRunnable(pla), 20 * 40);
                                        pl.invisibleSB.put(pla, bt);
                                        pla.sendMessage(pl.getLang("lang.itemUseMessageDontCD").replaceAll("%item", "Smoke of Deceit"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (pl.spectators.containsKey(p)) {
            if (e.getItem() != null && e.getItem().getType().equals(Material.COMPASS)) {
                if ((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    p.teleport(pl.spectators.get(p).addN().getLocation().add(0, 2, 0));
                } else if ((e.getAction() == Action.LEFT_CLICK_AIR) || (e.getAction() == Action.LEFT_CLICK_BLOCK)) {
                    p.teleport(pl.spectators.get(p).subN().getLocation().add(0, 2, 0));
                }
            }
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (pl.ingameList.containsKey(p) || pl.spectators.containsKey(p)) {
                e.setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (pl.ingameList.containsKey(p) || pl.spectators.containsKey(p)) {
            if (e.getCause() == TeleportCause.COMMAND || e.getCause() == TeleportCause.UNKNOWN) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (pl.ingameList.containsKey(p)) {
            e.setKeepLevel(true);
            pl.playerDeathItems.put(p, p.getInventory().getContents());
            pl.playerDeathArmor.put(p, p.getInventory().getArmorContents());
            for (ItemStack is : e.getDrops()) {
                if (is.getType().equals(Material.DIAMOND_SWORD)) {
                    pl.getServer().getWorld(pl.worldName).dropItemNaturally(e.getEntity().getLocation(), is);
                }
            }
            e.getDrops().clear();
        }
        e.setDeathMessage("");
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (pl.playerDeathItems.containsKey(p)) {
            for (ItemStack is : pl.playerDeathItems.get(p)) {
                if (is != null) {
                    if (!is.getType().equals(Material.DIAMOND_SWORD)) { // divine rapier will drop on death
                        p.getInventory().addItem(is);
                    }
                }
            }
            pl.playerDeathItems.remove(p);
            p.getInventory().setArmorContents(pl.playerDeathArmor.get(p)); // helmet wont work being wool
            pl.playerDeathArmor.remove(p);
            int run = pl.getServer().getScheduler().scheduleSyncRepeatingTask(pl, new RespawnRunnable(p), 2, 2);
            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new RespawnStopRunnable(run), 8);
            if (pl.ingameList.get(p).getTeam() == 1) {
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 11));
                e.setRespawnLocation(pl.blueDeploy);
            } else {
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));
                e.setRespawnLocation(pl.redDeploy);
            }
        }
    }

    private void checkIngameThings(Player p) {
        if (pl.queue.containsKey(p)) {
            pl.removePlayerFromQueue(p);
        }
        if (pl.ingameList.containsKey(p)) {
            pl.removePlayer(p);
        }
        if (pl.spectators.containsKey(p)) {
            pl.removeSpectator(p);
        }
        if (pl.playerDeathItems.containsKey(p)) {
            pl.playerDeathItems.remove(p);
            pl.playerDeathArmor.remove(p);
        }
        if (pl.teleportingT.containsKey(p)) {
            pl.teleportingT.remove(p);
            pl.teleportingE.remove(p);
        }
        if (pl.teleportingJ.contains(p)) {
            pl.teleportingJ.remove(p);
        }
    }

    private class EffectStopRunnable implements Runnable {

        private final Player p;

        public EffectStopRunnable(Player pla) {
            this.p = pla;
        }

        @Override
        public void run() {
            pl.getServer().getScheduler().cancelTask(pl.invisibleSB.get(p).getTaskId());
        }
    }

    private class EffectRunnable implements Runnable {

        private final Player p;

        public EffectRunnable(Player p) {
            this.p = p;
        }

        @Override
        public void run() {
            pl.smokeEffect(p.getLocation(), pl.getRandom(1, 9));
        }
    }

    private class RespawnStopRunnable implements Runnable {

        private final int run;

        public RespawnStopRunnable(int run) {
            this.run = run;
        }

        @Override
        public void run() {
            pl.getServer().getScheduler().cancelTask(run);
        }
    }

    private class RespawnRunnable implements Runnable {

        private final Player p;

        public RespawnRunnable(Player p) {
            this.p = p;
        }

        @Override
        public void run() {
            pl.breakEffect(p.getLocation(), 2, 18);
        }
    }
}
