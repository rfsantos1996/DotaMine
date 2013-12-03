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
        if (pl.spectators.containsKey(p)) {
            e.setCancelled(true);
            return;
        }
        if (pl.ingameList.containsKey(p)) {
            if (p.getItemInHand().getType().equals(Material.AIR)) {
                if (pl.state == pl.PLAYING) {
                    for (Structure s : pl.structures.keySet()) {
                        Block b = e.getBlock();
                        if (b.getLocation().distance(s.getLoc()) < 5) {
                            pl.debug("block break 1");
                            if (b.getType() == Material.WOOL) {
                                pl.debug("block break 2");
                                if (s.getTeam() != pl.ingameList.get(p).getTeam()) {
                                    if (towerBreakable(s)) {
                                        s.punchTower(false);
                                        if (s.getHP() > 0) {
                                            p.sendMessage(pl.getLang("lang.youDamagedTower").replaceAll("%tower", s.getName()).replaceAll("%hp", Integer.toString(s.getHP())));
                                        }
                                        pl.debug("punched tower : " + s.getName());
                                    } else {
                                        p.sendMessage(pl.getLang("lang.youMustDestroyFirstTowers"));
                                    }
                                } else {
                                    if (s.getHP() < 50) {
                                        s.punchTower(true);
                                        pl.debug("denied tower");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            e.setCancelled(true);
            return;
        }
        if (!p.isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (pl.ingameList.containsKey(p) || pl.spectators.containsKey(p)) {
            e.setCancelled(true);
            return;
        }
        if (!p.isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnnite(BlockIgniteEvent e) {
        if (e.getCause() == IgniteCause.SPREAD) {
            e.setCancelled(true);
        }
    }

    private boolean towerBreakable(Structure s) { // mid 2
        int sNumber = pl.structures.get(s);
        if (sNumber == pl.minN) { // first tower
            pl.debug(Integer.toString(sNumber));
            return true;
        }
        pl.debug(Integer.toString(sNumber));
        for (Structure s1 : pl.structures.keySet()) {
            if (s.getLane().equalsIgnoreCase(s1.getLane())) {
                pl.debug("equal lane");
                if (s1.isDestroyed() && pl.structures.get(s1) < sNumber) { // if 1 < 2 and tower is destroyed
                    int deltaN = pl.structures.get(s1) - sNumber;
                    if(deltaN == 1) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
