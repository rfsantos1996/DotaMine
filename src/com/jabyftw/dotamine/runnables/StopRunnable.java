package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class StopRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public StopRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        pl.endGame();
    }
}
