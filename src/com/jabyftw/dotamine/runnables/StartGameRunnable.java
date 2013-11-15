package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class StartGameRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public StartGameRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        pl.state = 3;
        pl.broadcast(pl.getLang("lang.theGamehasStarted"));
    }

}
