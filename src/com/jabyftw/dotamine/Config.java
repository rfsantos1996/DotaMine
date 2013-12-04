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
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;

/**
 *
 * @author Rafael
 */
public class Config {

    private final DotaMine pl;
    private CustomConfig configYML;
    private CustomConfig langYML;
    private CustomConfig structuresYML;
    public FileConfiguration defConfig;
    public FileConfiguration lang;
    public FileConfiguration structures;
    private boolean enabled;

    public Config(DotaMine pl) {
        this.pl = pl;
    }

    public void generateConfig() {
        configYML = new CustomConfig(pl, "config");
        langYML = new CustomConfig(pl, "lang");
        structuresYML = new CustomConfig(pl, "structures");

        defConfig = configYML.getCustomConfig();
        lang = langYML.getCustomConfig();
        structures = structuresYML.getCustomConfig();

        //config.addDefault("config.", value);
        defConfig.addDefault("config.EnableAfterCheckingStructuresDotYML", false);
        defConfig.addDefault("config.useVault", false);
        defConfig.addDefault("config.useControllableMobs", false);
        defConfig.addDefault("config.useEffects", true);
        defConfig.addDefault("config.nerfRangedAtNight", false);
        defConfig.addDefault("config.debugMode", false);
        defConfig.addDefault("config.restartAfterFinishing", true);
        defConfig.addDefault("config.scoreRunnableDelayInTicks", 60); // 3 sec
        defConfig.addDefault("config.MAX_PLAYERS", 12);
        defConfig.addDefault("config.MIN_PLAYERS", 6);
        defConfig.addDefault("config.world.name", "world");
        defConfig.addDefault("config.world.fromChunkX", -66);
        defConfig.addDefault("config.world.toChunkX", -48);
        defConfig.addDefault("config.world.fromChunkY", 15);
        defConfig.addDefault("config.world.toChunkY", -5);
        defConfig.addDefault("mysql.enabled", false);
        defConfig.addDefault("mysql.username", "root");
        defConfig.addDefault("mysql.password", "root");
        defConfig.addDefault("mysql.database", "minecraft");
        defConfig.addDefault("mysql.host", "localhost");
        defConfig.addDefault("mysql.port", 3306);
        setupStructures(structures);
        setupLang(lang);
        // Version
        defConfig.addDefault("DoNotChangeThis.ConfigVersion", pl.version);
        configYML.saveCustomConfig();
        pl.debug = defConfig.getBoolean("config.debugMode");
        enabled = defConfig.getBoolean("config.EnableAfterCheckingStructuresDotYML");
        pl.worldName = defConfig.getString("config.world.name");
        pl.useVault = defConfig.getBoolean("config.useVault");
        pl.useControllableMobs = defConfig.getBoolean("config.useControllableMobs");
        pl.useEffects = defConfig.getBoolean("config.useEffects");
        pl.restartAfter = defConfig.getBoolean("config.restartAfterFinishing");
        pl.scoreRunnable = defConfig.getInt("config.scoreRunnableDelayInTicks");
        pl.MIN_PLAYERS = defConfig.getInt("config.MIN_PLAYERS");
        pl.MAX_PLAYERS = defConfig.getInt("config.MAX_PLAYERS");
        pl.nerfRanged = defConfig.getBoolean("config.nerfRangedAtNight");
        pl.mysqlEnabled = defConfig.getBoolean("mysql.enabled");
        if (pl.mysqlEnabled) {
            pl.sql = new MySQL(pl, defConfig.getString("mysql.username"), defConfig.getString("mysql.password"), "jdbc:mysql://" + defConfig.getString("mysql.host") + ":" + defConfig.getInt("mysql.port") + "/" + defConfig.getString("mysql.database"));
            createTable();
            if (defConfig.getInt("DoNotChangeThis.ConfigVersion") < 4) {
                alterTable();
            }
        }
        if (!pl.restartAfter) {
            if (pl.getServer().getWorlds().size() < 2) {
                pl.restartAfter = true;
                pl.getLogger().log(Level.WARNING, "You must have more than 1 world to DotaMine unload the Dota world.");
            }
        }
        if (pl.restarted) {
            pl.getServer().createWorld(new WorldCreator(pl.worldName));
        }
        setLocations(pl.getServer().getWorld(pl.worldName), structures);
    }

    public void setLocations(World w, FileConfiguration config) {
        w.setPVP(true);
        w.setAutoSave(false);
        w.setSpawnFlags(false, false);
        for (Entity e : w.getEntities()) {
            e.remove();
        }

        pl.otherWorldSpawn = new Location(w, config.getInt("structures.locations.otherworldspawn.locX"), config.getInt("structures.locations.otherworldspawn.locY"), config.getInt("structures.locations.otherworldspawn.locZ"));
        pl.normalSpawn = new Location(w, config.getInt("structures.locations.normalspawn.locX"), config.getInt("structures.locations.normalspawn.locY"), config.getInt("structures.locations.normalspawn.locZ"));
        pl.redDeploy = new Location(w, config.getInt("structures.locations.redspawn.locX"), config.getInt("structures.locations.redspawn.locY"), config.getInt("structures.locations.redspawn.locZ"));
        pl.blueDeploy = new Location(w, config.getInt("structures.locations.bluespawn.locX"), config.getInt("structures.locations.bluespawn.locY"), config.getInt("structures.locations.bluespawn.locZ"));
        pl.specDeploy = new Location(w, config.getInt("structures.locations.spectator.locX"), config.getInt("structures.locations.spectator.locY"), config.getInt("structures.locations.spectator.locZ"));
        pl.debug("setted deploys");
        for (String keys : config.getConfigurationSection("structures.towers").getKeys(false)) {
            Structure t = new Structure(pl, getLoc(w, "structures.towers." + keys, config), getTpLoc(w, "structures.towers." + keys, config), config.getString("structures.towers." + keys + ".name"), config.getString("structures.towers." + keys + ".lane"), config.getString("structures.towers." + keys + ".team"), 1, getAfterDestroyLoc(w, "structures.towers." + keys, config));
            pl.structures.put(t, config.getInt("structures.towers." + keys + ".number"));
        }
        for (String keys : config.getConfigurationSection("structures.ancients").getKeys(false)) {
            Structure t = new Structure(pl, getLoc(w, "structures.ancients." + keys, config), null, config.getString("structures.ancients." + keys + ".name"), config.getString("structures.ancients." + keys + ".lane"), config.getString("structures.ancients." + keys + ".team"), 2, null);
            pl.structures.put(t, config.getInt("structures.ancients." + keys + ".number"));
        }
        pl.debug("setted structures");
        for (Structure t : pl.structures.keySet()) {
            int n = pl.structures.get(t);
            if (n < 0) {
                pl.getLogger().log(Level.WARNING, "Tower/Ancient number cant be lower than 0");
                pl.getPluginLoader().disablePlugin(pl);
            }
            if (n > pl.maxN) {
                pl.maxN = n;
                pl.debug(Integer.toString(pl.maxN));
            } else if (n < pl.minN) {
                pl.minN = n;
            }
        }
        for (Structure s : pl.structures.keySet()) {
            int deltaN = (pl.maxN - pl.structures.get(s)); // there are 3 towers, first = 1, then, 3 - 1 = 2 times *0.75
            if (deltaN == 0) {
                s.setHP(1);
            } else {
                s.setHP(deltaN);
            }
        }
        pl.debug("setted structures HP");
        pl.jungleRedSpawn.add(new Location(w, -904, 11, 164));
        pl.jungleRedSpawn.add(new Location(w, -913, 11, 34));
        pl.jungleBlueSpawn.add(new Location(w, -850, 11, 111));
        pl.jungleBlueSpawn.add(new Location(w, -967, 11, 87));
        pl.jungleSpawn.add(new Location(w, -845, 11, 81));
        pl.jungleSpawn.add(new Location(w, -901, 11, 68));
        pl.jungleSpawn.add(new Location(w, -933, 11, 18));
        pl.jungleSpawn.add(new Location(w, -884, 11, 181));
        pl.jungleSpawn.add(new Location(w, -916, 11, 130));
        pl.jungleSpawn.add(new Location(w, -963, 11, 116));

        for (String keys : config.getConfigurationSection("structures.creepspawn.top").getKeys(false)) {
            pl.addCreepLocSpawn("top", new Location(w, config.getInt("structures.creepspawn.top." + keys + ".locX"), config.getInt("structures.creepspawn.top." + keys + ".locY"), config.getInt("structures.creepspawn.top." + keys + ".locZ")));
        }
        pl.debug("setted top creepspawn");
        for (String keys : config.getConfigurationSection("structures.creepspawn.mid").getKeys(false)) {
            pl.addCreepLocSpawn("mid", new Location(w, config.getInt("structures.creepspawn.mid." + keys + ".locX"), config.getInt("structures.creepspawn.mid." + keys + ".locY"), config.getInt("structures.creepspawn.mid." + keys + ".locZ")));
        }
        pl.debug("setted mid creepspawn");
        for (String keys : config.getConfigurationSection("structures.creepspawn.bot").getKeys(false)) {
            pl.addCreepLocSpawn("bot", new Location(w, config.getInt("structures.creepspawn.bot." + keys + ".locX"), config.getInt("structures.creepspawn.bot." + keys + ".locY"), config.getInt("structures.creepspawn.bot." + keys + ".locZ")));
        }
        pl.debug("setted bot creepspawn");
        for (int a = -config.getInt("config.world.fromChunkX"); a <= config.getInt("config.world.toChunkX"); a++) {
            for (int b = -config.getInt("config.world.fromChunkY"); b <= config.getInt("config.world.toChunkY"); b++) {
                w.loadChunk(a, b);
            }
        }
    }

    private Location getLoc(World w, String path, FileConfiguration config) {
        return new Location(w, config.getInt(path + ".locX"), config.getInt(path + ".locY"), config.getInt(path + ".locZ"));
    }

    private Location getTpLoc(World w, String path, FileConfiguration config) {
        return new Location(w, config.getInt(path + ".tpLocX"), config.getInt(path + ".tpLocY"), config.getInt(path + ".tpLocZ"));
    }

    private Location getAfterDestroyLoc(World w, String path, FileConfiguration config) {
        try {
            return new Location(w, config.getInt(path + ".afterDestroyLocX"), config.getInt(path + ".afterDestroyLocY"), config.getInt(path + ".afterDestroyLocZ"));
        } catch (NullPointerException e) {
            return null;
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

    private void setupLang(FileConfiguration config) {
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
        config.addDefault("lang.youMustDestroyFirstTowers", "&cYou must destroy towers behind you first.");
        config.addDefault("lang.youDamagedTower", "&6You caused &e15 damage&6 on %tower &6(%hp&6)");
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
        langYML.saveCustomConfig();
    }

    private void setupStructures(FileConfiguration config) {
        if (!enabled) {
            config.addDefault("structures.locations.otherworldspawn.locX", 5);
            config.addDefault("structures.locations.otherworldspawn.locY", 5);
            config.addDefault("structures.locations.otherworldspawn.locZ", 5);
            config.addDefault("structures.locations.normalspawn.locX", -817);
            config.addDefault("structures.locations.normalspawn.locY", 5);
            config.addDefault("structures.locations.normalspawn.locZ", -29);
            config.addDefault("structures.locations.spectator.locX", -908);
            config.addDefault("structures.locations.spectator.locY", 25);
            config.addDefault("structures.locations.spectator.locZ", 98);
            config.addDefault("structures.locations.redspawn.locX", -1011);
            config.addDefault("structures.locations.redspawn.locY", 14);
            config.addDefault("structures.locations.redspawn.locZ", 203);
            config.addDefault("structures.locations.bluespawn.locX", -806);
            config.addDefault("structures.locations.bluespawn.locY", 14);
            config.addDefault("structures.locations.bluespawn.locZ", -4);

            config.addDefault("structures.creepspawn.bot.s1.locX", -997);
            config.addDefault("structures.creepspawn.bot.s1.locY", 11);
            config.addDefault("structures.creepspawn.bot.s1.locZ", 10);
            config.addDefault("structures.creepspawn.mid.s1.locX", -907);
            config.addDefault("structures.creepspawn.mid.s1.locY", 11);
            config.addDefault("structures.creepspawn.mid.s1.locZ", 98);
            config.addDefault("structures.creepspawn.top.s1.locX", -821);
            config.addDefault("structures.creepspawn.top.s1.locY", 11);
            config.addDefault("structures.creepspawn.top.s1.locZ", 187);

            /*
             BLUE
             */
            config.addDefault("structures.towers.btower1.name", "Blue Left Ancient Tower");
            config.addDefault("structures.towers.btower1.locX", -823);
            config.addDefault("structures.towers.btower1.locY", 16);
            config.addDefault("structures.towers.btower1.locZ", 17);
            config.addDefault("structures.towers.btower1.tpLocX", -825);
            config.addDefault("structures.towers.btower1.tpLocY", 12);
            config.addDefault("structures.towers.btower1.tpLocZ", 19);
            config.addDefault("structures.towers.btower1.lane", "mid");
            config.addDefault("structures.towers.btower1.team", "blue");
            config.addDefault("structures.towers.btower1.number", 4);

            config.addDefault("structures.towers.btower2.name", "Blue Right Ancient Tower");
            config.addDefault("structures.towers.btower2.locX", -828);
            config.addDefault("structures.towers.btower2.locY", 16);
            config.addDefault("structures.towers.btower2.locZ", 12);
            config.addDefault("structures.towers.btower2.tpLocX", -830);
            config.addDefault("structures.towers.btower2.tpLocY", 12);
            config.addDefault("structures.towers.btower2.tpLocZ", 14);
            config.addDefault("structures.towers.btower2.lane", "mid");
            config.addDefault("structures.towers.btower2.team", "blue");
            config.addDefault("structures.towers.btower2.number", 4);

            config.addDefault("structures.towers.btower1m.name", "Blue Mid Third Tower");
            config.addDefault("structures.towers.btower1m.locX", -852);
            config.addDefault("structures.towers.btower1m.locY", 16);
            config.addDefault("structures.towers.btower1m.locZ", 41);
            config.addDefault("structures.towers.btower1m.tpLocX", -854);
            config.addDefault("structures.towers.btower1m.tpLocY", 12);
            config.addDefault("structures.towers.btower1m.tpLocZ", 43);
            config.addDefault("structures.towers.btower1m.afterDestroyLocX", -832);
            config.addDefault("structures.towers.btower1m.afterDestroyLocY", 12);
            config.addDefault("structures.towers.btower1m.afterDestroyLocZ", 20);
            config.addDefault("structures.towers.btower1m.lane", "mid");
            config.addDefault("structures.towers.btower1m.team", "blue");
            config.addDefault("structures.towers.btower1m.number", 3);

            config.addDefault("structures.towers.btower2m.name", "Blue Mid Second Tower");
            config.addDefault("structures.towers.btower2m.locX", -871);
            config.addDefault("structures.towers.btower2m.locY", 15);
            config.addDefault("structures.towers.btower2m.locZ", 55);
            config.addDefault("structures.towers.btower2m.tpLocX", -870);
            config.addDefault("structures.towers.btower2m.tpLocY", 11);
            config.addDefault("structures.towers.btower2m.tpLocZ", 54);
            config.addDefault("structures.towers.btower2m.afterDestroyLocX", -860);
            config.addDefault("structures.towers.btower2m.afterDestroyLocY", 11);
            config.addDefault("structures.towers.btower2m.afterDestroyLocZ", 49);
            config.addDefault("structures.towers.btower2m.lane", "mid");
            config.addDefault("structures.towers.btower2m.team", "blue");
            config.addDefault("structures.towers.btower2m.number", 2);

            config.addDefault("structures.towers.btower3m.name", "Blue Mid First Tower");
            config.addDefault("structures.towers.btower3m.locX", -890);
            config.addDefault("structures.towers.btower3m.locY", 15);
            config.addDefault("structures.towers.btower3m.locZ", 84);
            config.addDefault("structures.towers.btower3m.tpLocX", -889);
            config.addDefault("structures.towers.btower3m.tpLocY", 11);
            config.addDefault("structures.towers.btower3m.tpLocZ", 83);
            config.addDefault("structures.towers.btower3m.afterDestroyLocX", -877);
            config.addDefault("structures.towers.btower3m.afterDestroyLocY", 11);
            config.addDefault("structures.towers.btower3m.afterDestroyLocZ", 65);
            config.addDefault("structures.towers.btower3m.lane", "mid");
            config.addDefault("structures.towers.btower3m.team", "blue");
            config.addDefault("structures.towers.btower3m.number", 1);

            config.addDefault("structures.towers.btower1t.name", "Blue Top Third Tower");
            config.addDefault("structures.towers.btower1t.locX", -811);
            config.addDefault("structures.towers.btower1t.locY", 16);
            config.addDefault("structures.towers.btower1t.locZ", 63);
            config.addDefault("structures.towers.btower1t.tpLocX", -811);
            config.addDefault("structures.towers.btower1t.tpLocY", 12);
            config.addDefault("structures.towers.btower1t.tpLocZ", 65);
            config.addDefault("structures.towers.btower1t.afterDestroyLocX", -820);
            config.addDefault("structures.towers.btower1t.afterDestroyLocY", 12);
            config.addDefault("structures.towers.btower1t.afterDestroyLocZ", 29);
            config.addDefault("structures.towers.btower1t.lane", "top");
            config.addDefault("structures.towers.btower1t.team", "blue");
            config.addDefault("structures.towers.btower1t.number", 3);

            config.addDefault("structures.towers.btower2t.name", "Blue Top Second Tower");
            config.addDefault("structures.towers.btower2t.locX", -813);
            config.addDefault("structures.towers.btower2t.locY", 15);
            config.addDefault("structures.towers.btower2t.locZ", 91);
            config.addDefault("structures.towers.btower2t.tpLocX", -813);
            config.addDefault("structures.towers.btower2t.tpLocY", 11);
            config.addDefault("structures.towers.btower2t.tpLocZ", 89);
            config.addDefault("structures.towers.btower2t.afterDestroyLocX", -810);
            config.addDefault("structures.towers.btower2t.afterDestroyLocY", 11);
            config.addDefault("structures.towers.btower2t.afterDestroyLocZ", 73);
            config.addDefault("structures.towers.btower2t.lane", "top");
            config.addDefault("structures.towers.btower2t.team", "blue");
            config.addDefault("structures.towers.btower2t.number", 2);

            config.addDefault("structures.towers.btower3t.name", "Blue Top First Tower");
            config.addDefault("structures.towers.btower3t.locX", -808);
            config.addDefault("structures.towers.btower3t.locY", 15);
            config.addDefault("structures.towers.btower3t.locZ", 159);
            config.addDefault("structures.towers.btower3t.tpLocX", -808);
            config.addDefault("structures.towers.btower3t.tpLocY", 11);
            config.addDefault("structures.towers.btower3t.tpLocZ", 157);
            config.addDefault("structures.towers.btower3t.afterDestroyLocX", -810);
            config.addDefault("structures.towers.btower3t.afterDestroyLocY", 11);
            config.addDefault("structures.towers.btower3t.afterDestroyLocZ", 128);
            config.addDefault("structures.towers.btower3t.lane", "top");
            config.addDefault("structures.towers.btower3t.team", "blue");
            config.addDefault("structures.towers.btower3t.number", 1);

            config.addDefault("structures.towers.btower1b.name", "Blue Bot Third Tower");
            config.addDefault("structures.towers.btower1b.locX", -874);
            config.addDefault("structures.towers.btower1b.locY", 16);
            config.addDefault("structures.towers.btower1b.locZ", 0);
            config.addDefault("structures.towers.btower1b.tpLocX", -876);
            config.addDefault("structures.towers.btower1b.tpLocY", 12);
            config.addDefault("structures.towers.btower1b.tpLocZ", 0);
            config.addDefault("structures.towers.btower1b.afterDestroyLocX", -838);
            config.addDefault("structures.towers.btower1b.afterDestroyLocY", 12);
            config.addDefault("structures.towers.btower1b.afterDestroyLocZ", 10);
            config.addDefault("structures.towers.btower1b.lane", "bot");
            config.addDefault("structures.towers.btower1b.team", "blue");
            config.addDefault("structures.towers.btower1b.number", 3);

            config.addDefault("structures.towers.btower2b.name", "Blue Bot Second Tower");
            config.addDefault("structures.towers.btower2b.locX", -902);
            config.addDefault("structures.towers.btower2b.locY", 15);
            config.addDefault("structures.towers.btower2b.locZ", 2);
            config.addDefault("structures.towers.btower2b.tpLocX", -900);
            config.addDefault("structures.towers.btower2b.tpLocY", 11);
            config.addDefault("structures.towers.btower2b.tpLocZ", 2);
            config.addDefault("structures.towers.btower2b.afterDestroyLocX", -884);
            config.addDefault("structures.towers.btower2b.afterDestroyLocY", 11);
            config.addDefault("structures.towers.btower2b.afterDestroyLocZ", 0);
            config.addDefault("structures.towers.btower2b.lane", "bot");
            config.addDefault("structures.towers.btower2b.team", "blue");
            config.addDefault("structures.towers.btower2b.number", 2);

            config.addDefault("structures.towers.btower3b.name", "Blue Bot First Tower");
            config.addDefault("structures.towers.btower3b.locX", -970);
            config.addDefault("structures.towers.btower3b.locY", 15);
            config.addDefault("structures.towers.btower3b.locZ", -2);
            config.addDefault("structures.towers.btower3b.tpLocX", -968);
            config.addDefault("structures.towers.btower3b.tpLocY", 11);
            config.addDefault("structures.towers.btower3b.tpLocZ", -2);
            config.addDefault("structures.towers.btower3b.afterDestroyLocX", -934);
            config.addDefault("structures.towers.btower3b.afterDestroyLocY", 11);
            config.addDefault("structures.towers.btower3b.afterDestroyLocZ", 0);
            config.addDefault("structures.towers.btower3b.lane", "bot");
            config.addDefault("structures.towers.btower3b.team", "blue");
            config.addDefault("structures.towers.btower3b.number", 1);

            /*
             RED
             */
            config.addDefault("structures.towers.rtower1.name", "Red Left Ancient Tower");
            config.addDefault("structures.towers.rtower1.locX", -994);
            config.addDefault("structures.towers.rtower1.locY", 16);
            config.addDefault("structures.towers.rtower1.locZ", 181);
            config.addDefault("structures.towers.rtower1.tpLocX", -992);
            config.addDefault("structures.towers.rtower1.tpLocY", 12);
            config.addDefault("structures.towers.rtower1.tpLocZ", 179);
            config.addDefault("structures.towers.rtower1.lane", "mid");
            config.addDefault("structures.towers.rtower1.team", "red");
            config.addDefault("structures.towers.rtower1.number", 4);

            config.addDefault("structures.towers.rtower2.name", "Red Right Ancient Tower");
            config.addDefault("structures.towers.rtower2.locX", -989);
            config.addDefault("structures.towers.rtower2.locY", 16);
            config.addDefault("structures.towers.rtower2.locZ", 189);
            config.addDefault("structures.towers.rtower2.tpLocX", -987);
            config.addDefault("structures.towers.rtower2.tpLocY", 12);
            config.addDefault("structures.towers.rtower2.tpLocZ", 184);
            config.addDefault("structures.towers.rtower2.lane", "mid");
            config.addDefault("structures.towers.rtower2.team", "red");
            config.addDefault("structures.towers.rtower2.number", 4);

            config.addDefault("structures.towers.rtower1m.name", "Red Mid Third Tower");
            config.addDefault("structures.towers.rtower1m.locX", -965);
            config.addDefault("structures.towers.rtower1m.locY", 16);
            config.addDefault("structures.towers.rtower1m.locZ", 157);
            config.addDefault("structures.towers.rtower1m.tpLocX", -963);
            config.addDefault("structures.towers.rtower1m.tpLocY", 12);
            config.addDefault("structures.towers.rtower1m.tpLocZ", 155);
            config.addDefault("structures.towers.rtower1m.afterDestroyLocX", -984);
            config.addDefault("structures.towers.rtower1m.afterDestroyLocY", 12);
            config.addDefault("structures.towers.rtower1m.afterDestroyLocZ", 177);
            config.addDefault("structures.towers.rtower1m.lane", "mid");
            config.addDefault("structures.towers.rtower1m.team", "red");
            config.addDefault("structures.towers.rtower1m.number", 3);

            config.addDefault("structures.towers.rtower2m.name", "Red Mid Second Tower");
            config.addDefault("structures.towers.rtower2m.locX", -946);
            config.addDefault("structures.towers.rtower2m.locY", 15);
            config.addDefault("structures.towers.rtower2m.locZ", 143);
            config.addDefault("structures.towers.rtower2m.tpLocX", -947);
            config.addDefault("structures.towers.rtower2m.tpLocY", 11);
            config.addDefault("structures.towers.rtower2m.tpLocZ", 144);
            config.addDefault("structures.towers.rtower2m.afterDestroyLocX", -957);
            config.addDefault("structures.towers.rtower2m.afterDestroyLocY", 11);
            config.addDefault("structures.towers.rtower2m.afterDestroyLocZ", 148);
            config.addDefault("structures.towers.rtower2m.lane", "mid");
            config.addDefault("structures.towers.rtower2m.team", "red");
            config.addDefault("structures.towers.rtower2m.number", 2);

            config.addDefault("structures.towers.rtower3m.name", "Red Mid First Tower");
            config.addDefault("structures.towers.rtower3m.locX", -927);
            config.addDefault("structures.towers.rtower3m.locY", 15);
            config.addDefault("structures.towers.rtower3m.locZ", 114);
            config.addDefault("structures.towers.rtower3m.tpLocX", -928);
            config.addDefault("structures.towers.rtower3m.tpLocY", 11);
            config.addDefault("structures.towers.rtower3m.tpLocZ", 115);
            config.addDefault("structures.towers.rtower3m.afterDestroyLocX", -940);
            config.addDefault("structures.towers.rtower3m.afterDestroyLocY", 11);
            config.addDefault("structures.towers.rtower3m.afterDestroyLocZ", 132);
            config.addDefault("structures.towers.rtower3m.lane", "mid");
            config.addDefault("structures.towers.rtower3m.team", "red");
            config.addDefault("structures.towers.rtower3m.number", 1);

            config.addDefault("structures.towers.rtower1t.name", "Red Top Third Tower");
            config.addDefault("structures.towers.rtower1t.locX", -943);
            config.addDefault("structures.towers.rtower1t.locY", 16);
            config.addDefault("structures.towers.rtower1t.locZ", 198);
            config.addDefault("structures.towers.rtower1t.tpLocX", -941);
            config.addDefault("structures.towers.rtower1t.tpLocY", 12);
            config.addDefault("structures.towers.rtower1t.tpLocZ", 198);
            config.addDefault("structures.towers.rtower1t.afterDestroyLocX", -981);
            config.addDefault("structures.towers.rtower1t.afterDestroyLocY", 12);
            config.addDefault("structures.towers.rtower1t.afterDestroyLocZ", 186);
            config.addDefault("structures.towers.rtower1t.lane", "top");
            config.addDefault("structures.towers.rtower1t.team", "red");
            config.addDefault("structures.towers.rtower1t.number", 3);

            config.addDefault("structures.towers.rtower2t.name", "Red Top Second Tower");
            config.addDefault("structures.towers.rtower2t.locX", -915);
            config.addDefault("structures.towers.rtower2t.locY", 15);
            config.addDefault("structures.towers.rtower2t.locZ", 196);
            config.addDefault("structures.towers.rtower2t.tpLocX", -917);
            config.addDefault("structures.towers.rtower2t.tpLocY", 11);
            config.addDefault("structures.towers.rtower2t.tpLocZ", 196);
            config.addDefault("structures.towers.rtower2t.afterDestroyLocX", -932);
            config.addDefault("structures.towers.rtower2t.afterDestroyLocY", 11);
            config.addDefault("structures.towers.rtower2t.afterDestroyLocZ", 199);
            config.addDefault("structures.towers.rtower2t.lane", "top");
            config.addDefault("structures.towers.rtower2t.team", "red");
            config.addDefault("structures.towers.rtower2t.number", 2);

            config.addDefault("structures.towers.rtower3t.name", "Red Top First Tower");
            config.addDefault("structures.towers.rtower3t.locX", -847);
            config.addDefault("structures.towers.rtower3t.locY", 15);
            config.addDefault("structures.towers.rtower3t.locZ", 201);
            config.addDefault("structures.towers.rtower3t.tpLocX", -849);
            config.addDefault("structures.towers.rtower3t.tpLocY", 11);
            config.addDefault("structures.towers.rtower3t.tpLocZ", 201);
            config.addDefault("structures.towers.rtower3t.afterDestroyLocX", -885);
            config.addDefault("structures.towers.rtower3t.afterDestroyLocY", 11);
            config.addDefault("structures.towers.rtower3t.afterDestroyLocZ", 198);
            config.addDefault("structures.towers.rtower3t.lane", "top");
            config.addDefault("structures.towers.rtower3t.team", "red");
            config.addDefault("structures.towers.rtower3t.number", 1);

            config.addDefault("structures.towers.rtower1b.name", "Red Bot Third Tower");
            config.addDefault("structures.towers.rtower1b.locX", -1006);
            config.addDefault("structures.towers.rtower1b.locY", 16);
            config.addDefault("structures.towers.rtower1b.locZ", 135);
            config.addDefault("structures.towers.rtower1b.tpLocX", -1006);
            config.addDefault("structures.towers.rtower1b.tpLocY", 12);
            config.addDefault("structures.towers.rtower1b.tpLocZ", 133);
            config.addDefault("structures.towers.rtower1b.afterDestroyLocX", -993);
            config.addDefault("structures.towers.rtower1b.afterDestroyLocY", 12);
            config.addDefault("structures.towers.rtower1b.afterDestroyLocZ", 172);
            config.addDefault("structures.towers.rtower1b.lane", "bot");
            config.addDefault("structures.towers.rtower1b.team", "red");
            config.addDefault("structures.towers.rtower1b.number", 3);

            config.addDefault("structures.towers.rtower2b.name", "Red Bot Second Tower");
            config.addDefault("structures.towers.rtower2b.locX", -1004);
            config.addDefault("structures.towers.rtower2b.locY", 15);
            config.addDefault("structures.towers.rtower2b.locZ", 107);
            config.addDefault("structures.towers.rtower2b.tpLocX", -1004);
            config.addDefault("structures.towers.rtower2b.tpLocY", 11);
            config.addDefault("structures.towers.rtower2b.tpLocZ", 109);
            config.addDefault("structures.towers.rtower2b.afterDestroyLocX", -1006);
            config.addDefault("structures.towers.rtower2b.afterDestroyLocY", 11);
            config.addDefault("structures.towers.rtower2b.afterDestroyLocZ", 125);
            config.addDefault("structures.towers.rtower2b.lane", "bot");
            config.addDefault("structures.towers.rtower2b.team", "red");
            config.addDefault("structures.towers.rtower2b.number", 2);

            config.addDefault("structures.towers.rtower3b.name", "Red Bot First Tower");
            config.addDefault("structures.towers.rtower3b.locX", -1009);
            config.addDefault("structures.towers.rtower3b.locY", 15);
            config.addDefault("structures.towers.rtower3b.locZ", 39);
            config.addDefault("structures.towers.rtower3b.tpLocX", -1009);
            config.addDefault("structures.towers.rtower3b.tpLocY", 11);
            config.addDefault("structures.towers.rtower3b.tpLocZ", 41);
            config.addDefault("structures.towers.rtower3b.afterDestroyLocX", -1006);
            config.addDefault("structures.towers.rtower3b.afterDestroyLocY", 11);
            config.addDefault("structures.towers.rtower3b.afterDestroyLocZ", 76);
            config.addDefault("structures.towers.rtower3b.lane", "bot");
            config.addDefault("structures.towers.rtower3b.team", "red");
            config.addDefault("structures.towers.rtower3b.number", 1);

            config.addDefault("structures.ancients.ancientb.name", "Blue Ancient");
            config.addDefault("structures.ancients.ancientb.locX", -820);
            config.addDefault("structures.ancients.ancientb.locY", 15);
            config.addDefault("structures.ancients.ancientb.locZ", 9);
            config.addDefault("structures.ancients.ancientb.lane", "mid");
            config.addDefault("structures.ancients.ancientb.team", "blue");
            config.addDefault("structures.ancients.ancientb.number", 5);

            config.addDefault("structures.ancients.ancientr.name", "Red Ancient");
            config.addDefault("structures.ancients.ancientr.locX", -997);
            config.addDefault("structures.ancients.ancientr.locY", 15);
            config.addDefault("structures.ancients.ancientr.locZ", 189);
            config.addDefault("structures.ancients.ancientr.lane", "mid");
            config.addDefault("structures.ancients.ancientr.team", "red");
            config.addDefault("structures.ancients.ancientr.number", 5);
            structuresYML.saveCustomConfig();
            pl.getLogger().log(Level.INFO, "Plugin configured to edited Duurax's LOL Map Rev 1.");
        }
    }
}
