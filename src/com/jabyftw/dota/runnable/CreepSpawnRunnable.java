package com.jabyftw.dota.runnable;

import com.jabyftw.dota.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.Attribute;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.AttributeModifierFactory;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.ModifyOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class CreepSpawnRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private final List<ControllableMob> cm = new ArrayList();
    private final List<Double> d = new ArrayList();
    private double speedV, dmgV;

    public CreepSpawnRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Location loc : pl.mobSpawn) {
            for (int i = 0; i < 4; i++) {
                spawnZombie(loc);
            }
            spawnSkeleton(loc);
        }
        if(pl.bigCreeps) {
            speedV = 0.8;
            dmgV = 3;
        } else {
            speedV = 0.2;
            dmgV = 0;
        }
        for (ControllableMob cmob : cm) { //TOOD: make AI, remove Drops, make them not to burn
            cmob.getAttributes().setMaximumNavigationDistance(1500);
            Attribute speed = cmob.getAttributes().getMovementSpeedAttribute();
            speed.attachModifier(AttributeModifierFactory.create(UUID.fromString("DotaMine-" + randomValue()), "speed", 0.5, ModifyOperation.ADD_TO_BASIS_VALUE));
            Attribute dmg = cmob.getAttributes().getAttackDamageAttribute();
            dmg.attachModifier(AttributeModifierFactory.create(UUID.fromString("DotaMine-" + randomValue()), "dmg", 2, ModifyOperation.ADD_TO_BASIS_VALUE));
        }
    }

    private int randomValue() {
        double i = Math.random() * 1000;
        if (!d.contains(i)) {
            d.add(i);
            return (int) i;
        } else {
            return randomValue();
        }
    }

    private void spawnZombie(Location loc) {
        Zombie zombie = pl.getServer().getWorld(pl.worldName).spawn(loc, Zombie.class);
        ControllableMob<Zombie> czombie = ControllableMobs.putUnderControl(zombie, true);
        cm.add(czombie);
    }

    private void spawnSkeleton(Location loc) {
        Skeleton skel = pl.getServer().getWorld(pl.worldName).spawn(loc, Skeleton.class);
        ControllableMob<Skeleton> cskel = ControllableMobs.putUnderControl(skel, true);
        cm.add(cskel);
    }
}
