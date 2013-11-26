package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
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
        if (e.getPlayer() == null) {
            e.setCancelled(true);
        } else if (!e.getBlock().getType().equals(Material.TNT)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (pl.gameStarted && e.getLocation().getWorld().equals(pl.getServer().getWorld(pl.worldName))) {
            Location expl = e.getLocation();
            e.setCancelled(true);
            for (Tower t : pl.towers.values()) {
                if (expl.distance(t.getLoc()) < 5) {
                    if (alreadyBrokenPast(t.getLoc())) {
                        if (t.getName().equalsIgnoreCase("Blue Ancient")) {
                            pl.broadcast(pl.getLang("lang.redTeamWon"));
                            pl.state = 3;
                            t.setDestroyed(true);
                            /*for(Jogador j : pl.ingameList.values()) {
                             if(j.getTeam() == 1) {
                             j.addLose(j.getPlayer().getName());
                             } else {
                             j.addWin(j.getPlayer().getName());
                             }
                             }*/
                            pl.getServer().setWhitelist(true);
                            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StopRunnable(pl), 20 * 15);
                        } else if (t.getName().equalsIgnoreCase("Red Ancient")) {
                            pl.broadcast(pl.getLang("lang.blueTeamWon"));
                            pl.state = 3;
                            t.setDestroyed(true);
                            //TODO: MYSQL and Scoreboard support
                            /*for(Jogador j : pl.ingameList.values()) {
                             if(j.getTeam() == 1) {
                             j.addWin(j.getPlayer().getName());
                             } else {
                             j.addLose(j.getPlayer().getName());
                             }
                             }*/
                            pl.getServer().setWhitelist(true);
                            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StopRunnable(pl), 20 * 15);
                        } else {
                            pl.broadcast(pl.getLang("lang.towerDestroyed").replaceAll("%tower", t.getName()));
                            t.setDestroyed(true);
                        }
                    } else {
                        t.setDestroyed(false);
                    }
                }
            }
        }
    }

    private boolean alreadyBrokenPast(Location loc) {
        if (loc.equals(pl.blueAncient)) {
            if (pl.towers.get(pl.blueSMidT).isDestroyed()) {
                return pl.towers.get(pl.blueSTopT).isDestroyed() || pl.towers.get(pl.blueSBotT).isDestroyed();
            } else {
                return false;
            }
        } else if (loc.equals(pl.blueSBotT)) {
            if (pl.towers.get(pl.blueFBotT).isDestroyed()) {
                pl.botCreepSpawn.add(pl.botSpawnPosB);
                return true;
            } else {
                return false;
            }
        } else if (loc.equals(pl.blueSTopT)) {
            if (pl.towers.get(pl.blueFTopT).isDestroyed()) {
                pl.botCreepSpawn.add(pl.topSpawnPosB);
                return true;
            } else {
                return false;
            }
        } else if (loc.equals(pl.blueSMidT)) {
            if (pl.towers.get(pl.blueFMidT).isDestroyed()) {
                pl.botCreepSpawn.add(pl.midSpawnPosB);
                return true;
            } else {
                return false;
            }
        } else if (loc.equals(pl.redAncient)) {
            if (pl.towers.get(pl.redSMidT).isDestroyed()) {
                return pl.towers.get(pl.redSTopT).isDestroyed() || pl.towers.get(pl.redSBotT).isDestroyed();
            } else {
                return false;
            }
        } else if (loc.equals(pl.redSBotT)) {
            if (pl.towers.get(pl.redFBotT).isDestroyed()) {
                pl.botCreepSpawn.add(pl.botSpawnPosR);
                return true;
            } else {
                return false;
            }
        } else if (loc.equals(pl.redSTopT)) {
            if (pl.towers.get(pl.redFTopT).isDestroyed()) {
                pl.botCreepSpawn.add(pl.topSpawnPosR);
                return true;
            } else {
                return false;
            }
        } else if (loc.equals(pl.redSMidT)) {
            if (pl.towers.get(pl.redFMidT).isDestroyed()) {
                pl.botCreepSpawn.add(pl.midSpawnPosR);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
