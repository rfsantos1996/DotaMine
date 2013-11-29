package com.jabyftw.dotamine.commands;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Ranking;
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
                        pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new StartGameRunnable(pl, true), 20 * 121);
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
                            sender.sendMessage(pl.getLang("lang.rankingEntry").replaceAll("%name", r.getName()).replaceAll("%wins", r.getWins()).replaceAll("%loses", r.getLoses()).replaceAll("%kills", r.getKills()).replaceAll("%deaths", r.getDeaths()));
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
                                p.getInventory().remove(onHand);
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
             sender.sendMessage(l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ() + " " + l.getYaw() + " " + l.getPitch());*/
            return false;
        }
    }

    private void useTp(Player p, int togo) {
        if (togo == 1) { // top
            if (pl.ingameList.get(p).getTeam() == 1) {
                if (pl.towers.get(pl.blueFTopT).isDestroyed()) {
                    if (pl.towers.get(pl.blueSTopT).isDestroyed()) {
                        tp(p, pl.blueDeploy);
                    } else {
                        tp(p, pl.blueSTopT);
                    }
                } else {
                    tp(p, pl.blueFTopT);
                }
            } else {
                if (pl.towers.get(pl.redFTopT).isDestroyed()) {
                    if (pl.towers.get(pl.redSTopT).isDestroyed()) {
                        tp(p, pl.redDeploy);
                    } else {
                        tp(p, pl.redSTopT);
                    }
                } else {
                    tp(p, pl.redFTopT);
                }
            }
        } else if (togo == 2) { // mid
            if (pl.ingameList.get(p).getTeam() == 1) {
                if (pl.towers.get(pl.blueFMidT).isDestroyed()) {
                    if (pl.towers.get(pl.blueSMidT).isDestroyed()) {
                        tp(p, pl.blueDeploy);
                    } else {
                        tp(p, pl.blueSMidT);
                    }
                } else {
                    tp(p, pl.blueFMidT);
                }
            } else {
                if (pl.towers.get(pl.redFMidT).isDestroyed()) {
                    if (pl.towers.get(pl.redSMidT).isDestroyed()) {
                        tp(p, pl.redDeploy);
                    } else {
                        tp(p, pl.redSMidT);
                    }
                } else {
                    tp(p, pl.redFMidT);
                }
            }
        } else if (togo == 3) { // bot
            if (pl.ingameList.get(p).getTeam() == 1) {
                if (pl.towers.get(pl.blueFBotT).isDestroyed()) {
                    if (pl.towers.get(pl.blueSBotT).isDestroyed()) {
                        tp(p, pl.blueDeploy);
                    } else {
                        tp(p, pl.blueSBotT);
                    }
                } else {
                    tp(p, pl.blueFBotT);
                }
            } else {
                if (pl.towers.get(pl.redFBotT).isDestroyed()) {
                    if (pl.towers.get(pl.redSBotT).isDestroyed()) {
                        tp(p, pl.redDeploy);
                    } else {
                        tp(p, pl.redSBotT);
                    }
                } else {
                    tp(p, pl.redFBotT);
                }
            }
        } else { // base
            if (pl.ingameList.get(p).getTeam() == 1) {
                tp(p, pl.blueDeploy);
            } else {
                tp(p, pl.redDeploy);
            }
        }
    }

    private void tp(Player p, Location location) {
        if (location.equals(pl.blueDeploy)) {
            ftp(p, pl.blueDeploy);
        } else if (location.equals(pl.redDeploy)) {
            ftp(p, pl.redDeploy);
        } else if (location.equals(pl.blueFBotT)) {
            ftp(p, pl.tpPlace.get(pl.blueFBotT));
        } else if (location.equals(pl.blueSBotT)) {
            ftp(p, pl.tpPlace.get(pl.blueSBotT));
        } else if (location.equals(pl.blueFMidT)) {
            ftp(p, pl.tpPlace.get(pl.blueFMidT));
        } else if (location.equals(pl.blueSMidT)) {
            ftp(p, pl.tpPlace.get(pl.blueSMidT));
        } else if (location.equals(pl.blueFTopT)) {
            ftp(p, pl.tpPlace.get(pl.blueFTopT));
        } else if (location.equals(pl.blueSTopT)) {
            ftp(p, pl.tpPlace.get(pl.blueSTopT));
        } else if (location.equals(pl.redFBotT)) {
            ftp(p, pl.tpPlace.get(pl.redFBotT));
        } else if (location.equals(pl.redSBotT)) {
            ftp(p, pl.tpPlace.get(pl.redSBotT));
        } else if (location.equals(pl.redFMidT)) {
            ftp(p, pl.tpPlace.get(pl.redFMidT));
        } else if (location.equals(pl.redSMidT)) {
            ftp(p, pl.tpPlace.get(pl.redSMidT));
        } else if (location.equals(pl.redFTopT)) {
            ftp(p, pl.tpPlace.get(pl.redFTopT));
        } else if (location.equals(pl.redSTopT)) {
            ftp(p, pl.tpPlace.get(pl.redSTopT));
        }
    }

    private void ftp(Player p, Location destination) {
        p.sendMessage(pl.getLang("lang.tpDontMove"));
        int TPTask = pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new TeleportRunnable(p, destination), 20 * 3);
        pl.teleporting.put(p, TPTask);
    }

    private class TeleportRunnable implements Runnable {

        private final Player p;
        private final Location destination;
        private int i = 1;

        public TeleportRunnable(Player p, Location destination) {
            this.p = p;
            this.destination = destination;
        }

        @Override
        public void run() {
            pl.breakEffect(destination, 2, 11);
            pl.breakEffect(p.getLocation(), 2, 55);
            i++;
            if (i >= 20 * 3) {
                p.teleport(destination);
                destination.getWorld().playSound(destination, Sound.PORTAL_TRIGGER, 1, 0);
                pl.teleporting.remove(p);
                pl.getServer().getScheduler().cancelTask(pl.teleporting.get(p));
            }
        }
    }
}
