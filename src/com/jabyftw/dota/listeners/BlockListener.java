package com.jabyftw.dota.listeners;

import com.jabyftw.dota.DotaMine;
import com.jabyftw.dota.Tower;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

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

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if ((pl.gameStarted) && (e.getLocation().getWorld().getName().equals(pl.worldName))) {
            Location exLoc = e.getLocation();
            for (Tower tower : pl.towers.values()) {
                if (tower.getLoc().distance(exLoc) < 10) {
                    if (!tower.isDestroyed()) {
                        if (alreadyBrokenBack(tower)) {
                            e.setCancelled(true);
                            pl.broadcastMsg(tower.getName() + " foi destruido.");
                            tower.setDestroyed(true);
                            if (tower.getName().equalsIgnoreCase("Anciente Azul")) {
                                pl.broadcastMsg(ChatColor.RED + "Time vermelho ganhou!");
                                pl.gameStarted = false;
                            } else if (tower.getName().equalsIgnoreCase("Anciente Vermelho")) {
                                pl.broadcastMsg(ChatColor.BLUE + "Time azul ganhou!");
                                pl.gameStarted = false;
                            }
                        } else {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    private boolean alreadyBrokenBack(Tower t) {
        if (t.getLoc().equals(pl.BlueAncient)) {
            return pl.towers.get(pl.BlueBot1).isDestroyed() || pl.towers.get(pl.BlueTop1).isDestroyed();
        } else if (t.getLoc().equals(pl.BlueBot1)) {
            return pl.towers.get(pl.BlueBot2).isDestroyed();
        } else if (t.getLoc().equals(pl.BlueTop1)) {
            return pl.towers.get(pl.BlueTop2).isDestroyed();

        } else if (t.getLoc().equals(pl.RedAncient)) {
            return pl.towers.get(pl.RedBot1).isDestroyed() || pl.towers.get(pl.RedTop1).isDestroyed();
        } else if (t.getLoc().equals(pl.RedBot1)) {
            return pl.towers.get(pl.RedBot2).isDestroyed();
        } else if (t.getLoc().equals(pl.RedTop1)) {
            return pl.towers.get(pl.RedTop2).isDestroyed();
        } else {
            return false;
        }
    }
}
