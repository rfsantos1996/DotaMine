package com.jabyftw.dota;

import com.jabyftw.dota.listeners.BlockListener;
import com.jabyftw.dota.listeners.PlayerListener;
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
    public boolean gameStarted = false;
    public Economy econ = null;
    public List<Player> spectators = new ArrayList();
    public Map<Player, Jogador> players = new HashMap();
    public Map<Player, ItemStack[]> playerDeathItems = new HashMap();
    public Map<Player, ItemStack[]> playerDeathArmor = new HashMap();
    public Location BlueSpawn, RedSpawn;
    public int redTeam, blueTeam;

    @Override
    public void onEnable() {
        redTeam = 0;
        blueTeam = 0;
        generateConfig();
        setupEconomy();
        setupWorld(getServer().getWorld(worldName));
        BlueSpawn.getChunk().load();
        RedSpawn.getChunk().load();
        sql = new SQL(this, username, password, url);
        getLogger().log(Level.INFO, "Connected to MySQL!");
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getLogger().log(Level.INFO, "Registered listeners!");
        // TODO: commands
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
        //TODO
    }

    private void generateConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("MySQL.username", "root");
        config.addDefault("MySQL.password", "123");
        config.addDefault("MySQL.table", "dotaRanking");
        config.addDefault("MySQL.url.host", "localhost");
        config.addDefault("MySQL.url.port", 3306);
        config.addDefault("MySQL.url.database", "minecraft");
        config.addDefault("config.locations.worldName", "World");
        config.addDefault("config.locations.bluespawn.x", -945);
        config.addDefault("config.locations.bluespawn.y", 49);
        config.addDefault("config.locations.bluespawn.z", 195);
        config.addDefault("config.locations.redspawn.x", -1188);
        config.addDefault("config.locations.redspawn.y", 49);
        config.addDefault("config.locations.redspawn.z", 440);
        config.options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        url = "jdbc:mysql://" + config.getString("MySQL.url.host") + ":" + config.getInt("MySQL.url.port") + "/" + config.getString("MySQL.url.database");
        username = config.getString("MySQL.username");
        password = config.getString("MySQL.password");
        tableName = config.getString("MySQL.table");
        worldName = config.getString("config.locations.spawn.worldName");

        BlueSpawn = new Location(getServer().getWorld(worldName), config.getDouble("config.locations.bluespawn.x"), config.getDouble("config.locations.bluespawn.y"), config.getDouble("config.locations.bluespawn.z"));
        RedSpawn = new Location(getServer().getWorld(worldName), config.getDouble("config.locations.bluespawn.x"), config.getDouble("config.locations.bluespawn.y"), config.getDouble("config.locations.bluespawn.z"));
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
        players.put(p, new Jogador(this, p, 0, 0, 0, 0, 0, team));
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
        } else {
            p.setCustomName(ChatColor.RED + p.getName());
            p.teleport(RedSpawn);
            p.setBedSpawnLocation(RedSpawn);
            redTeam++;
        }
        p.setCustomNameVisible(true);

        if (players.size() >= 6) {
            //TODO: start game
            return;
        } else {
            broadcastMsg(ChatColor.GOLD + "[Dota] " + ChatColor.RED + "Jogadores esperando: " + players.size() + " de 6 minimo. Para entrar use " + ChatColor.YELLOW + "/join");
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
}
