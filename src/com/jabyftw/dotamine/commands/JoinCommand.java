package com.jabyftw.dotamine.commands;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Rafael
 */
public class JoinCommand implements CommandExecutor {

    private final DotaMine pl;

    public JoinCommand(DotaMine plugin) {
        this.pl = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("dotamine.play")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (args.length > 0) {
                    if (!pl.queue.containsKey(p)) {
                        int attackType;
                        if (args[0].startsWith("r")) { // Ranged = 2
                            attackType = 2;
                            p.sendMessage(pl.getLang("lang.settedRanged"));
                        } else {
                            attackType = 1; // Meele = 1
                            p.sendMessage(pl.getLang("lang.settedMeele"));
                        }
                        pl.addPlayerToGame(p, attackType);
                        return true;
                    } else {
                        if (args[0].startsWith("r")) {
                            sender.sendMessage(pl.getLang("lang.alreadyInQueueUpdatedAttack").replaceAll("%attack", getAttackType(2)));
                            pl.queue.put(p, 2);
                        } else if (args[0].startsWith("m")) {
                            sender.sendMessage(pl.getLang("lang.alreadyInQueueUpdatedAttack").replaceAll("%attack", getAttackType(1)));
                            pl.queue.put(p, 1);
                        } else {
                            sender.sendMessage(pl.getLang("lang.leftQueue"));
                            pl.removePlayerFromQueue(p);
                        }
                        return true;
                    }
                } else {
                    p.sendMessage(pl.getLang("lang.usePlayCommand"));
                    return true;
                }
            } else {
                sender.sendMessage(pl.getLang("lang.onlyIngame"));
                return true;
            }
        } else {
            sender.sendMessage(pl.getLang("lang.noPermission"));
            return true;
        }
    }

    private String getAttackType(int i) {
        if (i == 1) {
            return "meele";
        } else {
            return "ranged";
        }
    }

}
