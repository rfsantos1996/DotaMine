package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class StartGameRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private final boolean forced;

    public StartGameRunnable(DotaMine pl, boolean forced) {
        this.pl = pl;
        this.forced = forced;
    }

    @Override
    public void run() {
        if (pl.queue.size() >= pl.MIN_PLAYERS || forced) {
            pl.getServer().getScheduler().cancelTask(pl.announceQueue);
            pl.debug("asked for start");
            pl.startGame(forced);
        } else {
            pl.broadcast(pl.getLang("lang.couldntStartGame"));
        }
    }

}
