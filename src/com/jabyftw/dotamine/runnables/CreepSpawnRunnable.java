package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackMelee;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackRanged;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIFloat;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AILookAtEntity;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.AttributeModifierFactory;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.ModifyOperation;
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

    public CreepSpawnRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Location loc : pl.creepSpawn.keySet()) {
            int team = getTeam(loc);
            for (int i = 0; i < 4; i++) {
                Zombie z = pl.getServer().getWorld(pl.worldName).spawn(loc, Zombie.class);
                ControllableMob<Zombie> cz = ControllableMobs.putUnderControl(z, true);
                cz.getAttributes().setMaximumNavigationDistance(1000);
                cz.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 0.8, ModifyOperation.ADD_TO_BASIS_VALUE));
                cz.getAI().addBehavior(new AIAttackMelee(1, 1.1));
                //cz.getAI().addBehavior(new AITargetNearest(2, 8, false));
                cz.getAI().addBehavior(new AIFloat(3));
                cz.getAI().addBehavior(new AILookAtEntity(4, (float) 8));
                cz.getActions().moveTo(pl.creepSpawn.get(loc), true);
                pl.controlMobs.put(cz, team);
                if (team == 1) {
                    pl.blueCreeps.add(cz);
                } else {
                    pl.redCreeps.add(cz);
                }
            }
            Skeleton s = pl.getServer().getWorld(pl.worldName).spawn(loc, Skeleton.class);
            ControllableMob<Skeleton> cs = ControllableMobs.putUnderControl(s, true);
            cs.getAttributes().setMaximumNavigationDistance(1000);
            cs.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 0.2, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getAI().addBehavior(new AIAttackRanged(1, 1.2, 20));
            //cs.getAI().addBehavior(new AITargetNearest(2, 20, false));
            cs.getAI().addBehavior(new AIFloat(3));
            cs.getAI().addBehavior(new AILookAtEntity(4, (float) 20));
            cs.getActions().moveTo(pl.creepSpawn.get(loc), true);
            pl.controlMobs.put(cs, team);
            if (team == 1) {
                pl.blueRangedCreeps.add(cs);
            } else {
                pl.redRangedCreeps.add(cs);
            }
        }
    }

    private int getTeam(Location loc) {
        //TODO: get all locs, return specific team for the nearest spawn point
        return 1;
    }
}
