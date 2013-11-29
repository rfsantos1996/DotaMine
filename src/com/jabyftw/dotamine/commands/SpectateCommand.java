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
public class SpectateCommand implements CommandExecutor {

    private final DotaMine pl;

    public SpectateCommand(DotaMine plugin) {
        this.pl = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (p.hasPermission("dotamine.spectate")) {
                if (pl.state == pl.PLAYING || pl.state == pl.SPAWNING) {
                    if (pl.spectators.containsKey(p)) {
                        if (args.length > 0) {
                            sender.sendMessage(pl.getLang("lang.leftSpectator"));
                            pl.removeSpectator(p);
                            return true;
                        } else {
                            sender.sendMessage(pl.getLang("lang.alreadySpectating"));
                            return true;
                        }
                    } else {
                        pl.addSpectator(p);
                        return true;
                    }
                } else {
                    p.sendMessage(pl.getLang("lang.gameNotStarted"));
                    return true;
                }
            } else {
                p.sendMessage(pl.getLang("lang.noPermission"));
                return true;
            }
        } else {
            sender.sendMessage(pl.getLang("lang.onlyIngame"));
            return true;
        }
    }
}
