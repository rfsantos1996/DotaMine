package com.jabyftw.dotamine.runnables.item;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author Rafael
 */
public class ForceRunnable extends BukkitRunnable {

    private final Player p;

    public ForceRunnable(Player p) {
        this.p = p;
    }

    @Override
    public void run() {
        Vector vec = p.getLocation().getDirection();
        vec.setY(0);
        vec.multiply(2.5 / vec.length());
        vec.setY(0.3);
        p.setVelocity(vec);
    }
}
