package com.jabyftw.dotamine.commands;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Ranking;
import com.jabyftw.dotamine.Structure;
import com.jabyftw.dotamine.runnables.StartGameRunnable;
import com.jabyftw.dotamine.runnables.item.ItemCDRunnable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Rafael
 */
public class DotaCommand implements CommandExecutor {

    private final DotaMine pl;

    public DotaCommand(DotaMine plugin) {
        this.pl = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            return false;
        } else if (args[0].equalsIgnoreCase("forcestart")) {
            if (sender.hasPermission("dotamine.forcestart")) {
                if (pl.state == pl.PLAYING) {
                    sender.sendMessage(pl.getLang("lang.alreadyStarted"));
                    return true;
                } else {
                    if (pl.queue.size() > 0) {
                        sender.sendMessage(pl.getLang("lang.forcingStart"));
                        pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StartGameRunnable(pl, true));
                        return true;
                    } else {
                        sender.sendMessage(pl.getLang("lang.nobodyOnQueue"));
                        return true;
                    }
                }
            } else {
                sender.sendMessage(pl.getLang("lang.noPermission"));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("ranking")) {
            if (sender.hasPermission("dotamine.ranking")) {
                if (pl.rankingList.size() > 0 && pl.mysqlEnabled) {
                    sender.sendMessage(pl.getLang("lang.rankingTitle"));
                    int i = 0;
                    while (i < 5) {
                        for (Ranking r : pl.rankingList) {
                            sender.sendMessage(pl.getLang("lang.rankingEntry").replaceAll("%name", r.getName()).replaceAll("%wins", r.getWins()).replaceAll("%loses", r.getLoses()).replaceAll("%kills", r.getKills()).replaceAll("%deaths", r.getDeaths()).replaceAll("%kdr", r.getKillDeathRatio()).replaceAll("%wlr", r.getWinLossRatio()).replaceAll("%avgLH", r.getAvgLH()));
                            i++;
                        }
                    }
                    return true;
                } else {
                    sender.sendMessage(pl.getLang("lang.noRankingFound"));
                    return true;
                }
            } else {
                sender.sendMessage(pl.getLang("lang.noPermission"));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("tp")) {
            if (args.length > 1) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    if (pl.ingameList.containsKey(p)) {
                        if (!pl.tpCD.contains(p)) {
                            ItemStack onHand = p.getInventory().getItemInHand();
                            if (onHand.getType().equals(Material.PAPER)) {
                                int toGo; // 1 = top, 2 = mid, 3 = bot, 4 base
                                if (args[1].startsWith("t")) {
                                    toGo = 1;
                                } else if (args[1].startsWith("m")) {
                                    toGo = 2;
                                } else if (args[1].startsWith("bo")) {
                                    toGo = 3;
                                } else {
                                    toGo = 4;
                                }
                                if (onHand.getAmount() > 1) {
                                    onHand.setAmount(onHand.getAmount() - 1);
                                } else {
                                    p.getInventory().remove(onHand);
                                }
                                useTp(p, toGo);
                                pl.tpCD.add(p);
                                pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new ItemCDRunnable(pl, p, 3), 20 * 90);
                                return true;
                            } else {
                                sender.sendMessage(pl.getLang("lang.noPaperOnHand"));
                                return true;
                            }
                        } else {
                            sender.sendMessage(pl.getLang("lang.itemCDMessage").replaceAll("%item", "TP Scroll"));
                            return true;
                        }
                    } else {
                        sender.sendMessage(pl.getLang("lang.notEvenPlaying"));
                        return true;
                    }
                } else {
                    sender.sendMessage(pl.getLang("lang.onlyIngame"));
                    return true;
                }
            } else {
                sender.sendMessage(pl.getLang("lang.tpCommand"));
                return true;
            }
        } else {
            /*Location l = ((Player) sender).getLocation();
             sender.sendMessage(l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ() + " " + l.getYaw() + " " + l.getPitch());
             pl.ingameList.get((Player) sender).addWin();*/
            return false;
        }
    }

    private void useTp(Player p, int togo) {
        if (togo == 1) { // TOP
            for (int i = 0; i < pl.maxN; i++) {
                for (Structure s : pl.structures.keySet()) {
                    if (s.getLane().startsWith("t")) {
                        if (i == pl.structures.get(s) && !s.isDestroyed() && s.getTeam() == pl.ingameList.get(p).getTeam() && s.getType() != 2) { // just towers
                            tp(p, s.getTpLoc());
                            return;
                        }
                    }
                }
            }
        } else if (togo == 2) { // MID
            for (int i = 0; i < pl.maxN; i++) {
                for (Structure s : pl.structures.keySet()) {
                    if (s.getLane().startsWith("m")) {
                        if (i == pl.structures.get(s) && !s.isDestroyed() && s.getTeam() == pl.ingameList.get(p).getTeam() && s.getType() != 2) {
                            tp(p, s.getTpLoc());
                            return;
                        }
                    }
                }
            }
        } else if (togo == 3) { // BOT
            for (int i = 0; i < pl.maxN; i++) {
                for (Structure s : pl.structures.keySet()) {
                    if (s.getLane().startsWith("bo")) {
                        if (i == pl.structures.get(s) && !s.isDestroyed() && s.getTeam() == pl.ingameList.get(p).getTeam() && s.getType() != 2) {
                            tp(p, s.getTpLoc());
                            return;
                        }
                    }
                }
            }
        } else { // Base
            if (pl.ingameList.get(p).getTeam() == 1) {
                tp(p, pl.blueDeploy);
            } else {
                tp(p, pl.redDeploy);
            }
        }
    }

    private void tp(Player p, Location destination) {
        p.sendMessage(pl.getLang("lang.tpDontMove"));
        int TPTask = pl.getServer().getScheduler().scheduleSyncRepeatingTask(pl, new TeleportEffectRunnable(p, destination), 2, 2);
        pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new TeleportRunnable(p, destination), 20 * 3);
        pl.teleporting.put(p, TPTask);
    }

    private class TeleportRunnable implements Runnable {

        private final Player p;
        private final Location destination;

        public TeleportRunnable(Player p, Location destination) {
            this.p = p;
            this.destination = destination;
        }

        @Override
        public void run() {
            if (pl.teleporting.containsKey(p)) {
                p.teleport(destination);
                destination.getWorld().playSound(destination, Sound.PORTAL_TRIGGER, 1, 0);
                pl.getServer().getScheduler().cancelTask(pl.teleporting.get(p));
                pl.teleporting.remove(p);
            } // else, was removed before, dont execute
        }
    }

    private class TeleportEffectRunnable implements Runnable {

        private final Player p;
        private final Location destination;

        public TeleportEffectRunnable(Player p, Location destination) {
            this.p = p;
            this.destination = destination;
        }

        @Override
        public void run() {
            pl.breakEffect(destination, 2, 11);
            pl.breakEffect(p.getLocation(), 2, 55);
        }
    }
}
