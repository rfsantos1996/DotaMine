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

    public Config(DotaMine pl) {
        this.pl = pl;
    }

    public void generateConfig(FileConfiguration config) {
        //config.addDefault("config.", value);
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
        config.addDefault("lang.usePlayCommand", "&cUsage: &6/play (ranged/meele)");
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
        config.addDefault("lang.cantTeleportEverytime", "&4Please, calm down. &cYou can't teleport everytime.");
        config.addDefault("lang.tpCommand", "&cUsage: &4/dota tp (bot/mid/top/base)");
        config.addDefault("lang.rankingTitle", "&eName &6|&e Wins &6|&e Loses &6|&e Kills &6|&e Deaths");
        config.addDefault("lang.rankingEntry", "&e%name &6|&e %wins &6|&e %loses &6|&e %kills &6|&e %deaths");
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
        // Version
        config.addDefault("DoNotChangeThis.ConfigVersion", pl.version);
        config.options().copyDefaults(true);
        pl.saveConfig();
        pl.reloadConfig();
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
        }
    }

    public void setLocations(World w) {
        w.setPVP(true);
        w.setAutoSave(false);
        w.setSpawnFlags(false, false);
        for (Entity e : w.getEntities()) {
            e.remove();
        }
        pl.blueFMidT = new Location(w, 12, 33, -7);
        pl.towers.put(pl.blueFMidT, new Tower(pl.blueFMidT, "Blue Mid First Tower", 1));
        pl.tpPlace.put(pl.blueFMidT, new Location(w, 15, 25, -11, 44, 2));
        pl.blueFBotT = new Location(w, -57, 33, -85);
        pl.towers.put(pl.blueFBotT, new Tower(pl.blueFBotT, "Blue Bot First Tower", 1));
        pl.tpPlace.put(pl.blueFBotT, new Location(w, -55, 25, -89, 47, 4));
        pl.blueFTopT = new Location(w, 81, 33, 62);
        pl.towers.put(pl.blueFTopT, new Tower(pl.blueFTopT, "Blue Top First Tower", 1));
        pl.tpPlace.put(pl.blueFTopT, new Location(w, 84, 25, 59, 44, 2));
        pl.redFMidT = new Location(w, -7, 33, 7);
        pl.towers.put(pl.redFMidT, new Tower(pl.redFMidT, "Red Mid First Tower", 2));
        pl.tpPlace.put(pl.redFMidT, new Location(w, -11, 25, 10, 226, 2));
        pl.redFBotT = new Location(w, -82, 33, -65);
        pl.towers.put(pl.redFBotT, new Tower(pl.redFBotT, "Red Bot First Tower", 2));
        pl.tpPlace.put(pl.redFBotT, new Location(w, -86, 25, -63, -133, 4));
        pl.redFTopT = new Location(w, 52, 33, 87);
        pl.towers.put(pl.redFTopT, new Tower(pl.redFTopT, "Red Top Fisrt Tower", 2));
        pl.tpPlace.put(pl.redFTopT, new Location(w, 49, 25, 90, 222, 6));

        pl.blueSMidT = new Location(w, 50, 33, -42);
        pl.towers.put(pl.blueSMidT, new Tower(pl.blueSMidT, "Blue Mid Second Tower", 1));
        pl.tpPlace.put(pl.blueSMidT, new Location(w, 53, 25, -46, 47, 4));
        pl.blueSBotT = new Location(w, 30, 33, -83);
        pl.towers.put(pl.blueSBotT, new Tower(pl.blueSBotT, "Blue Bot Second Tower", 1));
        pl.tpPlace.put(pl.blueSBotT, new Location(w, 33, 25, -87, -309, 4));
        pl.blueSTopT = new Location(w, 82, 33, -17);
        pl.towers.put(pl.blueSTopT, new Tower(pl.blueSTopT, "Blue Top Second Tower", 1));
        pl.tpPlace.put(pl.blueSTopT, new Location(w, 85, 25, -21, 47, 7));
        pl.redSMidT = new Location(w, -49, 33, 51);
        pl.towers.put(pl.redSMidT, new Tower(pl.redSMidT, "Red Mid Second Tower", 2));
        pl.tpPlace.put(pl.redSMidT, new Location(w, -53, 25, 54, 225, 3));
        pl.redSBotT = new Location(w, -79, 33, -1);
        pl.towers.put(pl.redSBotT, new Tower(pl.redSBotT, "Red Bot Second Tower", 2));
        pl.tpPlace.put(pl.redSBotT, new Location(w, -83, 25, 1, 226, 6));
        pl.redSTopT = new Location(w, -10, 33, 87);
        pl.towers.put(pl.redSTopT, new Tower(pl.redSTopT, "Red Top Second Tower", 2));
        pl.tpPlace.put(pl.redSTopT, new Location(w, -14, 25, 90, 225, 2));

        pl.blueAncient = new Location(w, 79, 36, -77);
        pl.towers.put(pl.blueAncient, new Tower(pl.blueAncient, "Blue Ancient", 1));
        pl.redAncient = new Location(w, -72, 36, 79);
        pl.towers.put(pl.redAncient, new Tower(pl.redAncient, "Red Ancient", 2));

        pl.botSpawnPre = new Location(w, -75, 25, -81);
        pl.botSpawnPosR = new Location(w, -79, 25, -25);
        pl.botSpawnPosB = new Location(w, 6, 25, -83);
        pl.botCreepSpawn.add(pl.botSpawnPre);

        pl.midSpawnPre = new Location(w, 4, 25, 0);
        pl.midSpawnPosR = new Location(w, -35, 25, 37);
        pl.midSpawnPosB = new Location(w, 36, 25, -27);
        pl.midCreepSpawn.add(pl.midSpawnPre);

        pl.topSpawnPre = new Location(w, 74, 25, 80);
        pl.topSpawnPosR = new Location(w, 11, 25, 87);
        pl.topSpawnPosB = new Location(w, 81, 25, 2);
        pl.topCreepSpawn.add(pl.topSpawnPre);

        pl.blueJungleBot = new Location(w, 11, 25, -37);
        pl.jungleSpawn.add(pl.blueJungleBot);
        pl.blueJungleTop = new Location(w, 51, 25, 9);
        pl.jungleSpawn.add(pl.blueJungleTop);
        pl.redJungleBot = new Location(w, -45, 25, -11);
        pl.jungleSpawn.add(pl.redJungleBot);
        pl.redJungleTop = new Location(w, -2, 25, 58);
        pl.jungleSpawn.add(pl.redJungleTop);

        pl.blueDeploy = new Location(w, 86, 28 + 1, -87, 37, 13);
        pl.redDeploy = new Location(w, -80, 28 + 1, 87, -134, 16);
        pl.specDeploy = new Location(w, 2, 33 + 1, 0);
        pl.normalSpawn = new Location(w, 50, 8 + 1, -121, -86, 2);
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
                    + "  PRIMARY KEY (`name`));");
        } catch (SQLException e) {
            pl.mysqlEnabled = false;
            pl.getLogger().log(Level.SEVERE, "Couldn''t connect to MySQL and create table: {0}", e.getMessage());
        }
    }
}
