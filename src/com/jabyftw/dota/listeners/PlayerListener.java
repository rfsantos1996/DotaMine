/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jabyftw.dota.listeners;

import com.jabyftw.dota.DotaMine;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        pl.clearPlayer(p, true);
        if (p.hasPermission("dotamine.spectate")) {
            if (pl.gameStarted) {
                pl.addSpectator(p);
            } else {
                if (p.hasPermission("dotamine.play")) {
                    p.sendMessage(ChatColor.RED + "You can join the game using " + ChatColor.GOLD + "/play");
                } else {
                    p.sendMessage(ChatColor.DARK_RED + "No games to watch. " + ChatColor.RED + "Use " + ChatColor.GOLD + "/spectate" + ChatColor.GOLD + " when possible.");
                }
            }
        } else if (p.hasPermission("dotamine.play")) {
            p.sendMessage(ChatColor.RED + "You can join the game using " + ChatColor.GOLD + "/play");
        } else {
            p.teleport(pl.getServer().getWorld(pl.worldName).getSpawnLocation());
            p.sendMessage(ChatColor.DARK_RED + "You cant spectate or play.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (pl.players.containsKey(p)) {
            pl.removePlayer(p, true);
            pl.clearPlayer(p, false);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (pl.players.containsKey(e.getPlayer())) {
            if (pl.players.get(e.getPlayer()).isFixed()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        p.setFoodLevel(20);
        if (pl.playerDeathItems.containsKey(p)) {
            int i = 0;
            for (ItemStack is : pl.playerDeathItems.get(p)) {
                p.getInventory().setItem(i, is);
                i++;
            }
            p.getInventory().setArmorContents(pl.playerDeathArmor.get(p));
            pl.playerDeathItems.remove(p);
            pl.playerDeathArmor.remove(p);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (pl.players.containsKey(p)) {
            e.setKeepLevel(true);
            pl.playerDeathItems.put(p, p.getInventory().getContents());
            pl.playerDeathArmor.put(p, p.getInventory().getArmorContents());
            e.getDrops().clear();
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (pl.spectators.contains(p)) {
            if ((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                if (e.getItem().getType().equals(new ItemStack(Material.COMPASS).getType())) {
                    //TODO: compass teleport to another player
                }
            }
        } else if (pl.players.containsKey(p)) {
            e.setCancelled(true); //TODO: shadow blade
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (pl.players.containsKey(p)) {
            e.setCancelled(true);
        }
    }
}
