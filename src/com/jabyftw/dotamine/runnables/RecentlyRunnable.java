package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.Location;

/**
 *
 * @author Rafael
 */
public class RecentlyRunnable implements Runnable {

    private final DotaMine pl;
    private final Location loc;
    private final int type;

    public RecentlyRunnable(DotaMine pl, Location loc, int type) {
        this.pl = pl;
        this.loc = loc;
        this.type = type;
    }

    @Override
    public void run() {
        if (type == 1) { // Red
            pl.jungleRedSpawn.put(loc, false);
        } else if (type == 2) { // blue
            pl.jungleBlueSpawn.put(loc, false);
        } else { // Default
            pl.jungleSpawn.put(loc, false);
        }
    }
}
