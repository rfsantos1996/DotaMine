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
public class JungleSpawnRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public JungleSpawnRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Location loc : pl.jungleSpawn) {
            loc.getChunk().load();
            boolean playernear = false;
            boolean spawn = true;
            for (Player p : pl.ingameList.keySet()) {
                if (p.getLocation().distance(loc) < 32) {
                    playernear = true;
                }
            }
            if (pl.jungleCreeps.size() > 0 && playernear) {
                for (ControllableMob cm : pl.jungleCreeps) {
                    if (cm.getEntity().getLocation().distance(loc) < 12 && !cm.getEntity().isDead()) {
                        spawn = false;
                    }
                }
            }
            if (spawn && playernear) {
                pl.spawnJungle(loc);
            }
        }
    }
}
