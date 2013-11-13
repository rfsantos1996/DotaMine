package com.jabyftw.dota.commands;

import com.jabyftw.dota.DotaMine;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Rafael
 */
public class DotaCommand implements CommandExecutor {
    
    private final DotaMine pl;
    public DotaCommand(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length < 1) {
            return false;
        } else if(sender.hasPermission("dotamine.dota")) {
            if(args[0].equalsIgnoreCase("help")) {
                if(sender.hasPermission("dotamine.play")) {
                    sender.sendMessage(ChatColor.GOLD + "/play" + ChatColor.RED + ": join game");
                    sender.sendMessage(ChatColor.GOLD + "/recall" + ChatColor.RED + ": teleports you to the base while playing");
                }
                if(sender.hasPermission("dotamine.spectate")) {
                    sender.sendMessage(ChatColor.GOLD + "/spectate" + ChatColor.RED + ": spectate game");
                }
                return true;
            }
            return true; //TODO: any aditional command?
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You dont have permission!");
            return true;
        }
    }
    
}
