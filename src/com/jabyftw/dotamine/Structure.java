package com.jabyftw.dotamine;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
    private int hp, runnable; // every wool break = 1.5 seconds, 4.5 minutes of breaking = 180 punchs, punch = 10 hp remove
    private Entity nearby = null;

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
        hp = pl.AncientHP;
        getNearbyBlocks(5);
        if (type == 1) {
            runnable = pl.getServer().getScheduler().scheduleSyncRepeatingTask(pl, new TowerAttackRunnable(this, (pl.TowerRange * pl.TowerRange)), 20 * 60, 30);
        }
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

    public void punchTower(Player p, boolean denied) {
        int dmg = pl.getRandom(8, 18);
        hp = hp - dmg;
        if (hp > 0) {
            p.sendMessage(pl.getLang("lang.youDamagedTower").replaceAll("%tower", getName()).replaceAll("%hp", Integer.toString(getHP())).replaceAll("%dmg", Integer.toString(dmg)));
        }
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
            setDestroyed(p, denied);
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

    public void setDestroyed(Player p, boolean denied) {
        if (getType() == 2) {
            if (team == 1) {
                pl.broadcast(pl.getLang("lang.redTeamWon"));
                pl.endGame(false, 2);
            } else {
                pl.broadcast(pl.getLang("lang.blueTeamWon"));
                pl.endGame(false, 1);
            }
        } else {
            if (denied) {
                pl.broadcast(pl.getLang("lang.towerDenied").replaceAll("%tower", name).replaceAll("%destroyer", p.getDisplayName()));
            } else {
                pl.broadcast(pl.getLang("lang.towerDestroyed").replaceAll("%tower", name).replaceAll("%destroyer", p.getDisplayName()));
            }
            pl.checkForMegacreeps();
            if (afterDestroy != null) {
                pl.addCreepLocSpawn(getLane(), afterDestroy);
            }
            for (Block b : blocks) {
                b.setType(Material.AIR);
                pl.smokeEffect(b.getLocation(), 2);
            }
            if (type == 1) {
                pl.getServer().getScheduler().cancelTask(runnable);
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

    private class TowerAttackRunnable implements Runnable {

        private final int radius;
        private final Structure t;
        private int damage = 2;

        public TowerAttackRunnable(Structure t, int radius) {
            this.t = t;
            this.radius = radius;
        }

        @Override
        public void run() {
            if (nearby != null && (nearby.getLocation().distanceSquared(loc) > radius || nearby.isDead())) {
                nearby = null;
                pl.debug("!= null and distanced -> null");
            }
            if (pl.structures.get(t) == pl.minN) { // Just first tower will attack creeps
                if (!pl.useControllableMobs) {
                    for (Entity e : pl.spawnedMobs) {
                        if (e.getLocation().distanceSquared(loc) < radius && !e.isDead()) {
                            nearby = e;
                            damage = 13;
                            pl.debug("!controllable mobs - entity found");
                            break;
                        }
                    }
                } else {
                    for (Entity e : pl.controlMobs.keySet()) {
                        if (e.getLocation().distanceSquared(loc) < radius && !e.isDead()) {
                            nearby = e;
                            damage = 13;
                            pl.debug("controllable mobs - entity found");
                            break;
                        }
                    }
                }
            }
            if (nearby == null) { // Still no mobs found
                for (Jogador j : pl.ingameList.values()) {
                    if (j.getTeam() != getTeam()) {
                        if (j.getPlayer().getLocation().distanceSquared(loc) < radius && !j.getPlayer().isDead()) {
                            nearby = (Entity) j.getPlayer();
                            damage = 2;
                            pl.debug("player found");
                            break;
                        }
                    }
                }
            }
            if (nearby != null && !nearby.isDead()) {
                if (nearby instanceof Damageable) {
                    Damageable dmgable = (Damageable) nearby;
                    dmgable.damage(damage);
                    for (Block b : blocks) {
                        pl.breakEffect(b.getLocation(), 2, 152);
                    }
                    pl.debug("damaged");
                }
            } else {
                pl.debug("no entities found or they are dead.");
            }
        }
    }

    private class AnnounceCDRunnable implements Runnable {

        @Override
        public void run() {
            announced = false;
        }
    }
}
