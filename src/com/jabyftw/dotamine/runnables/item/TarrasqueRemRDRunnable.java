package com.jabyftw.dotamine.runnables.item;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class TarrasqueRemRDRunnable extends BukkitRunnable {
    
    private final DotaMine pl;
    private final Player p;
    
    public TarrasqueRemRDRunnable(DotaMine pl, Player p) {
        this.pl = pl;
        this.p = p;
    }
    
    @Override
    public void run() {
        pl.tarrasqueRecentlyDamaged.remove(p);
    }
}
