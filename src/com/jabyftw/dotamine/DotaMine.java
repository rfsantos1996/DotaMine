package com.jabyftw.dotamine;

import com.jabyftw.dotamine.listeners.PlayerListener;
import com.jabyftw.dotamine.commands.DotaCommand;
import com.jabyftw.dotamine.commands.JoinCommand;
import com.jabyftw.dotamine.commands.SpectateCommand;
import com.jabyftw.dotamine.listeners.BlockListener;
import com.jabyftw.dotamine.listeners.EntityListener;
import com.jabyftw.dotamine.runnables.AnnounceQueueRunnable;
import com.jabyftw.dotamine.runnables.CreepSpawnRunnable;
import com.jabyftw.dotamine.runnables.CheckNightRunnable;
import com.jabyftw.dotamine.runnables.JungleSpawnRunnable;
import com.jabyftw.dotamine.runnables.ScoreboardRunnable;
import com.jabyftw.dotamine.runnables.StartGameRunnable;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackMelee;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackRanged;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIFloat;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AILookAtEntity;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AITargetNearest;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.AttributeModifierFactory;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.ModifyOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
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

/**
 *
 * @author Rafael
 */
public class DotaMine extends JavaPlugin implements Listener {

    public String worldName;
    public int redCount, blueCount, state, targetRunnable, scoreRunnable, MIN_PLAYERS, MAX_PLAYERS;
    public boolean useVault, gameStarted, nerfRanged;
    public Economy econ = null;
    public FileConfiguration config;
    public Map<Player, Jogador> ingameList = new HashMap();
    public Map<Location, Tower> towers = new HashMap();
    public Map<Player, ItemStack[]> playerDeathItems = new HashMap();
    public Map<Player, ItemStack[]> playerDeathArmor = new HashMap();
    public List<Location> botCreepSpawn = new ArrayList();
    public List<Location> midCreepSpawn = new ArrayList();
    public List<Location> topCreepSpawn = new ArrayList();
    public List<Location> jungleSpawn = new ArrayList();
    public Map<Player, Integer> queue = new HashMap();
    public List<Player> spectators = new ArrayList();
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

    @Override
    public void onEnable() {
        gameStarted = false;
        state = 0;
        config = getConfig();
        generateConfig();
        setLocations(getServer().getWorld(worldName));
        if (useVault) {
            setupEconomy();
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
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new AnnounceQueueRunnable(this), 20 * 15, 20 * 30);
        getLogger().log(Level.INFO, "Registered runnable.");
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
    }

    private void generateConfig() {
        //config.addDefault("config.", value);
        config.addDefault("config.useVault", false);
        config.addDefault("config.worldName", "world");
        config.addDefault("config.targetRunnableDelayInTicks", 30);
        config.addDefault("config.scoreRunnableDelayInTicks", 60); // 3 sec
        config.addDefault("config.MAX_PLAYERS", 12);
        config.addDefault("config.MIN_PLAYERS", 6);
        config.addDefault("config.nerfRangedAtNight", false);
        //config.addDefault("lang.", "&");
        config.addDefault("lang.noPermission", "&cNo permission!");
        config.addDefault("lang.onlyIngame", "&4You are not a player!");
        config.addDefault("lang.gameIsFull", "&cSorry, the game is full!");
        config.addDefault("lang.onBlueTeam", "&6You are on &bBlue Team&6!");
        config.addDefault("lang.onRedTeam", "&6You are on &4Red Team&6!");
        config.addDefault("lang.alreadyStarted", "&4Already started!");
        config.addDefault("lang.gameNotStarted", "&cThere is no game to spectate!");
        config.addDefault("lang.startingIn90sec", "&6Game starting in &c90 seconds&6!");
        config.addDefault("lang.youLoseXMoney", "&cYou lose &4%money&c for dying.");
        config.addDefault("lang.theGamehasStarted", "&6Good Luck and Have fun! &4The Dota&c has started!");
        config.addDefault("lang.creepsWillSpawn", "&6Creeps will spawn in 10 seconds.");
        config.addDefault("lang.joinedSpectator", "&6You are now a spectator!");
        config.addDefault("lang.leftSpectator", "&cYou left spectator mode!");
        config.addDefault("lang.waitingTheGame", "&6You are on queue! Waiting players...");
        config.addDefault("lang.youCanPlay", "&6You can play Dota using &c/join");
        config.addDefault("lang.youCanJoin", "&6You can join Dota using &c/join&6 before it starts");
        config.addDefault("lang.youCanSpectate", "&6You can spectate using &c/spectate&6!");
        config.addDefault("lang.redTeamWon", "&cRed Team &6won! Congratulations! &cRestarting in 30 sec!");
        config.addDefault("lang.blueTeamWon", "&bBlue Team &6won! Congratulations! &cRestarting in 30 sec!");
        config.addDefault("lang.towerDestroyed", "&4%tower &cwas destroyed");
        config.addDefault("lang.kickMessage", "&4The game is over!&c Restarting...");
        config.addDefault("lang.usePlayCommand", "&cUsage: &6/play (ranged/meele)");
        config.addDefault("lang.settedMeele", "&6You will play as a Meele hero.");
        config.addDefault("lang.settedRanged", "&6You will play as a Ranged hero.");
        config.addDefault("lang.forcingStart", "&cForcing start...");
        config.addDefault("lang.nobodyOnQueue", "&cQueue is empty! Cant force start...");
        config.addDefault("lang.diedForNeutral", "%name &cdied for a &4creep or neutral&c.");
        config.addDefault("lang.queueSizeIs", "&6People in queue: &e%size&6! &cWe need %needed player(s).");
        config.addDefault("lang.onePlayerLeft", "&cThere's only 1 player left. &4Restarting server...");
        config.addDefault("lang.lowRangedNightVision", "&cRanged players have low night vision. &4ATENTION AT NIGHT!");
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
        config.options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        worldName = config.getString("config.worldName");
        useVault = config.getBoolean("config.useVault");
        targetRunnable = config.getInt("config.targetRunnableDelayInTicks");
        scoreRunnable = config.getInt("config.scoreRunnableDelayInTicks");
        MIN_PLAYERS = config.getInt("config.MIN_PLAYERS");
        MAX_PLAYERS = config.getInt("config.MAX_PLAYERS");
        nerfRanged = config.getBoolean("config.nerfRangedAtNight");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
        return (econ != null);
    }

    private void setLocations(World w) {
        w.setPVP(true);
        w.setAutoSave(false);
        w.setSpawnFlags(false, false);
        for (Entity e : w.getEntities()) {
            e.remove();
        }
        blueFMidT = new Location(w, 12, 33, -7);
        towers.put(blueFMidT, new Tower(blueFMidT, "Blue Mid First Tower"));
        blueFBotT = new Location(w, -57, 33, -85);
        towers.put(blueFBotT, new Tower(blueFBotT, "Blue Bot First Tower"));
        blueFTopT = new Location(w, 81, 33, 62);
        towers.put(blueFTopT, new Tower(blueFTopT, "Blue Top First Tower"));
        redFMidT = new Location(w, -7, 33, 7);
        towers.put(redFMidT, new Tower(redFMidT, "Red Mid First Tower"));
        redFBotT = new Location(w, -82, 33, -65);
        towers.put(redFBotT, new Tower(redFBotT, "Red Bot First Tower"));
        redFTopT = new Location(w, 52, 33, 87);
        towers.put(redFTopT, new Tower(redFTopT, "Red Top Fisrt Tower"));

        blueSMidT = new Location(w, 50, 33, -42);
        towers.put(blueSMidT, new Tower(blueSMidT, "Blue Mid Second Tower"));
        blueSBotT = new Location(w, 30, 33, -83);
        towers.put(blueSBotT, new Tower(blueSBotT, "Blue Bot Second Tower"));
        blueSTopT = new Location(w, 82, 33, -17);
        towers.put(blueSTopT, new Tower(blueSTopT, "Blue Top Second Tower"));
        redSMidT = new Location(w, -49, 33, 51);
        towers.put(redSMidT, new Tower(redSMidT, "Red Mid Second Tower"));
        redSBotT = new Location(w, -79, 33, -1);
        towers.put(redSBotT, new Tower(redSBotT, "Red Bot Second Tower"));
        redSTopT = new Location(w, -10, 33, 87);
        towers.put(redSTopT, new Tower(redSTopT, "Red Top Second Tower"));

        blueAncient = new Location(w, 79, 36, -77);
        towers.put(blueAncient, new Tower(blueAncient, "Blue Ancient"));
        redAncient = new Location(w, -72, 36, 79);
        towers.put(redAncient, new Tower(redAncient, "Red Ancient"));

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

        blueDeploy = new Location(w, 86, 28, -87);
        redDeploy = new Location(w, -80, 28, 87);
        specDeploy = new Location(w, 2, 33, 0);
        normalSpawn = new Location(w, 51, 8, -121);

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
                hiding.hidePlayer(hideFrom);
            }
        }
    }

    public void showPlayer(Player showing) {
        for (Player showTo : getServer().getOnlinePlayers()) {
            if (showTo.equals(showing)) {
                continue;
            }
            if (!showTo.canSee(showing)) {
                showing.showPlayer(showTo);
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
            econ.withdrawPlayer(p.getName(), econ.getBalance(p.getName()));
        }
    }

    private int getTeam() {
        if (redCount > blueCount) {
            return 1; // Blue
        } else {
            return 2; // Red
        }
    }

    public void addtoPlayerQueue(Player p, int attackType) {
        cleanPlayer(p, false, false);
        p.setDisplayName(ChatColor.GREEN + p.getName());
        p.sendMessage(getLang("lang.waitingTheGame"));
        queue.put(p, attackType);
        if (queue.size() > 5) { // >= 6
            startGame(false);
        }
    }

    public void addPlayer(Player p, int attackType) {
        if (ingameList.size() < MAX_PLAYERS) {
            if (spectators.contains(p)) {
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
                sword.addUnsafeEnchantment(Enchantment.DURABILITY, 999); // 0.1% chance for losing durability when using
                ItemStack bow = new ItemStack(Material.BOW, 1);
                bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
                bow.addEnchantment(Enchantment.DURABILITY, 2);
                p.getInventory().addItem(sword);
                p.getInventory().addItem(bow);
            } else { // Ranged
                ItemStack bow = new ItemStack(Material.BOW, 1);
                bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
                bow.addUnsafeEnchantment(Enchantment.DURABILITY, 999);
                p.getInventory().addItem(bow);
                p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
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

    public void removePlayer(Player p, int team) {
        cleanPlayer(p, false, false);
        p.teleport(normalSpawn);
        if (team == 1) {
            blueCount = blueCount - 1;
        } else {
            redCount = redCount - 1;
        }
        ingameList.remove(p);
    }

    public void addSpectator(Player p) {
        cleanPlayer(p, true, true);
        p.setDisplayName(ChatColor.GRAY + p.getName());
        p.setCustomName(ChatColor.GRAY + p.getName());
        p.setCustomNameVisible(true);
        p.teleport(specDeploy);
        p.sendMessage(getLang("lang.joinedSpectator"));
        spectators.add(p);
    }

    public void removeSpectator(Player p) {
        cleanPlayer(p, false, false);
        p.teleport(normalSpawn);
        p.sendMessage(getLang("lang.leftSpectator"));
        spectators.remove(p);
    }

    public void startGame(boolean forced) {
        for (Player p : queue.keySet()) {
            addPlayer(p, queue.get(p));
            p.sendMessage(getLang("lang.startingIn90sec"));
        }
        state = 1;
        gameStarted = true;
        if (forced) {//TODO: for release, remove force start low time
            getServer().getScheduler().scheduleSyncDelayedTask(this, new StartGameRunnable(this), 20 * 20);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new CreepSpawnRunnable(this), 20 * 20, 20 * 20);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new JungleSpawnRunnable(this), 20 * 10, 20 * 20);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new ScoreboardRunnable(this), scoreRunnable / 2, scoreRunnable / 2);
            if (nerfRanged) {
                getServer().getScheduler().scheduleSyncRepeatingTask(this, new CheckNightRunnable(this), 20, 20 * 3);
            }
        } else {
            getServer().getScheduler().scheduleSyncDelayedTask(this, new StartGameRunnable(this), 20 * 80);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new CreepSpawnRunnable(this), 20 * 90, 20 * 60);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new JungleSpawnRunnable(this), 20 * 80, 20 * 60);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new ScoreboardRunnable(this), 1, scoreRunnable);
            if (nerfRanged) {
                getServer().getScheduler().scheduleSyncRepeatingTask(this, new CheckNightRunnable(this), 20, 20 * 3);
            }
        }
        getServer().getWorld(worldName).setTime(0); // start as day
    }

    @EventHandler
    public void onPing(ServerListPingEvent e) {
        if (state == 0) {
            e.setMotd("§6[Dota] §aWaiting players... " + queue.size() + "/" + MIN_PLAYERS);
        } else if (state == 1) {
            e.setMotd("§6[Dota] §eStarting in +-90 sec...");
        } else if (state == 2) {
            e.setMotd("§6[Dota] §cPlaying... §4" + ingameList.size() + "/" + spectators.size());
        } else if (state == 3) {
            e.setMotd("§6[Dota] §4Restarting...");
        }
    }

    public void endGame() {
        for (Player p : getServer().getOnlinePlayers()) {
            p.kickPlayer(getLang("lang.kickMessage"));
        }
        getServer().shutdown();
    }

    public void spawnLaneCreeps(Location spawnloc) {
        for (int i = 0; i < 5; i++) {
            Zombie z = getServer().getWorld(worldName).spawn(spawnloc, Zombie.class);
            z.setRemoveWhenFarAway(false);
            ControllableMob<Zombie> cz = ControllableMobs.putUnderControl(z, true);
            cz.getAttributes().setMaximumNavigationDistance(300);
            cz.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 0.4, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "max health", 4, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAI().addBehavior(new AIAttackMelee(1, 1.1));
            cz.getAI().addBehavior(new AITargetNearest(2, 8, false));
            cz.getAI().addBehavior(new AIFloat(3));
            cz.getAI().addBehavior(new AILookAtEntity(4, (float) 8));
            laneCreeps.add(cz);
            controlMobs.add(cz);
        }
        for (int i = 0; i < 2; i++) {
            Skeleton s = getServer().getWorld(worldName).spawn(spawnloc, Skeleton.class);
            s.getEquipment().setItemInHand(new ItemStack(Material.BOW));
            s.getEquipment().setItemInHandDropChance(0);
            s.setRemoveWhenFarAway(false);
            ControllableMob<Skeleton> cs = ControllableMobs.putUnderControl(s, true);
            cs.getAttributes().setMaximumNavigationDistance(1000);
            cs.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 0.2, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "max health", 2, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getAI().addBehavior(new AIAttackRanged(1, 1.2, 20));
            cs.getAI().addBehavior(new AITargetNearest(2, 22, false));
            cs.getAI().addBehavior(new AIFloat(3));
            cs.getAI().addBehavior(new AILookAtEntity(4, (float) 24));
            laneCreeps.add(cs);
            controlMobs.add(cs);
        }
    }

    public void spawnJungle(Location loc) {
        for (int i = 0; i < 2; i++) {
            Zombie z = getServer().getWorld(worldName).spawn(loc, Zombie.class);
            z.setRemoveWhenFarAway(true);
            ControllableMob<Zombie> cz = ControllableMobs.putUnderControl(z, true);
            cz.getAttributes().setMaximumNavigationDistance(8);
            cz.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 0.7, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getAttackDamageAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "attack dmg", 3.0, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "health max", 10.0, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAI().addBehavior(new AIAttackMelee(1, 1.2));
            cz.getAI().addBehavior(new AITargetNearest(2, 5, true));
            cz.getAI().addBehavior(new AILookAtEntity(3, (float) 12));
            jungleCreeps.add(cz);
            controlMobs.add(cz);
        }
    }
}
