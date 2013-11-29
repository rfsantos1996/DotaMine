package com.jabyftw.dotamine;

/**
 *
 * @author Rafael
 */
public class Ranking {
    
    private final String name;
    private final int wins, loses, kills, deaths;
    
    public Ranking(String name, int wins, int loses, int kills, int deaths) {
        this.name = name;
        this.wins = wins;
        this.loses = loses;
        this.kills = kills;
        this.deaths = deaths;
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
}
