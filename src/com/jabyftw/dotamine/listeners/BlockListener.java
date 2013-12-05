package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Structure;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author Rafael
 */
public class BlockListener implements Listener {

    private final DotaMine pl;

    public BlockListener(DotaMine pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (e.getBlock().getLocation().distanceSquared(pl.normalSpawn) < (15*15)) {
            e.setCancelled(true);
            return;
        }
        if (pl.spectators.containsKey(p)) {
            e.setCancelled(true);
            return;
        }
        if (pl.ingameList.containsKey(p)) {
            if (p.getItemInHand().getType().equals(Material.AIR)) {
                if (pl.state == pl.PLAYING) {
                    for (Structure s : pl.structures.keySet()) {
                        Block b = e.getBlock();
                        if (b.getLocation().distanceSquared(s.getLoc()) < (5*5)) {
                            pl.debug("block break 1");
                            if (b.getType() == Material.WOOL) {
                                pl.debug("block break 2");
                                if (s.getTeam() != pl.ingameList.get(p).getTeam()) {
                                    if (towerBreakable(s)) {
                                        s.punchTower(p, false);
                                        pl.debug("punched tower : " + s.getName());
                                    } else {
                                        p.sendMessage(pl.getLang("lang.youMustDestroyFirstTowers"));
                                    }
                                } else {
                                    if (s.getHP() < 50) {
                                        s.punchTower(p, true);
                                        pl.debug("denied tower");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (pl.ingameList.containsKey(p) || pl.spectators.containsKey(p)) {
            e.setCancelled(true);
        } else if (p.getLocation().getWorld().getName().equalsIgnoreCase(pl.worldName)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        if (e.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(pl.worldName)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnnite(BlockIgniteEvent e) {
        if (e.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(pl.worldName)) {
            if (e.getCause() == IgniteCause.SPREAD) {
                e.setCancelled(true);
            }
        }
    }

    private boolean towerBreakable(Structure s) {
        int sNumber = pl.structures.get(s); // 2
        pl.debug(Integer.toString(sNumber));
        if (sNumber == pl.minN) { // first tower, allow
            return true;
        }
        for (Structure s1 : pl.structures.keySet()) {
            if (pl.structures.get(s1) == (sNumber - 1)) { // 1 = (2-1)
                if (s1.getLane().equalsIgnoreCase(s.getLane()) && s1.getTeam() == s.getTeam()) { // same lane and team
                    return s1.isDestroyed();
                }
            }
        }
        return false;
    }
}
