package com.jabyftw.dotamine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.bukkit.entity.Player;

/**
 *
 * @author Rafael
 */
public class Jogador {

    private final DotaMine pl;
    private final Player p;
    private int lh, kills, killstreak, deaths;
    private final int team, attackType;

    public Jogador(DotaMine pl, Player p, int LH, int kills, int killstreak, int deaths, int team, int attackType) {
        this.pl = pl;
        this.p = p;
        this.lh = LH;
        this.kills = kills;
        this.killstreak = killstreak;
        this.deaths = deaths;
        this.team = team;
        this.attackType = attackType;
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

    public int getAttackType() {
        return attackType; // 1 = Meele, 2 = Ranged
    }

    public int getMySQLKills() {
        int mysqlkills = 0;
        try {
            Statement s = pl.sql.getConn().createStatement();
            ResultSet rs = s.executeQuery("SELECT `kills` FROM dotamine WHERE `name`='" + p.getName() + "' LIMIT 1;");
            while (rs.next()) {
                mysqlkills = rs.getInt("kills");
            }
        } catch (SQLException ex) {
            pl.getLogger().log(Level.SEVERE, "Couldn't use MySQL: {0}", ex.getMessage());
        }
        return mysqlkills;
    }
    
    public int getMySQLLHs() {
        int mysqlLHs = 0;
        try {
            Statement s = pl.sql.getConn().createStatement();
            ResultSet rs = s.executeQuery("SELECT `lhs` FROM dotamine WHERE `name`='" + p.getName() + "' LIMIT 1;");
            while (rs.next()) {
                mysqlLHs = rs.getInt("lhs");
            }
        } catch (SQLException ex) {
            pl.getLogger().log(Level.SEVERE, "Couldn't use MySQL: {0}", ex.getMessage());
        }
        return mysqlLHs;
    }

    public int getMySQLDeaths() {
        int mysqldeaths = 0;
        try {
            Statement s = pl.sql.getConn().createStatement();
            ResultSet rs = s.executeQuery("SELECT `deaths` FROM dotamine WHERE `name`='" + p.getName() + "' LIMIT 1;");
            while (rs.next()) {
                mysqldeaths = rs.getInt("deaths");
            }
        } catch (SQLException ex) {
            pl.getLogger().log(Level.SEVERE, "Couldn't use MySQL: {0}", ex.getMessage());
        }
        return mysqldeaths;
    }

    public int getWins() {
        int wins = 0;
        try {
            Statement s = pl.sql.getConn().createStatement();
            ResultSet rs = s.executeQuery("SELECT `wins` FROM dotamine WHERE `name`='" + p.getName() + "' LIMIT 1;");
            while (rs.next()) {
                wins = rs.getInt("wins");
            }
        } catch (SQLException ex) {
            pl.getLogger().log(Level.SEVERE, "Couldn't use MySQL: {0}", ex.getMessage());
        }
        return wins;
    }

    public int getLoses() {
        int loses = 0;
        try {
            Statement s = pl.sql.getConn().createStatement();
            ResultSet rs = s.executeQuery("SELECT `loses` FROM dotamine WHERE `name`='" + p.getName() + "' LIMIT 1;");
            while (rs.next()) {
                loses = rs.getInt("loses");
            }
        } catch (SQLException ex) {
            pl.getLogger().log(Level.SEVERE, "Couldn't use MySQL: {0}", ex.getMessage());
        }
        return loses;
    }

    public void addWin() {
        if (pl.mysqlEnabled) {
            try {
                Statement s = pl.sql.getConn().createStatement();
                s.executeUpdate("UPDATE `dotamine` SET `wins`=" + (getWins() + 1) + ", `lhs`=" + (getMySQLLHs() + lh) + " WHERE `name`='" + p.getName() + "';");
            } catch (SQLException ex) {
                pl.getLogger().log(Level.SEVERE, "Couldn't use MySQL: {0}", ex.getMessage());
            }
        }
    }

    public void addLose() {
        if (pl.mysqlEnabled) {
            try {
                Statement s = pl.sql.getConn().createStatement();
                s.executeUpdate("UPDATE `dotamine` SET `loses`=" + (getLoses() + 1) + ", `lhs`=" + (getMySQLLHs() + lh) + " WHERE `name`='" + p.getName() + "';");
            } catch (SQLException ex) {
                pl.getLogger().log(Level.SEVERE, "Couldn't use MySQL: {0}", ex.getMessage());
            }
        }
    }

    public void addLH() {
        lh = lh + 1;
        if (pl.useVault) {
            int money;
            if (pl.megaCreeps) {
                money = (int) pl.getRandom(32, 42);
            } else {
                money = (int) pl.getRandom(42, 52);
            }
            pl.econ.depositPlayer(p.getName(), money);
        }
    }

    public void addJungleLH() {
        lh = lh + 1;
        if (pl.useVault) {
            int money;
            if (pl.megaCreeps) {
                money = (int) pl.getRandom(63, 74);
            } else {
                money = (int) pl.getRandom(83, 94);
            }
            pl.econ.depositPlayer(p.getName(), money);
        }
    }

    public void addKill(Jogador dead) {
        kills = kills + 1;
        killstreak = killstreak + 1;
        if (pl.useVault) {
            int money = (int) getKillMoney(dead.getKillstreak());
            pl.econ.depositPlayer(p.getName(), money);
            announceKillstreak(p.getDisplayName(), dead.getPlayer().getDisplayName(), killstreak, money);
        }
        if (pl.mysqlEnabled) {
            try {
                Statement s = pl.sql.getConn().createStatement();
                s.executeUpdate("UPDATE `dotamine` SET `kills`=" + (getMySQLKills() + 1) + " WHERE `name`='" + p.getName() + "';");
            } catch (SQLException ex) {
                pl.getLogger().log(Level.SEVERE, "Couldn't use MySQL: {0}", ex.getMessage());
            }
        }
    }

    public void addDeath() {
        deaths = deaths + 1;
        if (pl.useVault) {
            int deathcost = (int) getDeathMoney();
            double balance = pl.econ.getBalance(p.getName());
            if (deathcost > balance) {
                if (balance != 0) {
                    pl.econ.withdrawPlayer(p.getName(), balance);
                    p.sendMessage(pl.getLang("lang.youLoseXMoney").replaceAll("%money", "everything"));
                } // else dont have anything...
            } else {
                pl.econ.withdrawPlayer(p.getName(), deathcost);
                p.sendMessage(pl.getLang("lang.youLoseXMoney").replaceAll("%money", Integer.toString(deathcost)));
            }
        }
        killstreak = 0;
        if (pl.mysqlEnabled) {
            try {
                Statement s = pl.sql.getConn().createStatement();
                s.executeUpdate("UPDATE `dotamine` SET `deaths`=" + (getMySQLDeaths() + 1) + " WHERE `name`='" + p.getName() + "';");
            } catch (SQLException ex) {
                pl.getLogger().log(Level.SEVERE, "Couldn't use MySQL: {0}", ex.getMessage());
            }
        }
    }

    public void addNeutralDeath() {
        addDeath();
        pl.broadcast(pl.getLang("lang.diedForNeutral").replaceAll("%name", p.getDisplayName()));
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

    private void announceKillstreak(String name, String dead, int killstreak, double money) {
        if (killstreak < 2) {
            pl.broadcast(pl.getLang("lang.killstreak.one").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
        } else if (killstreak == 2) {
            pl.broadcast(pl.getLang("lang.killstreak.two").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
        } else if (killstreak == 3) {
            pl.broadcast(pl.getLang("lang.killstreak.tree").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
        } else if (killstreak == 4) {
            pl.broadcast(pl.getLang("lang.killstreak.four").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
        } else if (killstreak == 5) {
            pl.broadcast(pl.getLang("lang.killstreak.five").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
        } else if (killstreak == 6) {
            pl.broadcast(pl.getLang("lang.killstreak.six").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
        } else if (killstreak == 7) {
            pl.broadcast(pl.getLang("lang.killstreak.seven").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
        } else if (killstreak == 8) {
            pl.broadcast(pl.getLang("lang.killstreak.eight").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
        } else if (killstreak == 9) {
            pl.broadcast(pl.getLang("lang.killstreak.nine").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
        } else if (killstreak > 9) {
            if (killstreak >= 50) {
                pl.broadcast(pl.getLang("lang.killstreak.fiftyAndBeyond").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
            } else {
                pl.broadcast(pl.getLang("lang.killstreak.tenAndBeyond").replaceAll("%name", name).replaceAll("%dead", dead).replaceAll("%money", Double.toString(money)));
            }
        }
    }

    public void addTowerKillMoney() {
        if (pl.useVault) {
            int money = (int) pl.getRandom(130, 180);
            pl.econ.depositPlayer(p.getName(), money);
            p.sendMessage(pl.getLang("lang.announceTowerKill").replaceAll("%money", Integer.toString(money)));
        }
    }
}
