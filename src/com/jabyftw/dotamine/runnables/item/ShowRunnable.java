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

    public ShowRunnable(DotaMine pl, Player p, int from) {
        this.pl = pl;
        this.p = p;
        if (from == 1) { // shadow
            pl.invisibleSB.remove(p);
        } else { // web
            pl.invisibleW.remove(p);
        }
    }

    @Override
    public void run() {
        pl.showPlayerFromTeam(p, pl.getOtherTeam(p));
        pl.smokeEffect(p.getLocation(), 10);
        p.sendMessage(pl.getLang("lang.inviOverMessage"));
    }
}
