package com.jabyftw.dotamine.runnables.item;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class ShadowShowRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private final Player p;

    public ShadowShowRunnable(DotaMine pl, Player p) {
        this.pl = pl;
        this.p = p;
    }

    @Override
    public void run() {
        pl.invisibleSB.remove(p);
        pl.showPlayerFromTeam(p, pl.getOtherTeam(p));
        pl.smokeEffect(p.getLocation(), 10);
        p.sendMessage(pl.getLang("lang.shadowOverMessage"));
    }
}
