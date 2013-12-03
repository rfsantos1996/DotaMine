/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jabyftw.dotamine;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;

/**
 *
 * @author Rafael
 */
public class Config {

    private final DotaMine pl;
    private final FileConfiguration config;
    private boolean enabled;

    public Config(DotaMine pl, FileConfiguration config) {
        this.pl = pl;
        this.config = config;
    }

    public void generateConfig() {
        //config.addDefault("config.", value);
        config.addDefault("config.EnableAfterCheckingConfigDotYML", false);
        config.addDefault("config.useVault", false);
        config.addDefault("config.useControllableMobs", false);
        config.addDefault("config.useEffects", true);
        config.addDefault("config.nerfRangedAtNight", false);
        config.addDefault("config.worldName", "world");
        config.addDefault("config.scoreRunnableDelayInTicks", 60); // 3 sec
        config.addDefault("config.MAX_PLAYERS", 12);
        config.addDefault("config.MIN_PLAYERS", 6);
        config.addDefault("mysql.enabled", false);
        config.addDefault("mysql.username", "root");
        config.addDefault("mysql.password", "root");
        config.addDefault("mysql.database", "minecraft");
        config.addDefault("mysql.host", "localhost");
        config.addDefault("mysql.port", 3306);
        setupStructures();
        setupLang();
        // Version
        config.addDefault("DoNotChangeThis.ConfigVersion", pl.version);
        config.options().copyDefaults(true);
        pl.saveConfig();
        pl.reloadConfig();
        enabled = config.getBoolean("config.EnableAfterCheckingConfigDotYML");
        pl.worldName = config.getString("config.worldName");
        pl.useVault = config.getBoolean("config.useVault");
        pl.useControllableMobs = config.getBoolean("config.useControllableMobs");
        pl.useEffects = config.getBoolean("config.useEffects");
        pl.scoreRunnable = config.getInt("config.scoreRunnableDelayInTicks");
        pl.MIN_PLAYERS = config.getInt("config.MIN_PLAYERS");
        pl.MAX_PLAYERS = config.getInt("config.MAX_PLAYERS");
        pl.nerfRanged = config.getBoolean("config.nerfRangedAtNight");
        pl.mysqlEnabled = config.getBoolean("mysql.enabled");
        if (pl.mysqlEnabled) {
            pl.sql = new MySQL(pl, config.getString("mysql.username"), config.getString("mysql.password"), "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getInt("mysql.port") + "/" + config.getString("mysql.database"));
            createTable();
        }
        if (config.getInt("DoNotChangeThis.ConfigVersion") != pl.version) {
            pl.getLogger().log(Level.WARNING, "Recommended: recreate your config.yml");
            if (config.getInt("DoNotChangeThis.ConfigVersion") < 4) {
                alterTable();
            }
        }
    }

    public void setLocations(World w) {
        w.setPVP(true);
        w.setAutoSave(false);
        w.setSpawnFlags(false, false);
        for (Entity e : w.getEntities()) {
            e.remove();
        }

        pl.normalSpawn = new Location(w, config.getInt("structures.locations.normalspawn.locX"), config.getInt("structures.locations.normalspawn.locY"), config.getInt("structures.locations.normalspawn.locZ"));
        pl.redDeploy = new Location(w, config.getInt("structures.locations.redspawn.locX"), config.getInt("structures.locations.redspawn.locY"), config.getInt("structures.locations.redspawn.locZ"));
        pl.blueDeploy = new Location(w, config.getInt("structures.locations.bluespawn.locX"), config.getInt("structures.locations.bluespawn.locY"), config.getInt("structures.locations.bluespawn.locZ"));
        pl.specDeploy = new Location(w, config.getInt("structures.locations.spectator.locX"), config.getInt("structures.locations.spectator.locY"), config.getInt("structures.locations.spectator.locZ"));

        for (String keys : config.getConfigurationSection("structures.towers").getKeys(false)) {
            Structure t = new Structure(pl, new Location(w, config.getInt("structures.towers." + keys + ".locX"), config.getInt("structures.towers." + keys + ".locY"), config.getInt("structures.towers." + keys + ".locZ")), config.getString("structures.towers." + keys + ".name"), config.getString("structures.towers." + keys + ".lane"), config.getInt("structures.towers." + keys + ".number"), config.getString("structures.towers." + keys + ".team"), 1);
            pl.structures.add(t);
        }
        for (String keys : config.getConfigurationSection("structures.ancients").getKeys(false)) {
            Structure t = new Structure(pl, new Location(w, config.getInt("structures.ancients." + keys + ".locX"), config.getInt("structures.ancients." + keys + ".locY"), config.getInt("structures.ancients." + keys + ".locZ")), config.getString("structures.ancients." + keys + ".name"), config.getString("structures.ancients." + keys + ".lane"), config.getInt("structures.ancients." + keys + ".number"), config.getString("structures.ancients." + keys + ".team"), 2);
            pl.structures.add(t);
        }
        for (Structure t : pl.structures) {
            if (t.getNumber() < 0) {
                pl.getLogger().log(Level.WARNING, "Tower/Ancient number cant be lower than 0");
                pl.getPluginLoader().disablePlugin(pl);
            }
            if (t.getNumber() > pl.maxN) {
                pl.maxN = t.getNumber();
            }
        }
        for (Structure s : pl.structures) {
            int deltaN = (pl.maxN - s.getNumber()); // there are 3 towers, first = 1, then, 3 - 1 = 2 times *0.75
            if (deltaN == 0) {
                s.setHP(1);
            } else {
                s.setHP(deltaN);
            }
        }
        for (int a = -11; a <= 9; a++) {
            for (int b = -10; b <= 10; b++) {
                w.loadChunk(a, b);
            }
        }
    }

    private void createTable() {
        try {
            Statement s = pl.sql.getConn().createStatement();
            s.execute("CREATE TABLE IF NOT EXISTS `dotamine` (\n"
                    + "  `name` VARCHAR(24) NOT NULL,\n"
                    + "  `wins` INT NOT NULL DEFAULT 0,\n"
                    + "  `loses` INT NOT NULL DEFAULT 0,\n"
                    + "  `kills` INT NOT NULL DEFAULT 0,\n"
                    + "  `deaths` INT NOT NULL DEFAULT 0,\n"
                    + "  `lhs` INT NOT NULL DEFAULT 0,\n"
                    + "  PRIMARY KEY (`name`));");
        } catch (SQLException e) {
            pl.mysqlEnabled = false;
            pl.getLogger().log(Level.SEVERE, "Couldn''t connect to MySQL and create table: {0}", e.getMessage());
        }
    }

    private void alterTable() {
        try {
            Statement s = pl.sql.getConn().createStatement();
            s.execute("ALTER TABLE `dotamine` ADD COLUMN `lhs` INT(11) NOT NULL DEFAULT 0 AFTER `deaths`;");
        } catch (SQLException ex) {
            pl.getLogger().log(Level.SEVERE, "Couldn''t alterate table: {0}", ex.getMessage());
        }
    }

    private void setupLang() {
        //config.addDefault("lang.", "&");
        config.addDefault("lang.motd.WAITING", "&6[Dota] &aWaiting players... %queue/%min");
        config.addDefault("lang.motd.WAITING_QUEUE", "&6[Dota] &eWaiting queue to fill...  %queue/%max");
        config.addDefault("lang.motd.SPAWNING", "&6[Dota] &eStarting in +-60 sec... %ingame/%max");
        config.addDefault("lang.motd.PLAYING", "&6[Dota] &cPlaying... %ingame/%max");
        config.addDefault("lang.motd.RESTARTING", "&6[Dota] &4Restarting...");
        config.addDefault("lang.chat.ingame", "&6[%team&6] %general");
        config.addDefault("lang.chat.spectating", "&6[&7Spectator&6] %general");
        config.addDefault("lang.chat.general", "%name&r: %message");
        config.addDefault("lang.chat.blueTeam", "&bBlue");
        config.addDefault("lang.chat.redTeam", "&4Red");
        config.addDefault("lang.noPermission", "&cNo permission!");
        config.addDefault("lang.joinMessage", "&7+ %name");
        config.addDefault("lang.quitMessage", "&7- %name");
        config.addDefault("lang.onlyIngame", "&4You are not a player!");
        config.addDefault("lang.alreadyInvisible", "&4Already invisible!");
        config.addDefault("lang.announceTowerKill", "&6You received &e%money&6 for destroying the tower.");
        config.addDefault("lang.tpDontMove", "&cYou are teleporting...");
        config.addDefault("lang.tpCancelled", "&4Teleport cancelled.");
        config.addDefault("lang.noPaperOnHand", "&cYou need a PAPER on YOUR hand.");
        config.addDefault("lang.notEvenPlaying", "&4You must be on a game");
        config.addDefault("lang.gameIsFull", "&cSorry, the game is full!");
        config.addDefault("lang.onBlueTeam", "&6You are on &bBlue Team&6!");
        config.addDefault("lang.onRedTeam", "&6You are on &4Red Team&6!");
        config.addDefault("lang.alreadyStarted", "&4Already started!");
        config.addDefault("lang.cantJoinQueue", "&cCant join queue!");
        config.addDefault("lang.leftQueue", "&4You left queue. &6Use &c/join (meele/ranged) &6to rejoin.");
        config.addDefault("lang.gameNotStarted", "&cThere is no game to spectate!");
        config.addDefault("lang.startingIn60sec", "&6Game starting in &c60 seconds&6! Use &c/bs&6 for shopping.");
        config.addDefault("lang.youLoseXMoney", "&cYou lost &4%money&c for dying.");
        config.addDefault("lang.theGamehasStarted", "&6Good Luck and Have fun! &4Dota&c has started!");
        config.addDefault("lang.creepsWillSpawn", "&6Creeps will spawn in 10 seconds.");
        config.addDefault("lang.joinedSpectator", "&6You are now a spectator! &cTo leave, use &4/spectate leave");
        config.addDefault("lang.leftSpectator", "&cYou left spectator mode!");
        config.addDefault("lang.waitingTheGame", "&6You are on queue! &cWaiting players...");
        config.addDefault("lang.youCanPlay", "&6You can play Dota using &c/join");
        config.addDefault("lang.youCanJoin", "&6You can join Dota using &c/join&6 before it starts");
        config.addDefault("lang.youCanSpectate", "&6You can spectate using &c/spectate&6!");
        config.addDefault("lang.redTeamWon", "&cRed Team &6won! &eCongratulations! &cRestarting in 30 sec!");
        config.addDefault("lang.blueTeamWon", "&bBlue Team &6won! &eCongratulations! &cRestarting in 30 sec!");
        config.addDefault("lang.towerDestroyed", "&4%tower &cwas destroyed");
        config.addDefault("lang.kickMessage", "&4The game is over!&c Restarting...");
        config.addDefault("lang.usePlayCommand", "&cUsage: &6/play (ranged/meele/leave)");
        config.addDefault("lang.settedMeele", "&6You will play as a Meele hero.");
        config.addDefault("lang.settedRanged", "&6You will play as a Ranged hero.");
        config.addDefault("lang.forcingStart", "&cForcing start...");
        config.addDefault("lang.nobodyOnQueue", "&cQueue is empty! Cant force start...");
        config.addDefault("lang.diedForNeutral", "%name &cdied for a &4creep or neutral&c.");
        config.addDefault("lang.queueSizeIs", "&e%size players &6in queue! &cWe need %needed player(s).");
        config.addDefault("lang.onePlayerLeft", "&cThere's only 1 player left. &4Restarting server...");
        config.addDefault("lang.lowRangedNightVision", "&cRanged players have low night vision. &4ATENTION AT NIGHT!");
        config.addDefault("lang.alreadyInQueueUpdatedAttack", "&cAlready on queue. &6New attack type: %attack");
        config.addDefault("lang.noRankingFound", "&cNo ranking entry.");
        config.addDefault("lang.startingIn2Minutes", "&cStarting game in 2 minutes. &6Last chance to use &4/join");
        config.addDefault("lang.couldntStartGame", "&4Couldn't start game! &cTrying again...");
        config.addDefault("lang.itemCDMessage", "&4%item &cis on cooldown.");
        config.addDefault("lang.itemOverCDMessage", "&e%item &6is now usable.");
        config.addDefault("lang.inviOverMessage", "&cYou are now visible.");
        config.addDefault("lang.itemUseMessage", "&6You used &e%item&6. It can be used again after %cd sec.");
        config.addDefault("lang.itemUseMessageDontCD", "&6You used &e%item&6.");
        config.addDefault("lang.hasTarrasque", "&6You now have &eHeart of Tarrasque&6. You will regen every 5 sec.");
        config.addDefault("lang.tarrasqueRemoved", "&cYou don't have &4Heart of Tarrasque&c.");
        config.addDefault("lang.alreadySpectating", "&cYou're already spectating");
        config.addDefault("lang.dontSpamClicks", "&4Please, calm down. &cDon't spam clicks.");
        config.addDefault("lang.tpCommand", "&cUsage: &4/dota tp (bot/mid/top/base)");
        config.addDefault("lang.startingNow", "&eQueue is full. &6Starting now.");
        config.addDefault("lang.towerUnderAttack", "%tower &4is under attack. &6Tower HP: &e%hp");
        config.addDefault("lang.rankingTitle", "&e=== &6Ranking &e===");
        config.addDefault("lang.rankingEntry", "&e%name &6| W/L:&e %wlr &6| W:&e %wins &6| L:&e %loses &6| K/D:&e %kdr &6| K:&e %kills &6| D:&e %deaths &6| Avg. LH:&e %avgLH");
        config.addDefault("lang.killstreak.one", "%name &6killed %dead &6for &e%money");
        config.addDefault("lang.killstreak.two", "%name &6killed %dead &6for &e%money");
        config.addDefault("lang.killstreak.tree", "%name &6killed %dead &6for &e%money &6- &4KILLING SPREE");
        config.addDefault("lang.killstreak.four", "%name &6killed %dead &6for &e%money &6- &4DOMINATING");
        config.addDefault("lang.killstreak.five", "%name &6killed %dead &6for &e%money &6- &4MEGA KILL");
        config.addDefault("lang.killstreak.six", "%name &6killed %dead &6for &e%money &6- &4UNSTOPPABLE");
        config.addDefault("lang.killstreak.seven", "%name &6killed %dead &6for &e%money &6- &4WICKED SICK");
        config.addDefault("lang.killstreak.eight", "%name &6killed %dead &6for &e%money &6- &4MONSTER KILL");
        config.addDefault("lang.killstreak.nine", "%name &6killed %dead &6for &e%money &6- &4GOD LIKE");
        config.addDefault("lang.killstreak.tenAndBeyond", "%name &6killed %dead &6for &e%money &6- &4BEYOND GOD LIKE");
        config.addDefault("lang.killstreak.fiftyAndBeyond", "%name &6killed %dead &6for &e%money &6- &4KILLING DOMINATING MEGA UNSTOPPABLE WICKED MONSTER BEYOND GODLIKE");
    }

    private void setupStructures() {
        if (!enabled) {
            config.addDefault("structures.locations.normalspawn.locX", 5);
            config.addDefault("structures.locations.normalspawn.locY", 64);
            config.addDefault("structures.locations.normalspawn.locZ", -5);

            config.addDefault("structures.locations.spectator.locX", 5);
            config.addDefault("structures.locations.spectator.locY", 64);
            config.addDefault("structures.locations.spectator.locZ", -5);

            config.addDefault("structures.locations.redspawn.locX", 5);
            config.addDefault("structures.locations.redspawn.locY", 64);
            config.addDefault("structures.locations.redspawn.locZ", -5);

            config.addDefault("structures.locations.bluespawn.locX", 5);
            config.addDefault("structures.locations.bluespawn.locY", 64);
            config.addDefault("structures.locations.bluespawn.locZ", -5);

            config.addDefault("structures.towers.tower1.name", "bob blue");
            config.addDefault("structures.towers.tower1.locX", 5);
            config.addDefault("structures.towers.tower1.locY", 64);
            config.addDefault("structures.towers.tower1.locZ", -5);
            config.addDefault("structures.towers.tower1.lane", "mid");
            config.addDefault("structures.towers.tower1.team", "blue");
            config.addDefault("structures.towers.tower1.number", "1");

            config.addDefault("structures.towers.tower1.name", "bob red");
            config.addDefault("structures.towers.tower1.locX", 5);
            config.addDefault("structures.towers.tower1.locY", 64);
            config.addDefault("structures.towers.tower1.locZ", -5);
            config.addDefault("structures.towers.tower1.lane", "mid");
            config.addDefault("structures.towers.tower1.team", "red");
            config.addDefault("structures.towers.tower1.number", "1");

            config.addDefault("structures.ancients.ancient1.name", "ancient blue");
            config.addDefault("structures.ancients.ancient1.locX", 5);
            config.addDefault("structures.ancients.ancient1.locY", 64);
            config.addDefault("structures.ancients.ancient1.locZ", -5);
            config.addDefault("structures.ancients.ancient1.lane", "mid");
            config.addDefault("structures.ancients.ancient1.team", "blue");
            config.addDefault("structures.ancients.ancient1.number", "1");

            config.addDefault("structures.ancients.ancient1.name", "ancient red");
            config.addDefault("structures.ancients.ancient1.locX", 5);
            config.addDefault("structures.ancients.ancient1.locY", 64);
            config.addDefault("structures.ancients.ancient1.locZ", -5);
            config.addDefault("structures.ancients.ancient1.lane", "mid");
            config.addDefault("structures.ancients.ancient1.team", "red");
            config.addDefault("structures.ancients.ancient1.number", "1");
        }
    }
}
