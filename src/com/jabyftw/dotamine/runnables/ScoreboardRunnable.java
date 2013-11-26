package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Jogador;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 *
 * @author Rafael
 */
public class ScoreboardRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private boolean firstUse = true;
    private final Map<Player, Objective> objectives = new HashMap();

    public ScoreboardRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Jogador j : pl.ingameList.values()) {
            if (firstUse) {
                ScoreboardManager sm = pl.getServer().getScoreboardManager();
                Scoreboard s = sm.getNewScoreboard();
                Objective o = s.registerNewObjective("test", "dummy");
                o.setDisplaySlot(DisplaySlot.SIDEBAR);
                o.setDisplayName("§4[DOTA]");
                o.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Last hits:")).setScore(j.getLH());
                o.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + "Deaths:")).setScore(j.getDeaths());
                o.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + "Kills:")).setScore(j.getKills());
                o.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + "Killstreak:")).setScore(j.getKillstreak());
                if (pl.useVault) {
                    o.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Money:")).setScore((int) pl.econ.getBalance(j.getPlayer().getName()));
                }
                j.getPlayer().setScoreboard(s);
                objectives.put(j.getPlayer(), o);
            } else {
                Objective o = objectives.get(j.getPlayer());
                o.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Last hits:")).setScore(j.getLH());
                o.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + "Deaths:")).setScore(j.getDeaths());
                o.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + "Kills:")).setScore(j.getKills());
                o.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + "Killstreak:")).setScore(j.getKillstreak());
                if (pl.useVault) {
                    o.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Money:")).setScore((int) pl.econ.getBalance(j.getPlayer().getName()));
                }
            }
        }
        firstUse = false;
    }

}
