package com.jabyftw.dotamine;

import java.util.List;
import org.bukkit.entity.Player;

/**
 *
 * @author Rafael
 */
public class Spectator {

    private final DotaMine pl;
    private final Player p;
    private int n = 1;

    Spectator(DotaMine pl, Player p) {
        this.pl = pl;
        this.p = p;
    }

    public Player getPlayer() {
        return p;
    }

    public List getIngameList() {
        return pl.getSpectatorList();
    }

    public Player addN() {
        List<Player> l = getIngameList();
        n = n + 1;
        if (n >= l.size()) {
            n = 0;
        }
        return l.get(n);
    }

    public Player subN() {
        List<Player> l = getIngameList();
        n = n - 1;
        if (n < 0) {
            n = l.size() -1;
        }
        return l.get(n);
    }
}
