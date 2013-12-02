package com.jabyftw.dotamine;

import java.text.DecimalFormat;

/**
 *
 * @author Rafael
 */
public class Ranking {

    private final String name;
    private final int wins, loses, kills, deaths, gamesPlayed, lhs;

    public Ranking(String name, int wins, int loses, int kills, int deaths, int lhs) {
        this.name = name;
        this.wins = wins;
        this.loses = loses;
        this.kills = kills;
        this.deaths = deaths;
        this.lhs = lhs;
        gamesPlayed = wins + loses;
    }

    public String getName() {
        return name;
    }

    public String getWins() {
        return Integer.toString(wins);
    }

    public String getLoses() {
        return Integer.toString(loses);
    }

    public String getKills() {
        return Integer.toString(kills);
    }

    public String getDeaths() {
        return Integer.toString(deaths);
    }

    public String getWinLossRatio() {
        int lose = loses;
        if (lose == 0) {
            lose = 1;
        }
        double d = (wins * 1.0 / lose);
        //return Double.toString(d);
        return new DecimalFormat("##0.00").format(d);
    }

    public String getKillDeathRatio() {
        int death = deaths;
        if (death == 0) {
            death = 1;
        }
        double d = (kills * 1.0 / death);
        //return Double.toString(d);
        return new DecimalFormat("##0.00").format(d);
    }

    public String getAvgLH() {
        int gamesPlayd = gamesPlayed;
        if (gamesPlayd == 0) {
            gamesPlayd = 1;
        }
        double d = (lhs * 1.0 / gamesPlayd);
        //return Double.toString(d);
        return new DecimalFormat("##0.00").format(d);
    }
}
