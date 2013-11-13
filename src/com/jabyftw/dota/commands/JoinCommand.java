package com.jabyftw.dota.commands;

import com.jabyftw.dota.DotaMine;
import org.bukkit.ChatColor;
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
    public JoinCommand(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(p.hasPermission("dotamine.play")) {
                if(pl.players.containsKey(p)) {
                    p.sendMessage(ChatColor.DARK_RED + "You are already playing!");
                    return true;
                } else {
                    if(pl.players.size() > 12) {
                        p.sendMessage(ChatColor.RED + "The game is full :/");
                        return true;
                    } else {
                        pl.addPlayer(p, pl.getTeam());
                        return true;
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "You dont have permission!");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Just players can use this.");
            return true;
        }
    }
}
