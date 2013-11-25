package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class TargetEnemyRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public TargetEnemyRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (ControllableMob cm : pl.controlMobs.keySet()) {
            Location cmLoc = cm.getEntity().getLocation();
            /*for(Location loc : pl.creepSpawn.values()) {
                if(cmLoc.distance(loc) < 10) {
                    //TODO: put new destination
                }
            }*/
            int team = pl.controlMobs.get(cm);
            if (team == 1) {
                for (ControllableMob enemy : pl.redCreeps) {
                    if (enemy.getEntity().getLocation().distance(cmLoc) < 8) {
                        cm.getActions().target(enemy.getEntity(), false);
                        return;
                    }
                }
                for (Player enemy : pl.redPlayers) {
                    if (enemy.getLocation().distance(cmLoc) < 12) {
                        cm.getActions().target(enemy, false);
                        return;
                    }
                }
                for (ControllableMob enemy : pl.redRangedCreeps) {
                    if (enemy.getEntity().getLocation().distance(cmLoc) < 20) {
                        cm.getActions().target(enemy.getEntity(), false);
                        return;
                    }
                }
            } else {
                for (ControllableMob enemy : pl.blueCreeps) {
                    if (enemy.getEntity().getLocation().distance(cmLoc) < 8) {
                        cm.getActions().target(enemy.getEntity(), false);
                        return;
                    }
                }
                for (Player enemy : pl.bluePlayers) {
                    if (enemy.getLocation().distance(cmLoc) < 12) {
                        cm.getActions().target(enemy, false);
                        return;
                    }
                }
                for (ControllableMob enemy : pl.blueRangedCreeps) {
                    if (enemy.getEntity().getLocation().distance(cmLoc) < 20) {
                        cm.getActions().target(enemy.getEntity(), false);
                        return;
                    }
                }
            }
        }
    }
}
