package com.jabyftw.dotamine.runnables.item;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class ItemCDRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private final Player p;
    private final int item;

    public ItemCDRunnable(DotaMine pl, Player p, int item) {
        this.pl = pl;
        this.p = p;
        this.item = item;
    }

    @Override
    public void run() {
        if (item == pl.SHADOW_BLADE) {
            pl.shadowCD.remove(p);
        } else if (item == pl.FORCE_STAFF) {
            pl.forceCD.remove(p);
        } else {
            pl.tpCD.remove(p);
        }
        p.sendMessage(pl.getLang("lang.itemOverCDMessage").replaceAll("%item", getItemName()));
    }
    
    private String getItemName() {
        if(item == pl.SHADOW_BLADE) {
            return "Shadow Blade";
        } else if(item == pl.FORCE_STAFF) {
            return "Force Staff";
        } else {
            return "TP Scroll"; // PAPER
        }
    }
}
