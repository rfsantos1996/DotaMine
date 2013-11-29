package com.jabyftw.dotamine;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 *
 * @author Rafael
 */
public class Tower {

    private final Location loc;
    private final String name;
    private final int team;

    public Tower(Location loc, String name, int team) {
        this.loc = loc;
        this.name = name;
        this.team = team;
    }

    public Location getLoc() {
        return loc;
    }

    public String getName() {
        return name;
    }

    public int getTeam() {
        return team;
    }

    public boolean isDestroyed() {
        return loc.getBlock().getType().equals(Material.AIR);
    }

    public void setDestroyed(boolean destroyed) {
        if (destroyed) {
            loc.getBlock().setType(Material.AIR);
        } else {
            loc.getBlock().setType(Material.TNT);
        }
    }
}
