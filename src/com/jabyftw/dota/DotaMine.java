package com.jabyftw.dota;

import com.jabyftw.dota.commands.DotaCommand;
import com.jabyftw.dota.commands.JoinCommand;
import com.jabyftw.dota.commands.RecallCommand;
import com.jabyftw.dota.commands.SpectateCommand;
import com.jabyftw.dota.listeners.BlockListener;
import com.jabyftw.dota.listeners.EntityListener;
import com.jabyftw.dota.listeners.PlayerListener;
import com.jabyftw.dota.runnable.CreepSpawnRunnable;
import com.jabyftw.dota.runnable.UnfreezeRunnable;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Rafael
 */
public class DotaMine extends JavaPlugin {

    private SQL sql;
    private String username, password, url;
    public String tableName, worldName;
    public boolean useVault;
    public boolean gameStarted = false;
    public boolean bigCreeps = false;
    public Economy econ = null;
    public List<Player> spectators = new ArrayList();
    public List<Location> mobSpawn = new ArrayList();
    public List<ControllableMob> controllablemobs = new ArrayList();
    public Map<Location, Tower> towers = new HashMap();
    public Map<Player, Jogador> players = new HashMap();
    public Map<Player, ItemStack[]> playerDeathItems = new HashMap();
    public Map<Player, ItemStack[]> playerDeathArmor = new HashMap();
    public Location RedAncient, BlueAncient, BlueMid, RedMid;
    public Location BlueTop1, BlueTop2, RedTop1, RedTop2, BlueBot1, BlueBot2, RedBot1, RedBot2;
    public Location BlueTopSpawn, BlueMidSpawn, BlueBotSpawn, RedTopSpawn, RedMidSpawn, RedBotSpawn, BlueSpawn, RedSpawn;
    public int redTeam, blueTeam;

    @Override
    public void onEnable() {
        redTeam = 0;
        blueTeam = 0;
        generateConfig();
        if (useVault) {
            setupEconomy();
        }
        setupWorld(getServer().getWorld(worldName));
        BlueSpawn.getChunk().load();
        RedSpawn.getChunk().load();
        sql = new SQL(this, username, password, url);
        getLogger().log(Level.INFO, "Connected to MySQL!");
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getLogger().log(Level.INFO, "Registered listeners!");
        getServer().getPluginCommand("join").setExecutor(new JoinCommand(this));
        getServer().getPluginCommand("recall").setExecutor(new RecallCommand(this));
        getServer().getPluginCommand("dota").setExecutor(new DotaCommand(this));
        getServer().getPluginCommand("spectate").setExecutor(new SpectateCommand(this));
        getLogger().log(Level.INFO, "Loaded commands!");
    }

    @Override
    public void onDisable() {
        sql.closeConn();
        for (int a = -55; a <= -79; a++) {
            for (int b = 7; b <= 31; b++) {
                getServer().getWorld(worldName).unloadChunk(a, b, false, false);
            }
        }
        getServer().unloadWorld(worldName, false);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
        return (econ != null);
    }

    private void setupWorld(World w) {
        w.setPVP(true);
        w.setAutoSave(false);
        towers.clear();
        towers.put(BlueAncient, new Tower(this, BlueAncient, "Anciente Azul"));
        towers.put(RedAncient, new Tower(this, RedAncient, "Anciente Vermelho"));
        towers.put(BlueMid, new Tower(this, BlueMid, "Torre Central Azul"));
        towers.put(RedMid, new Tower(this, RedMid, "Torre Central Vermelha"));
        towers.put(BlueTop1, new Tower(this, BlueTop1, "Torre Top Proxima Azul"));
        towers.put(BlueTop2, new Tower(this, BlueTop2, "Torre Top Longe Azul"));
        towers.put(RedTop1, new Tower(this, RedTop1, "Torre Top Proxima Vermelha"));
        towers.put(RedTop2, new Tower(this, RedTop2, "Torre Top Longe Vermelha"));
        towers.put(BlueBot1, new Tower(this, BlueBot1, "Torre Top Proxima Azul"));
        towers.put(BlueBot2, new Tower(this, BlueBot2, "Torre Top Longe Azul"));
        towers.put(RedBot1, new Tower(this, RedBot1, "Torre Bot Proxima Vermelha"));
        towers.put(RedBot2, new Tower(this, RedBot2, "Torre Bot Longe Vermelha"));
        mobSpawn.clear();
        mobSpawn.add(BlueTopSpawn);
        mobSpawn.add(BlueMidSpawn);
        mobSpawn.add(BlueBotSpawn);
        mobSpawn.add(RedTopSpawn);
        mobSpawn.add(RedMidSpawn);
        mobSpawn.add(RedBotSpawn);
    }

    private void generateConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("MySQL.username", "root");
        config.addDefault("MySQL.password", "123");
        config.addDefault("MySQL.table", "dotaRanking");
        config.addDefault("MySQL.url.host", "localhost");
        config.addDefault("MySQL.url.port", 3306);
        config.addDefault("MySQL.url.database", "minecraft");
        config.addDefault("config.useVault", false);
        config.addDefault("config.locations.worldName", "World");
        config.addDefault("config.locations.bluespawn.x", -945);
        config.addDefault("config.locations.bluespawn.y", 49);
        config.addDefault("config.locations.bluespawn.z", 195);
        config.addDefault("config.locations.redspawn.x", -1197);
        config.addDefault("config.locations.redspawn.y", 50);
        config.addDefault("config.locations.redspawn.z", 454);
        config.options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        url = "jdbc:mysql://" + config.getString("MySQL.url.host") + ":" + config.getInt("MySQL.url.port") + "/" + config.getString("MySQL.url.database");
        username = config.getString("MySQL.username");
        password = config.getString("MySQL.password");
        tableName = config.getString("MySQL.table");
        worldName = config.getString("config.locations.worldName");
        useVault = config.getBoolean("config.useVault");

        World w = getServer().getWorld(worldName);
        BlueSpawn = new Location(w, config.getDouble("config.locations.bluespawn.x"), config.getDouble("config.locations.bluespawn.y"), config.getDouble("config.locations.bluespawn.z"));
        RedSpawn = new Location(w, config.getDouble("config.locations.bluespawn.x"), config.getDouble("config.locations.bluespawn.y"), config.getDouble("config.locations.bluespawn.z"));
        BlueAncient = new Location(w, -975, 55, 224);
        RedAncient = new Location(w, -1159, 57, 410);
        BlueMid = new Location(w, -1040, 54, 292);
        RedMid = new Location(w, -1093, 54, 343);
        BlueTop1 = new Location(w, -1042, 53, 180);
        BlueTop2 = new Location(w, -1130, 53, 190);
        RedTop1 = new Location(w, -1192, 53, 350);
        RedTop2 = new Location(w, -1193, 53, 260);
        BlueBot1 = new Location(w, -941, 53, 285);
        BlueBot2 = new Location(w, -940, 53, 375);
        RedBot1 = new Location(w, -1091, 53, 455);
        RedBot2 = new Location(w, -1003, 53, 455);
        //TODO: other locations
        // BlueTopSpawn, BlueMidSpawn, BlueBotSpawn, RedTopSpawn, RedMidSpawn, RedBotSpawn
        BlueTopSpawn = new Location(w, -1254, 67, 125);
        BlueMidSpawn = new Location(w, -1254, 67, 125);
        BlueBotSpawn = new Location(w, -1254, 67, 125);
        RedTopSpawn = new Location(w, -1254, 67, 125);
        RedMidSpawn = new Location(w, -1254, 67, 125);
        RedBotSpawn = new Location(w, -1254, 67, 125);
    }

    public void showPlayer(Player showing) {
        for (Player player : getServer().getOnlinePlayers()) {
            if (showing.equals(player)) {
                continue;
            }
            if (!player.canSee(showing)) {
                player.showPlayer(showing);
            }
        }
    }

    public void hidePlayer(Player vanishing) {
        for (Player player : getServer().getOnlinePlayers()) {
            if (vanishing.equals(player)) {
                continue;
            }
            if (player.canSee(vanishing)) {
                player.hidePlayer(vanishing);
            }
        }
    }

    public void clearPlayer(Player p, boolean flying) {
        p.setLevel(0);
        p.setExp(0);
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setAllowFlight(flying);
        p.setGameMode(GameMode.ADVENTURE);
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.saveData();
    }

    public void addSpectator(Player p) {
        if (!players.containsKey(p)) {
            spectators.add(p);
            clearPlayer(p, true);
            hidePlayer(p);
            p.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
            p.sendMessage(ChatColor.DARK_RED + "You are a spectator! " + ChatColor.RED + "Use your " + ChatColor.GOLD + "compass" + ChatColor.RED + " to teleport between players");
        }
    }

    public void broadcastMsg(String message) {
        World world = getServer().getWorld(worldName);
        for (Player p : world.getPlayers()) {
            p.sendMessage(message);
        }
    }

    public void removePlayer(Player p, boolean quitting) {
        if (players.containsKey(p)) {
            players.remove(p);
            clearPlayer(p, false);
            if (playerDeathArmor.containsKey(p)) {
                playerDeathArmor.remove(p);
                playerDeathItems.remove(p);
            }
        }
        if (!quitting) {
            addSpectator(p);
            clearPlayer(p, true);
        }
    }

    public void addPlayer(Player p, int team) {
        players.put(p, new Jogador(this, p, 0, 0, 0, 0, team));
        if (spectators.contains(p)) {
            spectators.remove(p);
        }
        clearPlayer(p, false);
        showPlayer(p);
        if (team == 1) {
            p.setCustomName(ChatColor.AQUA + p.getName());
            p.teleport(BlueSpawn);
            p.setBedSpawnLocation(BlueSpawn);
            blueTeam++;
            p.sendMessage("You are team BLUE");
        } else {
            p.setCustomName(ChatColor.RED + p.getName());
            p.teleport(RedSpawn);
            p.setBedSpawnLocation(RedSpawn);
            redTeam++;
            p.sendMessage("You are team RED");
        }
        p.setCustomNameVisible(true);

        if (players.size() >= 6) {
            startGame(false);
        } else {
            broadcastMsg(ChatColor.GOLD + "[Dota] " + ChatColor.RED + "Jogadores esperando: " + players.size() + " de 6 minimo. Para entrar use " + ChatColor.YELLOW + "/join");
            //TODO: fix fixed (?)
            players.get(p).setFixed(true);
        }
    }

    /*
     base: 100
     */
    public double getKillMoney(int deadsKillstreak) {
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

    public double getLHMoney() {
        double x = Math.random();
        if (x > 21 && x < 30) {
            return x;
        }
        return getLHMoney();
    }

    public double getDeathMoney(int killstreak) {
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

    /*
     1 = blue
     2 = red
     */
    public int getTeam() {
        if (blueTeam > redTeam) {
            return 2;
        } else {
            return 1;
        }
    }

    public void startGame(boolean useFast) {
        gameStarted = true;
        int timeToStart;
        if (useFast) {
            timeToStart = 5;
        } else {
            timeToStart = 30;
        }
        getServer().getScheduler().scheduleSyncDelayedTask(this, new UnfreezeRunnable(this), 20 * timeToStart);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new CreepSpawnRunnable(this), (20 * timeToStart) * 3, (20 * timeToStart) * 2); // 1:30 and repeat every minute

        broadcastMsg(ChatColor.GOLD + "[Dota] " + ChatColor.RED + "The game will start in " + ChatColor.YELLOW + timeToStart + ChatColor.RED + " seconds!");
    }
}
