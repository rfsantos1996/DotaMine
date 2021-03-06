package com.jabyftw.dotamine.runnables.item;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class SmokeDeceitEffectRunnable extends BukkitRunnable {
    
    private final DotaMine pl;
    private final Player p;
    
    public SmokeDeceitEffectRunnable(DotaMine pl, Player p) {
        this.pl = pl;
        this.p = p;
    }

    @Override
    public void run() {
        pl.smokeEffect(p.getLocation(), 9);
    }
    
}
