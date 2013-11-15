package com.jabyftw.dotamine;

import com.jabyftw.dotamine.listeners.PlayerListener;
import com.jabyftw.dotamine.commands.DotaCommand;
import com.jabyftw.dotamine.commands.JoinCommand;
import com.jabyftw.dotamine.commands.SpectateCommand;
import com.jabyftw.dotamine.listeners.BlockListener;
import com.jabyftw.dotamine.listeners.EntityListener;
import com.jabyftw.dotamine.runnables.CreepSpawnRunnable;
import com.jabyftw.dotamine.runnables.JungleSpawnRunnable;
import com.jabyftw.dotamine.runnables.StartGameRunnable;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
    public int redCount, blueCount, state;
    public boolean useVault, gameStarted;
    public Economy econ = null;
    public FileConfiguration config;
    public Map<ControllableMob, Integer> controlMobs = new HashMap();
    public Map<Player, Jogador> ingameList = new HashMap();
    public Map<Tower, Location> towers = new HashMap();
    public Map<Player, ItemStack[]> playerDeathItems = new HashMap();
    public Map<Player, ItemStack[]> playerDeathArmor = new HashMap();
    public Map<Location, Location> creepSpawn = new HashMap();
    public Map<Location, Location> jungleSpawn = new HashMap();
    public List<Player> queue = new ArrayList();
    public List<Player> spectators = new ArrayList();
    public Location blueDeploy, redDeploy, specDeploy, normalSpawn;//TODO: towers

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
    }

    @Override
    public void onDisable() {
        //TODO: right chunks for map
        for (int a = -55; a <= -79; a++) {
            for (int b = 7; b <= 31; b++) {
                getServer().getWorld(worldName).unloadChunk(a, b, false, false);
            }
        }
        getServer().unloadWorld(worldName, false);
    }

    private void generateConfig() {
        config.addDefault("config.useVault", false);
        config.addDefault("lang.noPermission", "&cNo permission!");
        config.addDefault("lang.onlyIngame", "&4You are not a player!");
        config.addDefault("lang.gameIsFull", "&cSorry, the game is full!");
        config.addDefault("lang.onBlueTeam", "&6You are on &bBlue Team&6!");
        config.addDefault("lang.onRedTeam", "&6You are on &4Red Team&6!");
        config.addDefault("lang.alreadyStarted", "&4Already started!");
        config.addDefault("lang.gameNotStarted", "&cThere is no game to spectate!");
        config.addDefault("lang.startingIn90sec", "&6Game starting in &c90 seconds&6!");
        config.addDefault("lang.youLoseXMoney", "&cYou lose &4%money&c for dying.");
        config.addDefault("lang.playerWonXMoneyforKilling", "%player&6 won &e%money gold&6 for killing %dead");
        config.addDefault("lang.theGamehasStarted", "&6Good Luck and Have fun! &4The Dota&c has started!");
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
        //config.addDefault("lang.", "&");
        config.options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        useVault = config.getBoolean("config.useVault");
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
        blueDeploy = new Location(w, 3, 64, 5);
        redDeploy = new Location(w, 3, 64, 5);
        specDeploy = new Location(w, 5, 64, 3);
        normalSpawn = new Location(w, 5, 64, 3);
        //TODO: towers (list) and towers (locations)
        //TODO: creepspawn, junglespawn
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

    public void addtoPlayerQueue(Player p) {
        cleanPlayer(p, false, false);
        p.setDisplayName(ChatColor.GREEN + p.getName());
        p.sendMessage(getLang("lang.waitingTheGame"));
        queue.add(p);
        if (queue.size() >= 6) {
            startGame();
        }
    }

    public void addPlayer(Player p) {
        if (ingameList.size() < 12) {
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
                p.teleport(blueDeploy); // TODO: on unequip, dont allow remove blue/red wool
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 11));
                p.sendMessage(getLang("config.onBlueTeam"));
            } else {
                p.setDisplayName(ChatColor.RED + p.getName());
                p.setCustomName(ChatColor.RED + p.getName());
                p.setCustomNameVisible(true);
                redCount = redCount + 1;
                p.teleport(redDeploy);
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));
                p.sendMessage(getLang("config.onRedTeam"));
            }
            ingameList.put(p, new Jogador(this, p, 0, 0, 0, 0, team));
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

    public void startGame() {
        for (Player p : queue) {
            addPlayer(p);
            p.sendMessage(getLang("lang.startingIn90sec"));
        }
        state = 1;
        getServer().getScheduler().scheduleSyncDelayedTask(this, new StartGameRunnable(this), 20 * 90);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new CreepSpawnRunnable(this), 20*90, 20*60);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new JungleSpawnRunnable(this), 20*60, 20*60);
    }

    @EventHandler
    public void onPing(ServerListPingEvent e) {
        if (state == 0) {
            e.setMotd("§6[Dota] §aWaiting... " + queue.size() + "/6");
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
        getServer().setWhitelist(false);
        getServer().shutdown();
    }
}
