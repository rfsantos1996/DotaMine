package com.jabyftw.dotamine.runnables.item;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class TeleportRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private final Player p;
    private final Location destination;

    public TeleportRunnable(DotaMine pl, Player p, Location destination) {
        this.pl = pl;
        this.p = p;
        this.destination = destination;
    }

    @Override
    public void run() {
        pl.getServer().getScheduler().cancelTask(pl.teleportingE.get(p));
        p.teleport(destination);
        destination.getWorld().playSound(destination, Sound.PORTAL_TRAVEL, 1, 0);
        pl.teleportingT.remove(p);
        pl.teleportingE.remove(p);
    }
}
