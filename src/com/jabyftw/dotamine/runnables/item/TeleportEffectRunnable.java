package com.jabyftw.dotamine.runnables.item;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class TeleportEffectRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private final Player p;
    private final Location destination;

    public TeleportEffectRunnable(DotaMine pl, Player p, Location destination) {
        this.pl = pl;
        this.p = p;
        this.destination = destination;
    }

    @Override
    public void run() {
        pl.breakEffect(destination, 2, 11); // lava
        pl.breakEffect(p.getLocation(), 2, 55); // redstone wire
    }
}
