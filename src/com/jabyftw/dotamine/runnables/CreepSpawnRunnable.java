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
        spawnloc.getChunk().load();
        boolean playernear = false;
        int spawn = 0;
        for (Player p : pl.ingameList.keySet()) {
            if (p.getLocation().distance(spawnloc) < 32) {
                playernear = true;
            }
        }
        if (pl.laneCreeps.size() > 0 && playernear) {
            for (ControllableMob cm : pl.laneCreeps) {
                if (cm.getEntity().getLocation().distance(spawnloc) < 22) {
                    spawn++;
                }
            }
        }
        if (spawn < 3 && playernear) {
            pl.spawnLaneCreeps(spawnloc);
        }
    }
}
