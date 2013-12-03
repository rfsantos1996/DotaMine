package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Structure;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
        if (pl.ingameList.containsKey(p) || pl.spectators.containsKey(p)) {
            if (e.getPlayer().getItemInHand() == null) {
                if (pl.state == pl.PLAYING) {
                    for (Structure s : pl.structures) {
                        Block b = e.getBlock();
                        if (b.getLocation().distance(s.getLoc()) < 5) {
                            if (b.getType() == Material.WOOL) {
                                if (s.getTeam() != pl.ingameList.get(p).getTeam()) {
                                    s.punchTower(false);
                                } else {
                                    if (s.getHP() < 50) {
                                        s.punchTower(true);
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

    @EventHandler
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
}
