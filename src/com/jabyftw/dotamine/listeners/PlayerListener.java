package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.runnables.item.ItemCDRunnable;
import com.jabyftw.dotamine.runnables.item.ShowRunnable;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

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
            pl.endGame(true, 0);
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
                        int run = bs.scheduleSyncDelayedTask(pl, new ShowRunnable(pl, p, 1), 20 * 15);
                        pl.invisibleSB.put(p, run);
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
                        bs.scheduleSyncRepeatingTask(pl, new ForceStaffRunnable(p), 1, 1);
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
                                        int run = bs.scheduleSyncDelayedTask(pl, new ShowRunnable(pl, pla, 2), 20 * 40);
                                        bs.scheduleSyncRepeatingTask(pl, new SmokeOfDeceitRunnable(pla), 2, 2);
                                        pl.invisibleW.put(pla, run);
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
            pl.respawning.put(p, run);
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
        if (pl.teleporting.containsKey(p)) {
            pl.teleporting.remove(p);
        }
    }

    private class ForceStaffRunnable extends BukkitRunnable {

        private final Player p;
        private int i = 1;

        public ForceStaffRunnable(Player p) {
            this.p = p;
        }

        @Override
        public void run() {
            if (i == 1) {
                Vector vec = p.getLocation().getDirection();
                vec.setY(0);
                vec.multiply(2.5 / vec.length());
                vec.setY(0.3);
                p.setVelocity(vec);
            }
            i++;
            pl.smokeEffect(p.getLocation(), 10);
            if (i > 15) {
                pl.getServer().getScheduler().cancelTask(pl.forcingStaff.get(p));
                pl.forcingStaff.remove(p);
            }
        }
    }

    private class SmokeOfDeceitRunnable implements Runnable {

        private final Player p;
        private int i = 0;

        public SmokeOfDeceitRunnable(Player p) {
            this.p = p;
        }

        @Override
        public void run() {
            pl.smokeEffect(p.getLocation(), pl.getRandom(1, 9));
            i++;
            if (i > (20 * 40) * 2) {
                pl.getServer().getScheduler().cancelTask(pl.invisibleW.get(p));
                pl.invisibleW.remove(p);
            }
        }
    }

    private class RespawnRunnable implements Runnable {

        private final Player p;
        private int i = 0;

        public RespawnRunnable(Player p) {
            this.p = p;
        }

        @Override
        public void run() {
            pl.breakEffect(p.getLocation(), 2, 18);
            i++;
            if (i > 7) {
                pl.getServer().getScheduler().cancelTask(pl.respawning.get(p));
                pl.respawning.remove(p);
            }
        }
    }
}
