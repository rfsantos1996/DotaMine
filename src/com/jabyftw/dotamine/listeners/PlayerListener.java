package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.runnables.StopRunnable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

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
        pl.cleanPlayer(p, false, false);
        p.teleport(pl.normalSpawn);
        if (p.hasPermission("dotamine.play")) {
            if (pl.state == 0) {
                p.sendMessage(pl.getLang("lang.youCanPlay"));
            } else if (pl.state == 1) {
                p.sendMessage(pl.getLang("lang.youCanJoin"));
            } else if (p.hasPermission("dotamine.spectate")) {
                p.sendMessage(pl.getLang("lang.youCanSpectate"));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (pl.ingameList.containsKey(p)) {
            pl.removePlayer(p, pl.ingameList.get(p).getTeam());
        }
        if (pl.spectators.contains(p)) {
            pl.removeSpectator(p);
        }
        if (pl.playerDeathItems.containsKey(p)) {
            pl.playerDeathItems.remove(p);
            pl.playerDeathArmor.remove(p);
        }
        if (pl.ingameList.size() < 2 && pl.gameStarted) { // 1 player left
            pl.broadcast(pl.getLang("lang.onePlayerLeft"));
            pl.state = 3;
            pl.getServer().setWhitelist(true);
            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StopRunnable(pl), 20 * 10);
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        Player p = e.getPlayer();
        if (pl.ingameList.containsKey(p)) {
            pl.removePlayer(p, pl.ingameList.get(p).getTeam());
        }
        if (pl.spectators.contains(p)) {
            pl.removeSpectator(p);
        }
        if (pl.playerDeathItems.containsKey(p)) {
            pl.playerDeathItems.remove(p);
            pl.playerDeathArmor.remove(p);
        }
    }

    @EventHandler
    public void onPickUp(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        if (pl.spectators.contains(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventory(InventoryMoveItemEvent e) { // maybe will work..
        if (e.getItem().getType().equals(Material.WOOL)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        /*if (pl.ingameList.containsKey(p)) {
         // TODO: shadow blade
         } else */
        if (pl.spectators.contains(p)) {
            if (e.getItem().getType().equals(new ItemStack(Material.COMPASS).getType())) {
                if ((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    for (Player tp : pl.ingameList.keySet()) {
                        p.teleport(tp.getLocation(), TeleportCause.COMMAND);
                        return;
                    }
                    //TODO: make it better.. probably will tp to ONE only player
                }
            }
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (pl.ingameList.containsKey(p) || pl.spectators.contains(p)) {
                e.setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (pl.ingameList.containsKey(p) || pl.spectators.contains(p)) {
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
                    p.getInventory().addItem(is);
                }
            }
            pl.playerDeathItems.remove(p);
            p.getInventory().setArmorContents(pl.playerDeathArmor.get(p)); // helmet wont work being wool
            pl.playerDeathArmor.remove(p);
            if (pl.ingameList.get(p).getTeam() == 1) {
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 11));
                e.setRespawnLocation(pl.blueDeploy);
            } else {
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));
                e.setRespawnLocation(pl.redDeploy);
            }
        }
    }
}
