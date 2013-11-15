package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
            pl.removePlayer(p);
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
        if (pl.ingameList.containsKey(p) || pl.spectators.contains(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (pl.ingameList.containsKey(p)) {
            // TODO: shadow blade
        } else if (pl.spectators.contains(p)) {
            if (e.getItem().getType().equals(new ItemStack(Material.COMPASS).getType())) {
                if ((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    //TODO: compass teleport
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
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (pl.playerDeathItems.containsKey(p)) {
            int i = 0;
            for (ItemStack is : pl.playerDeathItems.get(p)) {
                p.getInventory().setItem(i, is);
            }
            p.getInventory().setArmorContents(pl.playerDeathArmor.get(p));
            pl.playerDeathItems.remove(p);
            pl.playerDeathArmor.remove(p);
        }
    }
}
