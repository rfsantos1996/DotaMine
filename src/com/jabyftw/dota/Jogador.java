package com.jabyftw.dota;

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

    public void addKill(int deadsKillstreak) {
        kills = kills + 1;
        killstreak = killstreak + 1;
        if (pl.useVault) {
            pl.econ.depositPlayer(p.getName(), pl.getKillMoney(deadsKillstreak));
        }
        // TODO: anunciar ganho e killstreak
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
        // TODO: anunciar perda
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isFixed() {
        return fixed;
    }
}
