package com.jabyftw.dotamine;

import com.jabyftw.dotamine.listeners.PlayerListener;
import com.jabyftw.dotamine.commands.DotaCommand;
import com.jabyftw.dotamine.commands.JoinCommand;
import com.jabyftw.dotamine.commands.SpectateCommand;
import com.jabyftw.dotamine.listeners.BlockListener;
import com.jabyftw.dotamine.listeners.EntityListener;
import com.jabyftw.dotamine.runnables.AnnounceGameRunnable;
import com.jabyftw.dotamine.runnables.AnnounceQueueRunnable;
import com.jabyftw.dotamine.runnables.CreepSpawnRunnable;
import com.jabyftw.dotamine.runnables.CheckNightRunnable;
import com.jabyftw.dotamine.runnables.JungleSpawnRunnable;
import com.jabyftw.dotamine.runnables.RankingUpdateRunnable;
import com.jabyftw.dotamine.runnables.ScoreboardRunnable;
import com.jabyftw.dotamine.runnables.StopRunnable;
import com.jabyftw.dotamine.runnables.item.TarrasqueRemRDRunnable;
import com.jabyftw.dotamine.runnables.item.TarrasqueRunRunnable;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackMelee;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackRanged;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIFloat;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AILookAtEntity;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIRandomLookaround;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AITargetHurtBy;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AITargetNearest;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.AttributeModifierFactory;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.ModifyOperation;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Rafael
 */
public class DotaMine extends JavaPlugin implements Listener {

    public MySQL sql;
    public String worldName;
    /*
     MOTD
     */
    public int WAITING = 0;
    public int WAITING_QUEUE = 1;
    public int SPAWNING = 2;
    public int PLAYING = 3;
    public int RESTARTING = 4;
    /*
     ITEM
     */
    public int SHADOW_BLADE = 1;
    public int FORCE_STAFF = 2;
    public int TP_SCROLL = 3;
    public int TARRASQUE = 4;
    public int SMOKE = 5;
    /*
     PLUGIN
     */
    public int redCount, blueCount, state, targetRunnable, scoreRunnable, version, MIN_PLAYERS, MAX_PLAYERS, announceQueue;
    public boolean useVault, nerfRanged, mysqlEnabled, useEffects, megaCreeps;
    public Economy econ = null;
    public Permission permission = null;
    public FileConfiguration config;
    public List<Ranking> rankingList = new ArrayList();
    public Map<Player, Jogador> ingameList = new HashMap();
    public Map<Location, Tower> towers = new HashMap();
    public Map<Location, Location> tpPlace = new HashMap();
    public Map<Player, ItemStack[]> playerDeathItems = new HashMap();
    public Map<Player, ItemStack[]> playerDeathArmor = new HashMap();
    public List<Location> botCreepSpawn = new ArrayList();
    public List<Location> midCreepSpawn = new ArrayList();
    public List<Location> topCreepSpawn = new ArrayList();
    public List<Location> jungleSpawn = new ArrayList();
    public Map<Player, Integer> queue = new HashMap();
    public Map<Player, Spectator> spectators = new HashMap();
    public List<ControllableMob> controlMobs = new ArrayList();
    public List<ControllableMob> jungleCreeps = new ArrayList();
    public List<ControllableMob> laneCreeps = new ArrayList();
    public Location blueDeploy, redDeploy, normalSpawn, specDeploy, blueAncient, redAncient;
    public Location blueFBotT, blueFMidT, blueFTopT, redFBotT, redFMidT, redFTopT;
    public Location blueSBotT, blueSMidT, blueSTopT, redSBotT, redSMidT, redSTopT;
    public Location blueJungleBot, blueJungleTop, redJungleBot, redJungleTop;
    public Location botSpawnPre, botSpawnPosR, botSpawnPosB;
    public Location midSpawnPre, midSpawnPosR, midSpawnPosB;
    public Location topSpawnPre, topSpawnPosR, topSpawnPosB;
    public Random random = new Random();
    /*
     ITEM
     */
    public List<Player> shadowCD = new ArrayList();
    public Map<Player, BukkitTask> invisibleSB = new HashMap();
    public List<Player> forceCD = new ArrayList();
    public Map<Player, Integer> forcingStaff = new HashMap();
    public List<Player> tpCD = new ArrayList();
    public Map<Player, Integer> teleportingE = new HashMap();
    public Map<Player, Integer> teleportingT = new HashMap();
    public List<Player> teleportingJ = new ArrayList();
    public List<Player> hasTarrasque = new ArrayList();
    public List<Player> tarrasqueRecentlyDamaged = new ArrayList();

    @Override
    public void onEnable() {
        state = WAITING;
        version = 2; // config version
        megaCreeps = false;
        config = getConfig();
        generateConfig();
        setLocations(getServer().getWorld(worldName));
        if (useVault) {
            setupEconomy();
            setupPermissions();
        }
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getLogger().log(Level.INFO, "Registered listeners.");
        getServer().getPluginCommand("join").setExecutor(new JoinCommand(this));
        getServer().getPluginCommand("spectate").setExecutor(new SpectateCommand(this));
        getServer().getPluginCommand("dota").setExecutor(new DotaCommand(this));
        getLogger().log(Level.INFO, "Registered commands.");
        announceQueue = getServer().getScheduler().scheduleSyncRepeatingTask(this, new AnnounceQueueRunnable(this), 20 * 15, 20 * 20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new TarrasqueRunRunnable(this), 20 * 2, 20 * 2);
        if (mysqlEnabled) {
            getServer().getScheduler().scheduleAsyncRepeatingTask(this, new RankingUpdateRunnable(this), 10, 20 * 60);
        }
        getLogger().log(Level.INFO, "Registered runnables.");
        getLogger().log(Level.WARNING, "Plugin configured to rfsantos1996's Dota Map Rev 1.");
    }

    @Override
    public void onDisable() {
        for (int a = -11; a <= 9; a++) {
            for (int b = -10; b <= 10; b++) {
                getServer().getWorld(worldName).unloadChunk(a, b, false, false);
            }
        }
        getServer().unloadWorld(worldName, false);
        if (mysqlEnabled) {
            sql.closeConn();
        }
    }

    private void generateConfig() {
        //config.addDefault("config.", value);
        config.addDefault("config.useVault", false);
        config.addDefault("config.useEffects", true);
        config.addDefault("config.nerfRangedAtNight", false);
        config.addDefault("config.worldName", "world");
        config.addDefault("config.targetRunnableDelayInTicks", 30);
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
        config.addDefault("lang.noPermission", "&cNo permission!");
        config.addDefault("lang.joinMessage", "&7+ %name");
        config.addDefault("lang.quitMessage", "&7- %name");
        config.addDefault("lang.onlyIngame", "&4You are not a player!");
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
        config.addDefault("lang.youLoseXMoney", "&cYou lose &4%money&c for dying.");
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
        config.addDefault("lang.queueSizeIs", "&e%size&6 in queue! &cWe need %needed player(s).");
        config.addDefault("lang.onePlayerLeft", "&cThere's only 1 player left. &4Restarting server...");
        config.addDefault("lang.lowRangedNightVision", "&cRanged players have low night vision. &4ATENTION AT NIGHT!");
        config.addDefault("lang.alreadyInQueueUpdatedAttack", "&cAlready on queue. &6New attack type: %attack");
        config.addDefault("lang.noRankingFound", "&cNo ranking entry.");
        config.addDefault("lang.startingIn2Minutes", "&cStarting game in 2 minutes. &6Last chance to use &4/join");
        config.addDefault("lang.couldntStartGame", "&4Couldn't start game! &cTrying again...");
        config.addDefault("lang.itemCDMessage", "&4%item &cis on cooldown.");
        config.addDefault("lang.itemOverCDMessage", "&e%item &6is now usable.");
        config.addDefault("lang.shadowOverMessage", "&cYou are now visible.");
        config.addDefault("lang.itemUseMessage", "&6You used &e%item&6. It can be used again after %cd sec.");
        config.addDefault("lang.itemUseMessageDontCD", "&6You used &e%item&6.");
        config.addDefault("lang.hasTarrasque", "&6You now have &eHeart of Tarrasque&6. You will regen every 5 sec.");
        config.addDefault("lang.tarrasqueRemoved", "&cYou don't have &4Heart of Tarrasque&c.");
        config.addDefault("lang.alreadySpectating", "&cYou're already spectating");
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
        config.addDefault("DoNotChangeThis.ConfigVersion", version);
        config.options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        worldName = config.getString("config.worldName");
        useVault = config.getBoolean("config.useVault");
        useEffects = config.getBoolean("config.useEffects");
        targetRunnable = config.getInt("config.targetRunnableDelayInTicks");
        scoreRunnable = config.getInt("config.scoreRunnableDelayInTicks");
        MIN_PLAYERS = config.getInt("config.MIN_PLAYERS");
        MAX_PLAYERS = config.getInt("config.MAX_PLAYERS");
        nerfRanged = config.getBoolean("config.nerfRangedAtNight");
        mysqlEnabled = config.getBoolean("mysql.enabled");
        if (mysqlEnabled) {
            sql = new MySQL(this, config.getString("mysql.username"), config.getString("mysql.password"), "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getInt("mysql.port") + "/" + config.getString("mysql.database"));
            createTable();
        }
        if (config.getInt("DoNotChangeThis.ConfigVersion") != version) {
            getLogger().log(Level.WARNING, "Recommended: recreate your config.yml");
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
        return (econ != null);
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private void setLocations(World w) {
        w.setPVP(true);
        w.setAutoSave(false);
        w.setSpawnFlags(false, false);
        for (Entity e : w.getEntities()) {
            e.remove();
        }
        blueFMidT = new Location(w, 12, 33, -7);
        towers.put(blueFMidT, new Tower(blueFMidT, "Blue Mid First Tower", 1));
        tpPlace.put(blueFMidT, new Location(w, 15, 25, -11, 44, 2));
        blueFBotT = new Location(w, -57, 33, -85);
        towers.put(blueFBotT, new Tower(blueFBotT, "Blue Bot First Tower", 1));
        tpPlace.put(blueFBotT, new Location(w, -55, 25, -89, 47, 4));
        blueFTopT = new Location(w, 81, 33, 62);
        towers.put(blueFTopT, new Tower(blueFTopT, "Blue Top First Tower", 1));
        tpPlace.put(blueFTopT, new Location(w, 84, 25, 59, 44, 2));
        redFMidT = new Location(w, -7, 33, 7);
        towers.put(redFMidT, new Tower(redFMidT, "Red Mid First Tower", 2));
        tpPlace.put(redFMidT, new Location(w, -11, 25, 10, 226, 2));
        redFBotT = new Location(w, -82, 33, -65);
        towers.put(redFBotT, new Tower(redFBotT, "Red Bot First Tower", 2));
        tpPlace.put(redFBotT, new Location(w, -86, 25, -63, -133, 4));
        redFTopT = new Location(w, 52, 33, 87);
        towers.put(redFTopT, new Tower(redFTopT, "Red Top Fisrt Tower", 2));
        tpPlace.put(redFTopT, new Location(w, 49, 25, 90, 222, 6));

        blueSMidT = new Location(w, 50, 33, -42);
        towers.put(blueSMidT, new Tower(blueSMidT, "Blue Mid Second Tower", 1));
        tpPlace.put(blueSMidT, new Location(w, 53, 25, -46, 47, 4));
        blueSBotT = new Location(w, 30, 33, -83);
        towers.put(blueSBotT, new Tower(blueSBotT, "Blue Bot Second Tower", 1));
        tpPlace.put(blueSBotT, new Location(w, 33, 25, -87, -309, 4));
        blueSTopT = new Location(w, 82, 33, -17);
        towers.put(blueSTopT, new Tower(blueSTopT, "Blue Top Second Tower", 1));
        tpPlace.put(blueSTopT, new Location(w, 85, 25, -21, 47, 7));
        redSMidT = new Location(w, -49, 33, 51);
        towers.put(redSMidT, new Tower(redSMidT, "Red Mid Second Tower", 2));
        tpPlace.put(redSMidT, new Location(w, -53, 25, 54, 225, 3));
        redSBotT = new Location(w, -79, 33, -1);
        towers.put(redSBotT, new Tower(redSBotT, "Red Bot Second Tower", 2));
        tpPlace.put(redSBotT, new Location(w, -83, 25, 1, 226, 6));
        redSTopT = new Location(w, -10, 33, 87);
        towers.put(redSTopT, new Tower(redSTopT, "Red Top Second Tower", 2));
        tpPlace.put(redSTopT, new Location(w, -14, 25, 90, 225, 2));

        blueAncient = new Location(w, 79, 36, -77);
        towers.put(blueAncient, new Tower(blueAncient, "Blue Ancient", 1));
        redAncient = new Location(w, -72, 36, 79);
        towers.put(redAncient, new Tower(redAncient, "Red Ancient", 2));

        botSpawnPre = new Location(w, -75, 25, -81);
        botSpawnPosR = new Location(w, -79, 25, -25);
        botSpawnPosB = new Location(w, 6, 25, -83);
        botCreepSpawn.add(botSpawnPre);

        midSpawnPre = new Location(w, 4, 25, 0);
        midSpawnPosR = new Location(w, -35, 25, 37);
        midSpawnPosB = new Location(w, 36, 25, -27);
        midCreepSpawn.add(midSpawnPre);

        topSpawnPre = new Location(w, 74, 25, 80);
        topSpawnPosR = new Location(w, 11, 25, 87);
        topSpawnPosB = new Location(w, 81, 25, 2);
        topCreepSpawn.add(topSpawnPre);

        blueJungleBot = new Location(w, 11, 25, -37);
        jungleSpawn.add(blueJungleBot);
        blueJungleTop = new Location(w, 51, 25, 9);
        jungleSpawn.add(blueJungleTop);
        redJungleBot = new Location(w, -45, 25, -11);
        jungleSpawn.add(redJungleBot);
        redJungleTop = new Location(w, -2, 25, 58);
        jungleSpawn.add(redJungleTop);

        blueDeploy = new Location(w, 86, 28 + 1, -87, 37, 13);
        redDeploy = new Location(w, -80, 28 + 1, 87, -134, 16);
        specDeploy = new Location(w, 2, 33 + 1, 0);
        normalSpawn = new Location(w, 50, 8 + 1, -121, -86, 2);

        for (int a = -11; a <= 9; a++) {
            for (int b = -10; b <= 10; b++) {
                w.loadChunk(a, b);
            }
        }
    }

    public String getLang(String path) {
        return config.getString(path).replaceAll("&", "§");
    }

    public void broadcast(String msg) {
        for (Player p : getServer().getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }

    /*
    
     PLAYER THINGS
    
     */
    public void hidePlayer(Player hiding) {
        for (Player hideFrom : getServer().getOnlinePlayers()) {
            if (hideFrom.equals(hiding)) {
                continue;
            }
            if (hideFrom.canSee(hiding)) {
                hideFrom.hidePlayer(hiding);
            }
        }
    }

    public void showPlayer(Player showing) {
        for (Player showTo : getServer().getOnlinePlayers()) {
            if (showTo.equals(showing)) {
                continue;
            }
            if (!showTo.canSee(showing)) {
                showTo.showPlayer(showing);
            }
        }
    }

    public void cleanPlayer(Player p, boolean flying, boolean hide) {
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setLevel(0);
        p.setExp(0);
        p.setExhaustion(0);
        p.setGameMode(GameMode.SURVIVAL);
        p.setDisplayName(p.getName());
        p.setCustomNameVisible(false);
        for (PotionEffect pe : p.getActivePotionEffects()) {
            p.removePotionEffect(pe.getType());
        }
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setAllowFlight(flying);
        p.setFlying(flying);
        p.saveData();
        if (hide) {
            hidePlayer(p);
        } else {
            showPlayer(p);
        }
        if (useVault) {
            double balance = econ.getBalance(p.getName());
            if (balance != 300) {
                econ.withdrawPlayer(p.getName(), balance);
                econ.depositPlayer(p.getName(), 300);
            }
        }
    }

    private int getTeam() {
        if (redCount > blueCount) {
            return 1; // Blue
        } else {
            return 2; // Red
        }
    }

    public void addPlayerToGame(Player p, int attackType) {
        if (state == WAITING || state == WAITING_QUEUE) {
            cleanPlayer(p, false, false);
            p.setDisplayName(ChatColor.GREEN + p.getName());
            p.sendMessage(getLang("lang.waitingTheGame"));
            queue.put(p, attackType);
        } else if (state == SPAWNING) {
            addPlayer(p, attackType); // spawning
        } else {
            p.sendMessage(getLang("lang.waitingTheGame"));
        }
    }

    public void addPlayer(Player p, int attackType) {
        if (ingameList.size() < MAX_PLAYERS) {
            if (spectators.containsKey(p)) {
                spectators.remove(p);
            }
            cleanPlayer(p, false, false);
            int team = getTeam();
            if (team == 1) {
                p.setDisplayName(ChatColor.BLUE + p.getName());
                p.setCustomName(ChatColor.BLUE + p.getName());
                p.setCustomNameVisible(true);
                blueCount = blueCount + 1;
                p.teleport(blueDeploy);
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 11));
                p.sendMessage(getLang("lang.onBlueTeam"));
            } else {
                p.setDisplayName(ChatColor.RED + p.getName());
                p.setCustomName(ChatColor.RED + p.getName());
                p.setCustomNameVisible(true);
                redCount = redCount + 1;
                p.teleport(redDeploy);
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));
                p.sendMessage(getLang("lang.onRedTeam"));
            }
            if (attackType == 1) { // Meele
                ItemStack sword = new ItemStack(Material.WOOD_SWORD, 1);
                sword.addUnsafeEnchantment(Enchantment.DURABILITY, 1000); // 0.1% chance for losing durability when using
                ItemStack bow = new ItemStack(Material.BOW, 1);
                bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
                bow.addEnchantment(Enchantment.DURABILITY, 2);
                p.getInventory().addItem(sword);
                p.getInventory().addItem(bow);
                if (p.hasPermission("dotamine.ranged")) {
                    permission.playerRemove(p, "dotamine.ranged");
                }
                permission.playerAdd(p, "dotamine.meele");
            } else { // Ranged
                ItemStack bow = new ItemStack(Material.BOW, 1);
                bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
                bow.addUnsafeEnchantment(Enchantment.DURABILITY, 1000);
                p.getInventory().addItem(bow);
                p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
                if (p.hasPermission("dotamine.meele")) {
                    permission.playerRemove(p, "dotamine.meele");
                }
                permission.playerAdd(p, "dotamine.ranged");
            }
            ingameList.put(p, new Jogador(this, p, 0, 0, 0, 0, team, attackType));
        } else {
            if (p.hasPermission("dotamine.spectate")) {
                addSpectator(p);
            } else {
                p.sendMessage(getLang("config.gameIsFull"));
            }
        }
    }

    public void removePlayer(Player p) {
        cleanPlayer(p, false, false);
        p.teleport(normalSpawn);
        if (ingameList.get(p).getTeam() == 1) {
            blueCount = blueCount - 1;
        } else {
            redCount = redCount - 1;
        }
        ingameList.remove(p);
        if (queue.containsKey(p)) {
            queue.remove(p);
        }
    }

    public void addSpectator(Player p) {
        cleanPlayer(p, true, true);
        p.setDisplayName(ChatColor.GRAY + p.getName());
        p.setCustomName(ChatColor.GRAY + p.getName());
        p.setCustomNameVisible(true);
        p.teleport(specDeploy);
        p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1));
        p.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
        p.sendMessage(getLang("lang.joinedSpectator"));
        spectators.put(p, new Spectator(this, p));
    }

    public void removeSpectator(Player p) {
        cleanPlayer(p, false, false);
        p.teleport(normalSpawn);
        p.sendMessage(getLang("lang.leftSpectator"));
        spectators.remove(p);
    }

    public void startGame(boolean forced) {
        for (Player p : getServer().getOnlinePlayers()) {
            if (!queue.containsKey(p)) {
                p.sendMessage(getLang("lang.youCanJoin"));
            }
        }
        for (Player p : queue.keySet()) {
            addPlayer(p, queue.get(p));
            p.sendMessage(getLang("lang.startingIn60sec"));
        }
        queue.clear();
        state = SPAWNING;
        getServer().getScheduler().scheduleSyncDelayedTask(this, new AnnounceGameRunnable(this), 20 * 50);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new CreepSpawnRunnable(this), 20 * 60, 20 * 30);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new JungleSpawnRunnable(this), 20 * 60, 20 * 40);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new ScoreboardRunnable(this), 1, scoreRunnable);
        if (nerfRanged) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new CheckNightRunnable(this), 20, 20 * 3);
        }
        getServer().getWorld(worldName).setTime(0); // start as day
    }

    @EventHandler
    public void onPing(ServerListPingEvent e) {
        if (state == WAITING) {
            e.setMotd(getMotdMessage("WAITING"));
        } else if (state == WAITING_QUEUE) {
            e.setMotd(getMotdMessage("WAITING_QUEUE"));
        } else if (state == SPAWNING) {
            e.setMotd(getMotdMessage("SPAWNING"));
        } else if (state == PLAYING) {
            e.setMotd(getMotdMessage("PLAYING"));
        } else if (state == RESTARTING) {
            e.setMotd(getMotdMessage("RESTARTING"));
        }
    }

    public void endGame(int winTeam) {
        state = RESTARTING;
        for (Jogador j : ingameList.values()) {
            if (j.getTeam() == winTeam) {
                j.addWin();
            } else {
                j.addLose();
            }
        }
        getServer().setWhitelist(true);
        getServer().getScheduler().scheduleSyncDelayedTask(this, new StopRunnable(this), 20 * 10);
        for (Player p : getServer().getOnlinePlayers()) {
            p.kickPlayer(getLang("lang.kickMessage"));
        }
    }

    public void shutdownServer() {
        getServer().shutdown();
    }

    private double LknockM, LdmgM, LhealthM;
    private double LknockR, LdmgR, LhealthR;

    public void spawnLaneCreeps(Location spawnloc) {
        if (megaCreeps) {
            LknockM = 0.6;
            LknockR = 0.4;
            LdmgM = 3;
            LdmgR = 7;
            LhealthM = 6;
            LhealthR = 4;
        } else {
            LknockM = 0.4;
            LknockR = 0.2;
            LdmgM = 2;
            LdmgR = 4;
            LhealthM = 3;
            LhealthR = 2;
        }
        for (int i = 0; i < 4; i++) {
            Zombie z = getServer().getWorld(worldName).spawn(spawnloc, Zombie.class);
            if (megaCreeps) {
                ItemStack sword = new ItemStack(Material.STONE_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 4);
                z.getEquipment().setItemInHand(sword);
                z.getEquipment().setItemInHandDropChance(0);
            }
            z.setRemoveWhenFarAway(false);
            z.setCanPickupItems(false);
            ControllableMob<Zombie> cz = ControllableMobs.putUnderControl(z, true);
            cz.getAttributes().setMaximumNavigationDistance(64);
            cz.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", LknockM, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getAttackDamageAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "attack dmg", LdmgM, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "max health", LhealthM, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getEntity().setHealth(cz.getEntity().getMaxHealth());
            cz.getAI().addBehavior(new AIAttackMelee(1, 1.1));
            cz.getAI().addBehavior(new AITargetHurtBy(2, false));
            cz.getAI().addBehavior(new AITargetNearest(3, 10, false, 20 * 3));
            cz.getAI().addBehavior(new AIFloat(4));
            cz.getAI().addBehavior(new AILookAtEntity(5, (float) 8));
            cz.getAI().addBehavior(new AIRandomLookaround(5));
            laneCreeps.add(cz);
            controlMobs.add(cz);
        }
        for (int i = 0; i < 2; i++) {
            Skeleton s = getServer().getWorld(worldName).spawn(spawnloc, Skeleton.class);
            ItemStack bow = new ItemStack(Material.BOW);
            if (megaCreeps) {
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 2);
            }
            s.getEquipment().setItemInHand(bow);
            s.getEquipment().setItemInHandDropChance(0);
            s.setRemoveWhenFarAway(false);
            s.setCanPickupItems(false);
            ControllableMob<Skeleton> cs = ControllableMobs.putUnderControl(s, true);
            cs.getAttributes().setMaximumNavigationDistance(64);
            cs.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", LknockR, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getAttributes().getAttackDamageAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "attack dmg", LdmgR, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "max health", LhealthR, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getEntity().setHealth(cs.getEntity().getMaxHealth());
            cs.getAI().addBehavior(new AIAttackRanged(1, 1.2, 14, 40));
            cs.getAI().addBehavior(new AITargetHurtBy(2, false));
            cs.getAI().addBehavior(new AITargetNearest(3, 16, false));
            cs.getAI().addBehavior(new AIFloat(4));
            cs.getAI().addBehavior(new AILookAtEntity(5, (float) 20));
            laneCreeps.add(cs);
            controlMobs.add(cs);
        }
    }

    public void spawnJungle(Location loc) {
        for (int i = 0; i < 2; i++) {
            Zombie z = getServer().getWorld(worldName).spawn(loc, Zombie.class);
            z.setRemoveWhenFarAway(true);
            z.setCanPickupItems(false);
            ControllableMob<Zombie> cz = ControllableMobs.putUnderControl(z, true);
            cz.getAttributes().setMaximumNavigationDistance(8);
            cz.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 0.65, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getAttackDamageAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "attack dmg", 5.0, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "health max", 10.0, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getEntity().setHealth(cz.getEntity().getMaxHealth());
            cz.getAI().addBehavior(new AIAttackMelee(1, 1.2));
            cz.getAI().addBehavior(new AITargetNearest(2, 5, true));
            cz.getAI().addBehavior(new AILookAtEntity(3, (float) 12));
            jungleCreeps.add(cz);
            controlMobs.add(cz);
        }
    }

    private void createTable() {
        try {
            Statement s = sql.getConn().createStatement();
            s.execute("CREATE TABLE IF NOT EXISTS `dotamine` (\n"
                    + "  `name` VARCHAR(24) NOT NULL,\n"
                    + "  `wins` INT NOT NULL DEFAULT 0,\n"
                    + "  `loses` INT NOT NULL DEFAULT 0,\n"
                    + "  `kills` INT NOT NULL DEFAULT 0,\n"
                    + "  `deaths` INT NOT NULL DEFAULT 0,\n"
                    + "  PRIMARY KEY (`name`));");
        } catch (SQLException e) {
            mysqlEnabled = false;
            getLogger().log(Level.SEVERE, "Couldn''t connect to MySQL and create table: {0}", e.getMessage());
        }
    }

    public void removePlayerFromQueue(Player p) {
        queue.remove(p);
        p.setDisplayName(p.getName());
        p.teleport(normalSpawn);
    }

    public void hidePlayerFromTeam(Player p, int teamHideFrom) {
        for (Jogador j : ingameList.values()) {
            if (j.getTeam() == teamHideFrom) {
                j.getPlayer().hidePlayer(p);
            }
        }
    }

    public void showPlayerFromTeam(Player p, int teamShowFrom) {
        for (Jogador j : ingameList.values()) {
            if (j.getTeam() == teamShowFrom) {
                j.getPlayer().showPlayer(p);
            }
        }
    }

    public void smokeEffect(Location location, int quantity) {
        if (useEffects) {
            for (int i = 0; i < quantity; i++) {
                location.getWorld().playEffect(location, Effect.SMOKE, random.nextInt(8));
            }
        }
    }

    public void breakEffect(Location location, int quantity, int BlockID) {
        if (useEffects) {
            for (int i = 0; i < quantity; i++) {
                location.getWorld().playEffect(location, Effect.STEP_SOUND, BlockID);
            }
        }
    }

    public int getOtherTeam(Player p) {
        if (ingameList.get(p).getTeam() == 1) {
            return 2;
        } else {
            return 1;
        }
    }

    public int getOtherTeam(int team) {
        if (team == 1) {
            return 2;
        } else {
            return 1;
        }
    }

    public int getRandom(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    private String getMotdMessage(String name) {
        return getLang("lang.motd." + name).replaceAll("%queue", Integer.toString(queue.size())).replaceAll("%ingame", Integer.toString(ingameList.size())).replaceAll("%spectator", Integer.toString(spectators.size())).replaceAll("%min", Integer.toString(MIN_PLAYERS)).replaceAll("%max", Integer.toString(MAX_PLAYERS));
    }

    public void cancelTp(Player p) {
        getServer().getScheduler().cancelTask(teleportingE.get(p));
        teleportingE.remove(p);
        getServer().getScheduler().cancelTask(teleportingT.get(p));
        teleportingT.remove(p);
        p.sendMessage(getLang("lang.tpCancelled"));
    }

    public void removeTarrasque(Player damaged) {
        damaged.removePotionEffect(PotionEffectType.REGENERATION);
        tarrasqueRecentlyDamaged.add(damaged);
        getServer().getScheduler().scheduleSyncDelayedTask(this, new TarrasqueRemRDRunnable(this, damaged), 20 * 14);
    }

    public boolean checkForTarrasque(Player p) {
        if (p.getInventory().contains(Material.FERMENTED_SPIDER_EYE)) {
            if (!hasTarrasque.contains(p)) {
                hasTarrasque.add(p);
                p.sendMessage(getLang("lang.hasTarrasque"));
            }
            return true;
        } else {
            if (hasTarrasque.contains(p)) {
                hasTarrasque.remove(p);
                p.sendMessage(getLang("lang.tarrasqueRemoved"));
            }
            return false;
        }
    }

    public List<Player> getSpectatorList() {
        List<Player> l = new ArrayList();
        for (Player p : ingameList.keySet()) {
            l.add(p);
        }
        return l;
    }
}
