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
import com.jabyftw.dotamine.runnables.EntitySpecialJungleSpawnRunnable;
import com.jabyftw.dotamine.runnables.JungleSpawnRunnable;
import com.jabyftw.dotamine.runnables.JungleSpecialSpawnRunnable;
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
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class DotaMine extends JavaPlugin implements Listener {

    public MySQL sql;
    public String worldName;
    public Config config = null;
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
    public int redCount, blueCount, state, scoreRunnable, version, MIN_PLAYERS, MAX_PLAYERS, announceQueue, AncientHP, TowerRange;
    public boolean useVault, nerfRanged, mysqlEnabled, useEffects, megaCreeps, useControllableMobs, debug, restartAfter;
    public boolean restarted = false;
    public Economy econ = null;
    public Permission permission = null;
    private List<String> list = new ArrayList();
    // Location
    public Map<Structure, Integer> structures;
    public int maxN = 0;
    public int minN = 1;
    public List<Location> botCreepSpawn, midCreepSpawn, topCreepSpawn;
    public Map<Location, Boolean> jungleRedSpawn, jungleBlueSpawn, jungleSpawn;
    public Location redDeploy, blueDeploy, specDeploy, normalSpawn, otherWorldSpawn;
    // Players
    public List<Ranking> rankingList;
    public Map<Player, Jogador> ingameList;
    public Map<Player, Integer> queue;
    public Map<Player, Spectator> spectators;
    public Map<Player, ItemStack[]> playerDeathItems, playerDeathArmor;
    // Mobs
    public Map<Entity, ControllableMob> controlMobs, jungleCreeps, laneCreeps;
    public Map<Entity, Integer> jungleSpecialCreeps, jungleEntityCreeps;
    public List<Entity> spawnedMobs, laneEntityCreeps;
    public Random random = new Random();
    /*
     ITEM
     */
    public List<Player> shadowCD, forceCD, tpCD, interactCD, hasTarrasque, tarrasqueRecentlyDamaged, smokeCD;
    public Map<Player, Integer> invisible, invisibleSB, invisibleW, invisibleEffectW, forcingStaff, teleporting, respawning;

    private void startVariables() { // or restart
        structures = new HashMap();
        botCreepSpawn = new ArrayList();
        midCreepSpawn = new ArrayList();
        topCreepSpawn = new ArrayList();
        jungleSpawn = new HashMap();
        jungleRedSpawn = new HashMap();
        jungleBlueSpawn = new HashMap();

        rankingList = new ArrayList();
        ingameList = new HashMap();
        queue = new HashMap();
        spectators = new HashMap();
        playerDeathItems = new HashMap();
        playerDeathArmor = new HashMap();

        controlMobs = new HashMap();
        jungleCreeps = new HashMap();
        jungleSpecialCreeps = new HashMap();
        laneCreeps = new HashMap();
        spawnedMobs = new ArrayList();
        laneEntityCreeps = new ArrayList();
        jungleEntityCreeps = new HashMap();

        shadowCD = new ArrayList();
        invisible = new HashMap();
        invisibleSB = new HashMap();
        invisibleW = new HashMap();
        invisibleEffectW = new HashMap();
        forceCD = new ArrayList();
        forcingStaff = new HashMap();
        tpCD = new ArrayList();
        interactCD = new ArrayList();
        teleporting = new HashMap();
        respawning = new HashMap();
        hasTarrasque = new ArrayList();
        tarrasqueRecentlyDamaged = new ArrayList();
        smokeCD = new ArrayList();
    }

    @Override
    public void onEnable() {
        startVariables();
        state = WAITING;
        megaCreeps = false;
        version = 4; // config version
        config = new Config(this);
        config.generateConfig();
        if (useVault) {
            setupVault();
        }
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getLogger().log(Level.INFO, "Registered listeners.");
        getServer().getPluginCommand("join").setExecutor(new JoinCommand(this));
        list.add("j");
        getServer().getPluginCommand("join").setAliases(list);
        getServer().getPluginCommand("spectate").setExecutor(new SpectateCommand(this));
        getServer().getPluginCommand("dota").setExecutor(new DotaCommand(this));
        getLogger().log(Level.INFO, "Registered commands.");
        announceQueue = getServer().getScheduler().scheduleSyncRepeatingTask(this, new AnnounceQueueRunnable(this), 20 * 15, 20 * 20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new TarrasqueRunRunnable(), 20 * 2, 20 * 2);
        if (mysqlEnabled) {
            getServer().getScheduler().scheduleAsyncRepeatingTask(this, new RankingUpdateRunnable(), 10, 20 * 60);
        }
        getLogger().log(Level.INFO, "Registered runnables.");
    }

    @Override
    public void onDisable() {
        unloadGameWorld();
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
        return config.lang.getString(path).replaceAll("&", "§");
    }

    public void broadcast(String msg) {
        for (Player p : getServer().getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }

    public void debug(String msg) {
        if (debug) {
            getLogger().log(Level.OFF, "DEBUG: " + msg);
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
                p.getInventory().addItem(sword);
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
        p.setFallDistance(0);
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
        debug("state = spawning");
        getServer().getScheduler().scheduleSyncDelayedTask(this, new AnnounceGameRunnable(), 20 * 50);
        if (useControllableMobs) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new CreepSpawnRunnable(this), 20 * 60, 20 * 30);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new JungleSpawnRunnable(this), 20 * 60, 20 * 15);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new JungleSpecialSpawnRunnable(this), 20 * 60, 20 * 15);
        } else {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new EntityCreepSpawnRunnable(this), 20 * 60, 20 * 30);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new EntityJungleSpawnRunnable(this), 20 * 60, 20 * 15);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new EntitySpecialJungleSpawnRunnable(this), 20 * 60, 20 * 15);
            getLogger().log(Level.WARNING, "You should use ControllableMobs for better experience.");
        }
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new ScoreboardRunnable(this), 1, scoreRunnable);
        if (nerfRanged) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new CheckNightRunnable(this), 20, 20 * 3);
        }
        getServer().getWorld(worldName).setTime(0); // start as day
        debug("prepared to start");
    }

    public void endGame(boolean forced, int winTeam) {
        state = RESTARTING;
        debug("state = restarting");
        if (!forced) {
            for (Jogador j : ingameList.values()) {
                if (j.getTeam() == winTeam) {
                    j.addWin();
                } else {
                    j.addLose();
                }
            }
        }
        if (restartAfter) {
            getServer().setWhitelist(true);
            getServer().getScheduler().scheduleSyncDelayedTask(this, new StopRunnable(), 20 * 5);
        } else {
            getServer().getScheduler().scheduleSyncDelayedTask(this, new UnloadRunnable(), 20 * 5);
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

    public void flamesEffect(Location location, int quantity) {
        if (useEffects) {
            for (int i = 0; i < quantity; i++) {
                location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0, 1);
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

    public void checkForMegacreeps() {
        int i = 0;
        for (Structure s : structures.keySet()) {
            if (structures.get(s) == minN && s.isDestroyed()) { // All first 6 towers must be destroyed on both teams
                i++;
            }
        }
        if (i > 5) {
            megaCreeps = true;
        }
    }

    public void addCreepLocSpawn(String lane, Location loc) {
        if (lane.startsWith("b")) {
            botCreepSpawn.add(loc);
            debug("added creep spawn b");
        } else if (lane.startsWith("m")) {
            midCreepSpawn.add(loc);
            debug("added creep spawn m");
        } else {
            topCreepSpawn.add(loc);
            debug("added creep spawn t");
        }
    }

    private void unloadGameWorld() {
        if (getServer().getWorld(worldName) != null) {
            for (int a = -getConfig().getInt("config.world.fromChunkX"); a <= getConfig().getInt("config.world.toChunkX"); a++) {
                for (int b = -getConfig().getInt("config.world.fromChunkY"); b <= getConfig().getInt("config.world.toChunkY"); b++) {
                    getServer().getWorld(worldName).unloadChunk(a, b, false, false);
                }
            }
            for (Player p : ingameList.keySet()) {
                p.teleport(otherWorldSpawn);
            }
            for (Player p : spectators.keySet()) {
                p.teleport(otherWorldSpawn);
            }
            for (Player p : getServer().getWorld(worldName).getPlayers()) {
                p.teleport(otherWorldSpawn);
            }
            getServer().unloadWorld(worldName, false);
            getLogger().log(Level.INFO, "Unloaded game world.");
            getServer().getScheduler().cancelTasks(this);
        }
    }

    private class UnloadRunnable implements Runnable {

        @Override
        public void run() {
            if (mysqlEnabled) {
                sql.closeConn();
            }
            unloadGameWorld();
            onEnable();
        }
    }

    private class RankingUpdateRunnable extends BukkitRunnable {

        @Override
        public void run() {
            try {
                Statement s = sql.getConn().createStatement();
                ResultSet rs = s.executeQuery("SELECT `name`, `wins`, `loses`, `kills`, `deaths`, `lhs` FROM `dotamine` ORDER BY `wins` DESC LIMIT 6;");
                rankingList.clear();
                while (rs.next()) {
                    rankingList.add(new Ranking(rs.getString("name"), rs.getInt("wins"), rs.getInt("loses"), rs.getInt("kills"), rs.getInt("deaths"), rs.getInt("lhs")));
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
            for (Player p : getServer().getOnlinePlayers()) {
                p.kickPlayer(getLang("lang.kickMessage"));
            }
            getServer().shutdown();
        }
    }

    private class AnnounceGameRunnable implements Runnable {

        @Override
        public void run() {
            state = PLAYING;
            debug("state = playing");
            broadcast(getLang("lang.theGamehasStarted"));
            broadcast(getLang("lang.creepsWillSpawn"));
        }
    }
}
