package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
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
            int team = pl.controlMobs.get(cm);
            if (team == 1) {
                for (ControllableMob enemy : pl.redCreeps) {
                    if (enemy.getEntity().getLocation().distance(cm.getEntity().getLocation()) < 8) {
                        cm.getActions().target(enemy.getEntity(), false);
                        return;
                    }
                }
                for (Player enemy : pl.redPlayers) {
                    if (enemy.getLocation().distance(cm.getEntity().getLocation()) < 12) {
                        cm.getActions().target(enemy, false);
                        return;
                    }
                }
                for (ControllableMob enemy : pl.redRangedCreeps) {
                    if (enemy.getEntity().getLocation().distance(cm.getEntity().getLocation()) < 20) {
                        cm.getActions().target(enemy.getEntity(), false);
                        return;
                    }
                }
            } else {
                for (ControllableMob enemy : pl.blueCreeps) {
                    if (enemy.getEntity().getLocation().distance(cm.getEntity().getLocation()) < 8) {
                        cm.getActions().target(enemy.getEntity(), false);
                        return;
                    }
                }
                for (Player enemy : pl.bluePlayers) {
                    if (enemy.getLocation().distance(cm.getEntity().getLocation()) < 12) {
                        cm.getActions().target(enemy, false);
                        return;
                    }
                }
                for (ControllableMob enemy : pl.blueRangedCreeps) {
                    if (enemy.getEntity().getLocation().distance(cm.getEntity().getLocation()) < 20) {
                        cm.getActions().target(enemy.getEntity(), false);
                        return;
                    }
                }
            }
        }
    }
}
