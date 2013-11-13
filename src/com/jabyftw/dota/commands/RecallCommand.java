package com.jabyftw.dota.commands;

import com.jabyftw.dota.DotaMine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Rafael
 */
public class RecallCommand implements CommandExecutor {
    
    private final DotaMine pl;
    public RecallCommand(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return false;//TODO: finish
    }
    
}
