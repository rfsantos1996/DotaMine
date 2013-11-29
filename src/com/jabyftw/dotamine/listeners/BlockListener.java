package com.jabyftw.dotamine.listeners;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Jogador;
import com.jabyftw.dotamine.Tower;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
        if (pl.ingameList.containsKey(p) || pl.spectators.containsKey(p)) {
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

    /*@EventHandler
    public void onIgnite(BlockIgniteEvent e) {
        if (e.getPlayer() == null) {
            e.setCancelled(true);
        } else if (!e.getBlock().getType().equals(Material.TNT)) {
            e.setCancelled(true);
        }
    }*/

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        e.setCancelled(true);
        if (pl.state == pl.PLAYING && e.getLocation().getWorld().equals(pl.getServer().getWorld(pl.worldName))) {
            Location expl = e.getLocation();
            for (Tower t : pl.towers.values()) {
                if (expl.distance(t.getLoc()) < 10) {
                    if (alreadyBrokenPast(t.getLoc())) {
                        if (t.getName().equalsIgnoreCase("Blue Ancient")) {
                            pl.broadcast(pl.getLang("lang.redTeamWon"));
                            t.setDestroyed(true);
                            pl.endGame(false, 2);
                        } else if (t.getName().equalsIgnoreCase("Red Ancient")) {
                            pl.broadcast(pl.getLang("lang.blueTeamWon"));
                            t.setDestroyed(true);
                            pl.endGame(false, 1);
                        } else {
                            pl.broadcast(pl.getLang("lang.towerDestroyed").replaceAll("%tower", t.getName()));
                            t.setDestroyed(true);
                            checkForMegacreeps();
                            addTowerMoney(pl.getOtherTeam(t.getTeam()));
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
            return true;
        }
    }

    private void addTowerMoney(int team) {
        for (Jogador j : pl.ingameList.values()) {
            if (j.getTeam() == team) {
                j.addTowerKillMoney();
            }
        }
    }

    private void checkForMegacreeps() {
        if (pl.towers.get(pl.blueFBotT).isDestroyed() && pl.towers.get(pl.blueFMidT).isDestroyed() && pl.towers.get(pl.blueFTopT).isDestroyed()) {
            pl.megaCreeps = true;
        } else if (pl.towers.get(pl.redFBotT).isDestroyed() && pl.towers.get(pl.redFMidT).isDestroyed() && pl.towers.get(pl.redFTopT).isDestroyed()) {
            pl.megaCreeps = true;
        }
    }
}
