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
public class CreepSpawnRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public CreepSpawnRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Location spawnloc : pl.botCreepSpawn) {
            spawn(spawnloc);
        }
        for (Location spawnloc : pl.topCreepSpawn) {
            spawn(spawnloc);
        }
        for (Location spawnloc : pl.midCreepSpawn) {
            spawn(spawnloc);
        }
    }

    private void spawn(Location spawnloc) {
        for (Player p : pl.ingameList.keySet()) {
            if (p.getLocation().distance(spawnloc) < 24) {
                if (pl.laneCreeps.size() > 0) {
                    int spawn = 0;
                    for (ControllableMob cm : pl.laneCreeps) {
                        if (cm.getEntity().getLocation().distance(spawnloc) < 18) {
                            spawn++;
                        }
                    }
                    if (spawn < 2) {
                        spawnloc.getChunk().load();
                        pl.spawnLaneCreeps(spawnloc);
                    }
                } else {
                    spawnloc.getChunk().load();
                    pl.spawnLaneCreeps(spawnloc);
                }
            }
        }
    }
}
