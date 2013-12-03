package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class AnnounceQueueRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private int startGame;
    private boolean announced = false;

    public AnnounceQueueRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        if (pl.queue.size() > 0 && (pl.state != pl.PLAYING || pl.state != pl.RESTARTING)) {
            if (pl.queue.size() < pl.MIN_PLAYERS) {
                int needed = pl.MIN_PLAYERS - pl.queue.size();
                pl.broadcast(pl.getLang("lang.queueSizeIs").replaceAll("%size", Integer.toString(pl.queue.size())).replaceAll("%needed", Integer.toString(needed)));
                if (announced) {
                    pl.getServer().getScheduler().cancelTask(startGame);
                    pl.state = pl.WAITING;
                    pl.debug("state = waiting");
                    announced = false;
                }
            } else {
                if (pl.queue.size() >= pl.MAX_PLAYERS) {
                    if(!announced) {
                        pl.broadcast(pl.getLang("lang.startingNow"));
                        pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StartGameRunnable(pl, false), 20);
                        announced = true;
                    } else {
                        pl.broadcast(pl.getLang("lang.startingNow"));
                        pl.getServer().getScheduler().cancelTask(startGame);
                        pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StartGameRunnable(pl, false), 20);
                    }
                } else {
                    if (!announced) {
                        pl.state = pl.WAITING_QUEUE;
                        pl.debug("state = waiting queue");
                        pl.broadcast(pl.getLang("lang.startingIn2Minutes"));
                        startGame = pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StartGameRunnable(pl, false), 20 * 121);
                        announced = true;
                    }
                }
            }
        }
    }

}
