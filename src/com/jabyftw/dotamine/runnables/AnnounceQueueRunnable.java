package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class AnnounceQueueRunnable extends BukkitRunnable {
    
    private final DotaMine pl;
    
    public AnnounceQueueRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        if(pl.queue.size() > 0 && !pl.gameStarted) {
            pl.broadcast(pl.getLang("lang.queueSizeIs").replaceAll("%size", Integer.toString(pl.queue.size())).replaceAll("%needed", Integer.toString(pl.MIN_PLAYERS - pl.queue.size())));
        }
    }
    
}
