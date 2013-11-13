package com.jabyftw.dota.listeners;

import com.jabyftw.dota.DotaMine;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author Rafael
 */
public class BlockListener implements Listener {

    private final DotaMine pl;

    public BlockListener(DotaMine plugin) {
        this.pl = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("dotamine.breakblock") || p.isOp()) {
            if (pl.gameStarted) {
                p.sendMessage("You can't destroy blocks while playing");
                e.setCancelled(true);
            }
        } else {
            e.setCancelled(true);
            p.sendMessage("You can't destroy blocks.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || p.hasPermission("dotamine.placeblock")) {
            if (pl.gameStarted) {
                p.sendMessage("You can't place blocks while playing");
                e.setCancelled(true);
            }
        } else {
            e.setCancelled(true);
            p.sendMessage("You can't place blocks.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(BlockDamageEvent e) {
        Player p = e.getPlayer();
        if (pl.gameStarted) {
            e.setCancelled(true);
        } else if (!p.hasPermission("dotamine.breakblock")) {
            e.setCancelled(true);
            p.sendMessage("You can't destroy blocks.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDispenseEvent(BlockDispenseEvent e) {
        Location loc = e.getBlock().getLocation();
        pl.getLogger().log(Level.OFF, "Dispenser on: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        e.setCancelled(true);
    }
}
