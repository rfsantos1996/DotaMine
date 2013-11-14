package com.jabyftw.dota.runnable;

import com.jabyftw.dota.DotaMine;
import com.jabyftw.dota.Jogador;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class UnfreezeRunnable extends BukkitRunnable {
    
    private final DotaMine pl;
    public UnfreezeRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Jogador j : pl.players.values()) {
            j.setFixed(false);
            j.getPlayer().sendMessage(ChatColor.GOLD + "Good luck, have fun!");
        }
        pl.broadcastMsg(ChatColor.GOLD + "The game has started.");
    }
}
