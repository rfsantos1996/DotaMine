package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class AnnounceGameRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public AnnounceGameRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        pl.broadcast(pl.getLang("lang.theGamehasStarted"));
        pl.broadcast(pl.getLang("lang.creepsWillSpawn"));
    }

}
