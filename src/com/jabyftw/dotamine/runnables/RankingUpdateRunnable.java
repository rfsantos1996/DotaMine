package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Ranking;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class RankingUpdateRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public RankingUpdateRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        try {
            Statement s = pl.sql.getConn().createStatement();
            ResultSet rs = s.executeQuery("SELECT `name`, `wins`, `loses`, `kills`, `deaths` FROM `dotamine` ORDER BY `wins` DESC LIMIT 6;");
            pl.rankingList.clear();
            while (rs.next()) {
                pl.rankingList.add(new Ranking(rs.getString("name"), rs.getInt("wins"), rs.getInt("loses"), rs.getInt("kills"), rs.getInt("deaths")));
            }
        } catch (SQLException ex) {
            pl.getLogger().log(Level.SEVERE, "Couldn''t connect to MySQL: {0}", ex.getMessage());
        }
    }
}
