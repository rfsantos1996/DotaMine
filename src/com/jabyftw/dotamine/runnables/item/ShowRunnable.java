package com.jabyftw.dotamine.runnables.item;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class ShowRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private final Player p;

    public ShowRunnable(DotaMine pl, Player p) {
        this.pl = pl;
        this.p = p;
    }

    @Override
    public void run() {
        if (pl.invisible.containsKey(p)) {
            pl.showPlayerFromTeam(p, pl.getOtherTeam(p));
            pl.smokeEffect(p.getLocation(), 10);
            if (pl.invisible.get(p) == 1) {    
                pl.invisibleSB.remove(p);
            } else {
                pl.getServer().getScheduler().cancelTask(pl.invisibleEffectW.get(p));
                pl.invisibleEffectW.remove(p);
                pl.invisibleW.remove(p);
            }
            pl.invisible.remove(p);
            p.sendMessage(pl.getLang("lang.inviOverMessage"));
        }
    }
}
