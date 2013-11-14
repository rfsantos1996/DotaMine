package com.jabyftw.dota;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Rafael
 */
public class Jogador {

    private final DotaMine pl;
    private int LH, kills, killstreak, deaths, team;
    private boolean fixed;
    private final Player p;

    public Jogador(DotaMine pl, Player p, int LH, int kills, int killstreak, int deaths, int team) {
        this.pl = pl;
        this.p = p;
        this.LH = LH;
        this.kills = kills;
        this.killstreak = killstreak;
        this.deaths = deaths;
        this.team = team;
    }

    public Player getPlayer() {
        return p;
    }

    public int getTeam() {
        return team;
    }

    public int getLH() {
        return LH;
    }

    public void addLH() {
        LH = LH + 1;
        if (pl.useVault) {
            pl.econ.depositPlayer(p.getName(), pl.getLHMoney());
        }
    }

    public int getKills() {
        return kills;
    }

    public void addKill(Jogador dead) {
        kills = kills + 1;
        killstreak = killstreak + 1;
        int deadKillstreak = dead.getKillstreak();
        double gain = pl.getKillMoney(deadKillstreak);
        if (pl.useVault) {
            pl.econ.depositPlayer(p.getName(), gain);
        }
        String message = p.getDisplayName() + " won " + ChatColor.YELLOW + gain + " gold";
        if (team == 1) {
            pl.broadcastMsg(ChatColor.AQUA + message + ChatColor.AQUA + " for killing " + dead.getPlayer().getDisplayName() + " (Killstreak: " + deadKillstreak + ").");
        } else {
            pl.broadcastMsg(ChatColor.RED + message + ChatColor.RED + " for killing " + dead.getPlayer().getDisplayName() + ". (Killstreak: " + deadKillstreak + ")");
        }
    }

    public int getKillstreak() {
        return killstreak;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        deaths = deaths + 1;
        if (pl.useVault) {
            pl.econ.withdrawPlayer(p.getName(), pl.getDeathMoney(killstreak));
        }
        killstreak = 0;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isFixed() {
        return fixed;
    }
}
