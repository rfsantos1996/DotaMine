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
    public Tower(Location loc, String name) {
        this.loc = loc;
        this.name = name;
    }

    public Location getLoc() {
        return loc;
    }

    public String getName() {
        return name;
    }
    
    public boolean isDestroyed() {
        return loc.getBlock().getType().equals(Material.AIR);
    }
    
    public void setDestroyed(boolean destroyed) {
        if(destroyed) {
            loc.getBlock().setType(Material.AIR);
        } else {
            loc.getBlock().setType(Material.TNT);
        }
    }
    
}
