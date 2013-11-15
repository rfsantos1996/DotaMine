package com.jabyftw.dotamine;

import org.bukkit.entity.Player;

/**
 *
 * @author Rafael
 */
public class Jogador {

    private final DotaMine pl;
    private final Player p;
    private int lh, kills, killstreak, deaths;
    private final int team;

    public Jogador(DotaMine pl, Player p, int LH, int kills, int killstreak, int deaths, int team) {
        this.pl = pl;
        this.p = p;
        this.lh = LH;
        this.kills = kills;
        this.killstreak = killstreak;
        this.deaths = deaths;
        this.team = team;
    }

    public int getKillstreak() {
        return killstreak;
    }

    public Player getPlayer() {
        return p;
    }

    public int getLH() {
        return lh;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getKills() {
        return kills;
    }

    public int getTeam() {
        return team;
    }

    public void addLH() {
        lh = lh + 1;
        int money = (int) getLHMoney();
        if(pl.useVault) {
            pl.econ.depositPlayer(p.getName(), money);
        }
    }

    public void addKill(Jogador dead) {
        kills = kills + 1;
        killstreak = killstreak + 1;
        int money = (int) getKillMoney(dead.getKillstreak());
        pl.broadcast(pl.getLang("lang.playerWonXMoneyforKilling").replaceAll("%player", p.getCustomName()).replaceAll("%money", Integer.toString(money)).replaceAll("%dead", dead.getPlayer().getCustomName()));
        if(pl.useVault) {
            pl.econ.depositPlayer(p.getName(), money);
        }
        //TODO: announce
    }

    public void addDeath() {
        deaths = deaths + 1;
        int deathcost = (int) getDeathMoney();
        p.sendMessage(pl.getLang("lang.youLoseXMoney").replaceAll("%money", Integer.toString(deathcost)));
        if(pl.useVault) {
            pl.econ.withdrawPlayer(p.getName(), deathcost);
        }
        killstreak = 0;
    }

    private double getKillMoney(int deadsKillstreak) {
        if (deadsKillstreak < 3) {
            return 100;
        } else if (deadsKillstreak > 3) {
            return 125;
        } else if (deadsKillstreak > 4) {
            return 250;
        } else if (deadsKillstreak > 5) {
            return 375;
        } else if (deadsKillstreak > 6) {
            return 500;
        } else if (deadsKillstreak > 7) {
            return 625;
        } else if (deadsKillstreak > 8) {
            return 750;
        } else if (deadsKillstreak > 9) {
            return 875;
        } else {
            return 1000 + (deadsKillstreak * 10);
        }
    }

    private double getLHMoney() {
        double x = Math.random();
        if (x > 21 && x < 30) {
            return x;
        }
        return getLHMoney();
    }

    private double getDeathMoney() {
        if (killstreak < 3) {
            return 100 / 1.2;
        } else if (killstreak >= 3) {
            return 125 / 1.5;
        } else if (killstreak >= 4) {
            return 250 / 1.6;
        } else if (killstreak >= 5) {
            return 375 / 1.8;
        } else if (killstreak >= 6) {
            return 500 / 2;
        } else if (killstreak >= 7) {
            return 625 / 2;
        } else if (killstreak >= 8) {
            return 750 / 2;
        } else if (killstreak >= 9) {
            return 875 / 2;
        } else {
            return 1000 + (killstreak * 10) / 2.2;
        }
    }
}
