package com.jabyftw.dotamine;

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
        double d = (wins / lose);
        return Double.toString(d);
    }

    public String getKillDeathRatio() {
        int death = deaths;
        if (death == 0) {
            death = 1;
        }
        double d = (kills / death);
        return Double.toString(d);
    }

    public String getAvgLH() {
        int gamesPlayd = gamesPlayed;
        if (gamesPlayd == 0) {
            gamesPlayd = 1;
        }
        double d = (lhs / gamesPlayd);
        return Double.toString(d);
    }
}
