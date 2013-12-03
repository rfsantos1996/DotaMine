package com.jabyftw.dotamine;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 *
 * @author Rafael
 */
public class Structure {

    private final DotaMine pl;
    private final Location loc, tpLoc, afterDestroy;
    private final String name, lane;
    private final int team, type;
    private boolean announced = false;
    private List<Block> blocks = new ArrayList();
    private int hp = 1300; // every wool break = 1.5 seconds, 4.5 minutes of breaking = 180 punchs, punch = 10 hp remove

    // loc, name, lane, number, team, type
    public Structure(DotaMine pl, Location loc, Location tpLoc, String name, String lane, String team, int type, Location afterDestroy) {
        this.pl = pl;
        this.loc = loc;
        this.tpLoc = tpLoc;
        this.name = name;
        this.lane = getFromStringLane(lane);
        this.team = getFromStringTeam(team);
        this.type = type;
        this.afterDestroy = afterDestroy;
        getNearbyBlocks(5);
        pl.debug("name: lane: number: team: type: " + getName() + "." + pl.structures.get(this));
    }

    public Location getLoc() {
        return loc;
    }

    public String getName() {
        if (team == 1) {
            return ChatColor.AQUA + name;
        } else {
            return ChatColor.RED + name;
        }
    }

    public String getLane() {
        return lane;
    }

    public int getTeam() {
        return team;
    }

    public int getType() {
        return type;
    }

    public void setHP(int multi) {
        for (int i = 0; i < multi; i++) {
            hp = (int) (hp * 0.75);
        }
    }

    public int getHP() {
        return hp;
    }

    public void punchTower(boolean denied) {
        hp = hp - 15;
        if (!announced) {
            for (Jogador j : pl.ingameList.values()) {
                if (j.getTeam() == team) {
                    j.getPlayer().sendMessage(pl.getLang("lang.towerUnderAttack").replaceAll("%tower", getName()).replaceAll("%hp", Integer.toString(getHP())));
                }
            }
            announced = true;
            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new AnnounceCDRunnable(), 20 * 30);
        }
        if (hp <= 0) {
            setDestroyed();
            if (!denied) {
                addTowerMoney(pl.getOtherTeam(getTeam()));
            }
        }
    }

    public boolean isDestroyed() {
        for (Block b : blocks) {
            if (b.getType().equals(Material.AIR)) {
                return true;
            }
        }
        return false;
    }

    public void setDestroyed() {
        if (getType() == 2) {
            if (team == 1) {
                pl.broadcast(pl.getLang("lang.redTeamWon"));
                pl.endGame(false, 2);
            } else {
                pl.broadcast(pl.getLang("lang.blueTeamWon"));
                pl.endGame(false, 1);
            }
        } else {
            pl.broadcast(pl.getLang("lang.towerDestroyed").replaceAll("%tower", getName()));
            pl.checkForMegacreeps();
            if (afterDestroy != null) {
                // TODO: add creep spawn location
            }
            for (Block b : blocks) {
                b.setType(Material.AIR);
            }
        }
    }

    private int getFromStringTeam(String team) {
        if (team.startsWith("b")) {
            return 1;
        } else {
            return 2;
        }
    }

    private String getFromStringLane(String lane) {
        if (lane.startsWith("bo")) {
            return "bot";
        } else if (lane.startsWith("m")) {
            return "mid";
        } else if (lane.startsWith("t")) {
            return "top";
        } else {
            return "base";
        }
    }

    private void addTowerMoney(int team) {
        for (Jogador j : pl.ingameList.values()) {
            if (j.getTeam() == team) {
                j.addTowerKillMoney();
            }
        }
    }

    private void getNearbyBlocks(int radius) {
        for (int x = -(radius); x <= radius; x++) {
            for (int y = -(radius); y <= radius; y++) {
                for (int z = -(radius); z <= radius; z++) {
                    Block b = loc.getBlock().getRelative(x, y, z);
                    if (b.getType() == Material.WOOL) {
                        blocks.add(b);
                    }
                }
            }
        }
    }

    public Location getTpLoc() {
        return tpLoc;
    }

    private class AnnounceCDRunnable implements Runnable {

        @Override
        public void run() {
            announced = false;
        }
    }
}
