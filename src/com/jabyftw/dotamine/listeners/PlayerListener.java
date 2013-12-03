package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Jogador;
import com.jabyftw.dotamine.runnables.item.ItemCDRunnable;
import com.jabyftw.dotamine.runnables.item.ShowRunnable;
import com.jabyftw.dotamine.runnables.item.SmokeDeceitEffectRunnable;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
        p.setDisplayName(ChatColor.GREEN + p.getName());
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
        if (pl.ingameList.size() < 2 && (pl.state == pl.SPAWNING || pl.state == pl.PLAYING) && pl.ingameList.containsKey(p)) { // 1 player left
            pl.broadcast(pl.getLang("lang.onePlayerLeft"));
            pl.endGame(true, 0);
        }
        
    }
    
    @EventHandler
    public void onKick(PlayerKickEvent e) {
        Player p = e.getPlayer();
        e.setLeaveMessage(pl.getLang("lang.quitMessage").replaceAll("%name", p.getName()));
        checkIngameThings(p);
        if (pl.ingameList.size() < 2 && (pl.state == pl.SPAWNING || pl.state == pl.PLAYING) && pl.ingameList.containsKey(p)) { // 1 player left
            pl.broadcast(pl.getLang("lang.onePlayerLeft"));
            pl.endGame(true, 0);
        }
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
            if (p.getLocation().distance(pl.blueDeploy) < 8) {
                if (pl.ingameList.get(p).getTeam() == 1) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 2), false);
                } else {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 10, 2), false);
                }
            } else if (p.getLocation().distance(pl.redDeploy) < 8) {
                if (pl.ingameList.get(p).getTeam() == 2) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 2), false);
                } else {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 10, 2), false);
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            if (pl.ingameList.containsKey(p)) {
                if (e.getSlotType().equals(SlotType.ARMOR)) {
                    if (e.getCurrentItem().getType().equals(Material.WOOL)) {
                        e.setCancelled(true);
                    }
                }
            } else if(pl.spectators.containsKey(p)) {
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
                    if (!pl.invisible.containsKey(p)) {
                        if (pl.shadowCD.contains(p)) {
                            p.sendMessage(pl.getLang("lang.itemCDMessage").replaceAll("%item", "Shadow Blade"));
                        } else {
                            p.sendMessage(pl.getLang("lang.itemUseMessage").replaceAll("%item", "Shadow Blade").replaceAll("%cd", "45"));
                            bs.scheduleSyncDelayedTask(pl, new ItemCDRunnable(pl, p, pl.SHADOW_BLADE), 20 * 45);
                            pl.shadowCD.add(p);
                            int show = bs.scheduleSyncDelayedTask(pl, new ShowRunnable(pl, p), 20 * 15);
                            pl.invisible.put(p, 1);
                            pl.invisibleSB.put(p, show);
                            pl.smokeEffect(p.getLocation(), 10);
                            pl.hidePlayerFromTeam(p, pl.getOtherTeam(p));
                        }
                    } else {
                        p.sendMessage(pl.getLang("lang.alreadyInvisible"));
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
                        bs.scheduleSyncDelayedTask(pl, new ForceStaffRunnable(p));
                        int run = bs.scheduleSyncRepeatingTask(pl, new ForceEffectRunnable(p), 2, 2);
                        bs.scheduleSyncDelayedTask(pl, new ForceStopRunnable(p), 16);
                        pl.forcingStaff.put(p, run);
                    }
                }
            } else if (e.getItem() != null && e.getItem().getType().equals(Material.FERMENTED_SPIDER_EYE)) { // Tarrasque
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    pl.checkForTarrasque(p);
                }
            } else if (e.getItem() != null && e.getItem().getType().equals(Material.WEB)) { // Smoke
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (p.getItemInHand().getType().equals(Material.WEB)) {
                        if (pl.smokeCD.contains(p)) {
                            p.sendMessage(pl.getLang("lang.itemCDMessage").replaceAll("%item", "Smoke of Deceit"));
                        } else {
                            p.getInventory().remove(p.getItemInHand());
                            List<Entity> list = p.getNearbyEntities(15 / 2, 13 / 2, 15 / 2);
                            list.add(p);
                            for (Entity en : list) {
                                if (en instanceof Player) {
                                    Player pla = (Player) en;
                                    if (pl.ingameList.containsKey(pla)) {
                                        if (pl.ingameList.get(pla).getTeam() == pl.ingameList.get(p).getTeam()) {
                                            if (!pl.invisible.containsKey(pla)) {
                                                pl.hidePlayerFromTeam(pla, pl.getOtherTeam(pla));
                                                int show = bs.scheduleSyncDelayedTask(pl, new ShowRunnable(pl, pla), 20 * 40);
                                                int show2 = bs.scheduleSyncRepeatingTask(pl, new SmokeDeceitEffectRunnable(pl, pla), 4, 4);
                                                pl.invisible.put(pla, 2);
                                                pl.invisibleW.put(pla, show);
                                                pl.invisibleEffectW.put(pla, show2);
                                            } else {
                                                pla.sendMessage(pl.getLang("lang.alreadyInvisible"));
                                            }
                                        }
                                    }
                                }
                            }
                            p.sendMessage(pl.getLang("lang.itemUseMessageDontCD").replaceAll("%item", "Smoke of Deceit"));
                            pl.smokeCD.add(p);
                            bs.scheduleSyncDelayedTask(pl, new ItemCDRunnable(pl, p, pl.SMOKE), 20 * 90);
                        }
                    }
                }
            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) { // SIGN
                Block block = e.getClickedBlock();
                if (block.getType().equals(Material.SIGN) || block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
                    Sign s = (Sign) block.getState();
                    if (s.getLine(0).equalsIgnoreCase("[Dota]")) {
                        if (s.getLine(1).equalsIgnoreCase("Shop")) {
                            if (!p.performCommand("bs")) {
                                p.sendMessage(ChatColor.RED + "No BossShop for you");
                            }
                        }
                    }
                }
            }
        } else if (pl.spectators.containsKey(p)) {
            if (e.getItem() != null && e.getItem().getType().equals(Material.COMPASS)) {
                if ((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    if (!pl.interactCD.contains(p)) {
                        p.teleport(pl.spectators.get(p).addN().getLocation().add(0, 2, 0));
                        pl.interactCD.add(p);
                        pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new InteractCDRunnable(p), 20 * 3);
                    } else {
                        p.sendMessage(pl.getLang("lang.dontSpamClicks"));
                    }
                    
                } else if ((e.getAction() == Action.LEFT_CLICK_AIR) || (e.getAction() == Action.LEFT_CLICK_BLOCK)) {
                    if (!pl.interactCD.contains(p)) {
                        p.teleport(pl.spectators.get(p).subN().getLocation().add(0, 2, 0));
                        pl.interactCD.add(p);
                        pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new InteractCDRunnable(p), 20 * 3);
                    } else {
                        p.sendMessage(pl.getLang("lang.dontSpamClicks"));
                    }
                }
            }
        } else {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (!pl.interactCD.contains(p)) {
                    Block block = e.getClickedBlock();
                    if (block.getType().equals(Material.SIGN) || block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
                        Sign s = (Sign) block.getState();
                        if (s.getLine(0).equalsIgnoreCase("[Dota]")) {
                            if (s.getLine(1).equalsIgnoreCase("Join")) {
                                if (s.getLine(2).startsWith("Meele")) {
                                    p.performCommand("join m");
                                } else if (s.getLine(2).startsWith("Ranged")) {
                                    p.performCommand("join r");
                                } else { // Spectator
                                    p.performCommand("spectate");
                                }
                            }
                        }
                        pl.interactCD.add(p);
                        pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new InteractCDRunnable(p), 20 * 3);
                    }
                } else {
                    p.sendMessage(pl.getLang("lang.dontSpamClicks"));
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
            int run = pl.getServer().getScheduler().scheduleSyncRepeatingTask(pl, new RespawnEffectRunnable(p, getTeamSpawn(p)), 2, 2);
            pl.respawning.put(p, run);
            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new RespawnStopRunnable(p), 5);
            if (pl.ingameList.get(p).getTeam() == 1) {
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 11));
                e.setRespawnLocation(pl.blueDeploy);
            } else {
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));
                e.setRespawnLocation(pl.redDeploy);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        String msg = e.getMessage();
        e.setCancelled(true);
        executeChat(sender, msg);
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
        if (pl.invisible.containsKey(p)) {
            if (pl.invisible.get(p) == 1) {
                pl.invisibleSB.remove(p);
            } else {
                pl.invisibleW.remove(p);
                pl.invisibleEffectW.remove(p);
            }
        }
    }
    
    private void executeChat(Player sender, String msg) {
        String message = pl.getLang("lang.chat.general").replaceAll("%name", sender.getDisplayName()).replaceAll("%message", msg);
        if (pl.spectators.containsKey(sender)) {
            String specChat = pl.getLang("lang.chat.spectating").replaceAll("%name", sender.getDisplayName()).replaceAll("%message", msg).replaceAll("%general", message);
            for (Player p : sender.getLocation().getWorld().getPlayers()) {
                if (!pl.ingameList.containsKey(p)) {
                    p.sendMessage(specChat);
                }
            }
            pl.getLogger().log(Level.INFO, specChat);
        } else if (pl.ingameList.containsKey(sender)) {
            int team = pl.ingameList.get(sender).getTeam();
            String ingameChat = pl.getLang("lang.chat.ingame").replaceAll("%name", sender.getDisplayName()).replaceAll("%message", msg).replaceAll("%general", message).replaceAll("%team", getTeamName(pl.ingameList.get(sender).getTeam()));
            for (Jogador j : pl.ingameList.values()) {
                if (j.getTeam() == team) {
                    j.getPlayer().sendMessage(ingameChat);
                }
            }
            for (Player p : pl.spectators.keySet()) {
                p.sendMessage(ingameChat);
            }
            pl.getLogger().log(Level.INFO, ingameChat);
        } else {
            for (Player p : sender.getLocation().getWorld().getPlayers()) {
                if (!pl.ingameList.containsKey(p)) {
                    p.getPlayer().sendMessage(message);
                }
            }
            pl.getLogger().log(Level.INFO, message);
        }
    }
    
    private String getTeamName(int team) {
        if (team == 1) {
            return pl.getLang("lang.chat.blueTeam");
        } else {
            return pl.getLang("lang.chat.redTeam");
        }
    }
    
    private Location getTeamSpawn(Player p) {
        if (pl.ingameList.get(p).getTeam() == 1) {
            Location l = pl.blueDeploy;
            return l.subtract(0, 1, 0);
        } else {
            Location l = pl.redDeploy; // create a copy of location, not sure if subtract will return a new one
            return l.subtract(0, 1, 0);
        }
    }
    
    private class InteractCDRunnable implements Runnable {
        
        private final Player p;
        
        public InteractCDRunnable(Player p) {
            this.p = p;
        }
        
        @Override
        public void run() {
            pl.interactCD.remove(p);
        }
    }
    
    private class ForceStaffRunnable extends BukkitRunnable {
        
        private final Player p;
        
        public ForceStaffRunnable(Player p) {
            this.p = p;
        }
        
        @Override
        public void run() {
            Vector vec = p.getLocation().getDirection();
            vec.setY(0);
            vec.multiply(2.5 / vec.length());
            vec.setY(0.3);
            p.setVelocity(vec);
        }
    }
    
    private class ForceEffectRunnable extends BukkitRunnable {
        
        private final Player p;
        
        public ForceEffectRunnable(Player p) {
            this.p = p;
        }
        
        @Override
        public void run() {
            pl.smokeEffect(p.getLocation(), 10);
        }
    }
    
    private class ForceStopRunnable extends BukkitRunnable {
        
        private final Player p;
        
        public ForceStopRunnable(Player p) {
            this.p = p;
        }
        
        @Override
        public void run() {
            pl.getServer().getScheduler().cancelTask(pl.forcingStaff.get(p));
            pl.forcingStaff.remove(p);
        }
    }
    
    private class RespawnEffectRunnable implements Runnable {
        
        private final Player p;
        private final Location respawnLoc;
        
        public RespawnEffectRunnable(Player p, Location respawnLoc) {
            this.p = p;
            this.respawnLoc = respawnLoc;
        }
        
        @Override
        public void run() {
            pl.breakEffect(respawnLoc, 2, 18);
            
        }
    }
    
    private class RespawnStopRunnable implements Runnable {
        
        private final Player p;
        
        public RespawnStopRunnable(Player p) {
            this.p = p;
        }
        
        @Override
        public void run() {
            pl.getServer().getScheduler().cancelTask(pl.respawning.get(p));
            pl.respawning.remove(p);
        }
    }
}
