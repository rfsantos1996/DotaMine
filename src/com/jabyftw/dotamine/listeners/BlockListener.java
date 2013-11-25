package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Jogador;
import com.jabyftw.dotamine.Tower;
import com.jabyftw.dotamine.runnables.StopRunnable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

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
        if (pl.ingameList.containsKey(p) || pl.spectators.contains(p)) {
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
        if (pl.ingameList.containsKey(p) || pl.spectators.contains(p)) {
            e.setCancelled(true);
            return;
        }
        if (!p.isOp()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onIgnite(BlockIgniteEvent e) {
        if(e.getPlayer() == null) {
            e.setCancelled(true);
        } else if(!e.getBlock().getType().equals(Material.TNT)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (pl.gameStarted && e.getLocation().getWorld().equals(pl.getServer().getWorld(pl.worldName))) {
            Location expl = e.getLocation();
            e.setCancelled(true);
            for (Tower t : pl.towers.keySet()) {
                if (expl.distance(t.getLoc()) < 5) {
                    if (alreadyBrokenPast(t.getLoc()) && !t.isDestroyed()) {
                        t.setDestroyed(true);
                        pl.broadcast(pl.getLang("lang.towerDestroyed").replaceAll("%tower", t.getName()));
                        if (t.getName().equalsIgnoreCase("Blue Ancient")) {
                            pl.broadcast(pl.getLang("lang.redTeamWon"));
                            pl.state = 3;
                            /*for(Jogador j : pl.ingameList.values()) {
                                if(j.getTeam() == 1) {
                                    j.addLose();
                                } else {
                                    j.addWin();
                                }
                            }*/
                            pl.getServer().setWhitelist(true);
                            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StopRunnable(pl), 20 * 15);
                        } else if (t.getName().equalsIgnoreCase("Red Ancient")) {
                            pl.broadcast(pl.getLang("lang.blueTeamWon"));
                            pl.state = 3;
                            //TODO: MYSQL and Scoreboard support
                            /*for(Jogador j : pl.ingameList.values()) {
                                if(j.getTeam() == 1) {
                                    j.addWin();
                                } else {
                                    j.addLose();
                                }
                            }*/
                            pl.getServer().setWhitelist(true);
                            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StopRunnable(pl), 20 * 15);
                        }
                    }
                }
            }
        }
    }

    private boolean alreadyBrokenPast(Location loc) { // TODO: maybe isnt needed
        return true;
        /*if (t.getLoc().equals(pl.BlueAncient)) {
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
         }*/
    }
}
