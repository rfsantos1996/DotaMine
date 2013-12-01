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
import com.jabyftw.dotamine.runnables.EntityCreepSpawnRunnable;
import com.jabyftw.dotamine.runnables.EntityJungleSpawnRunnable;
import com.jabyftw.dotamine.runnables.JungleSpawnRunnable;
import com.jabyftw.dotamine.runnables.ScoreboardRunnable;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable; // TODO: better SQL, k/d, avg. LH per game

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
    public int redCount, blueCount, state, scoreRunnable, version, MIN_PLAYERS, MAX_PLAYERS, announceQueue;
    public boolean useVault, nerfRanged, mysqlEnabled, useEffects, megaCreeps, useControllableMobs;
    public Economy econ = null;
    public Config config = new Config(this);
    public Permission permission = null;
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
    public List<Entity> spawnedMobs = new ArrayList();
    public List<Entity> laneEntityCreeps = new ArrayList();
    public List<Entity> jungleEntityCreeps = new ArrayList();
    public Location botSpawnPre, botSpawnPosR, botSpawnPosB, midSpawnPre, midSpawnPosR, midSpawnPosB, topSpawnPre, topSpawnPosR, topSpawnPosB, blueJungleBot, blueJungleTop, redJungleBot, redJungleTop, blueSBotT, blueSMidT, blueSTopT, redSBotT, redSMidT, redSTopT, blueFBotT, blueFMidT, blueFTopT, redFBotT, redFMidT, redFTopT, blueDeploy, redDeploy, normalSpawn, specDeploy, blueAncient, redAncient;
    public Random random = new Random();
    /*
     ITEM
     */
    public List<Player> shadowCD = new ArrayList();
    public Map<Player, Integer> invisible = new HashMap();
    public Map<Player, Integer> invisibleSB = new HashMap();
    public Map<Player, Integer> invisibleW = new HashMap();
    public Map<Player, Integer> invisibleEffectW = new HashMap();
    public List<Player> forceCD = new ArrayList();
    public Map<Player, Integer> forcingStaff = new HashMap();
    public List<Player> tpCD = new ArrayList();
    public List<Player> interactCD = new ArrayList();
    public Map<Player, Integer> teleporting = new HashMap();
    public Map<Player, Integer> respawning = new HashMap();
    public List<Player> hasTarrasque = new ArrayList();
    public List<Player> tarrasqueRecentlyDamaged = new ArrayList();
    public List<Player> smokeCD = new ArrayList();

    @Override
    public void onEnable() {
        state = WAITING;
        megaCreeps = false;
        version = 3; // config version
        config.generateConfig(getConfig());
        config.setLocations(getServer().getWorld(worldName));
        if (useVault) {
            setupVault();
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
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new TarrasqueRunRunnable(), 20 * 2, 20 * 2);
        if (mysqlEnabled) {
            getServer().getScheduler().scheduleAsyncRepeatingTask(this, new RankingUpdateRunnable(), 10, 20 * 60);
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

    private void setupVault() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
    }

    public String getLang(String path) {
        return getConfig().getString(path).replaceAll("&", "§");
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
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10, 100));
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
        getServer().getScheduler().scheduleSyncDelayedTask(this, new AnnounceGameRunnable(), 20 * 50);
        if (useControllableMobs) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new CreepSpawnRunnable(this), 20 * 60, 20 * 30);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new JungleSpawnRunnable(this), 20 * 60, 20 * 40);
        } else {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new EntityCreepSpawnRunnable(this), 20 * 60, 20 * 30);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new EntityJungleSpawnRunnable(this), 20 * 60, 20 * 40);
            getLogger().log(Level.WARNING, "You should use ControllableMobs for better experience.");
        }
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new ScoreboardRunnable(this), 1, scoreRunnable);
        if (nerfRanged) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new CheckNightRunnable(this), 20, 20 * 3);
        }
        getServer().getWorld(worldName).setTime(0); // start as day
    }

    public void endGame(boolean forced, int winTeam) {
        state = RESTARTING;
        if (!forced) {
            for (Jogador j : ingameList.values()) {
                if (j.getTeam() == winTeam) {
                    j.addWin();
                } else {
                    j.addLose();
                }
            }
        }
        getServer().setWhitelist(true);
        getServer().getScheduler().scheduleSyncDelayedTask(this, new StopRunnable(), 20 * 10);
        for (Player p : getServer().getOnlinePlayers()) {
            p.kickPlayer(getLang("lang.kickMessage"));
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

    public void removeTarrasque(Player damaged) {
        damaged.removePotionEffect(PotionEffectType.REGENERATION);
        tarrasqueRecentlyDamaged.add(damaged);
        getServer().getScheduler().scheduleSyncDelayedTask(this, new TarrasqueRemRDRunnable(damaged), 20 * 14);
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

    private class RankingUpdateRunnable extends BukkitRunnable {

        @Override
        public void run() {
            try {
                Statement s = sql.getConn().createStatement();
                ResultSet rs = s.executeQuery("SELECT `name`, `wins`, `loses`, `kills`, `deaths` FROM `dotamine` ORDER BY `wins` DESC LIMIT 6;");
                rankingList.clear();
                while (rs.next()) {
                    rankingList.add(new Ranking(rs.getString("name"), rs.getInt("wins"), rs.getInt("loses"), rs.getInt("kills"), rs.getInt("deaths")));
                }
            } catch (SQLException ex) {
                getLogger().log(Level.SEVERE, "Couldn''t connect to MySQL: {0}", ex.getMessage());
            }
        }
    }

    private class TarrasqueRunRunnable implements Runnable {

        @Override
        public void run() {
            for (Player p : ingameList.keySet()) {
                checkForTarrasque(p);
                if (hasTarrasque.contains(p)) {
                    if (!tarrasqueRecentlyDamaged.contains(p)) {
                        p.removePotionEffect(PotionEffectType.REGENERATION);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 4, 1), true);
                    }
                }
            }
        }
    }

    private class TarrasqueRemRDRunnable implements Runnable {

        private final Player damaged;

        public TarrasqueRemRDRunnable(Player damaged) {
            this.damaged = damaged;
        }

        @Override
        public void run() {
            tarrasqueRecentlyDamaged.remove(damaged);
        }
    }

    private class StopRunnable implements Runnable {

        @Override
        public void run() {
            getServer().shutdown();
        }
    }

    private class AnnounceGameRunnable implements Runnable {

        @Override
        public void run() {
            state = PLAYING;
            broadcast(getLang("lang.theGamehasStarted"));
            broadcast(getLang("lang.creepsWillSpawn"));
        }
    }
}
