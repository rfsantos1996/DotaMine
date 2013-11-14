package com.jabyftw.dota.runnable;

import com.jabyftw.dota.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackMelee;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackRanged;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIBehavior;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIFloat;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AILookAtEntity;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.Attribute;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.AttributeModifierFactory;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.ModifyOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class CreepSpawnRunnable extends BukkitRunnable {
//TODO: fix all!
    private final DotaMine pl;
    private final List<Double> d = new ArrayList();
    private double speedV, dmgV;
    private final AILookAtEntity AIleNear;
    private final AILookAtEntity AIleDist;
    private final AIFloat AIf;
    private final AIAttackMelee AInear;
    private final AIAttackRanged AIdist;

    public CreepSpawnRunnable(DotaMine pl) {
        this.pl = pl;
        AIleNear = new AILookAtEntity(3, 8);
        AIleDist = new AILookAtEntity(3, 20);
        AIf = new AIFloat();
        AInear = new AIAttackMelee(1, 1.25);
        AIdist = new AIAttackRanged(1, 1.1, 22, 30);
    }

    @Override
    public void run() {
        if (pl.bigCreeps) {
            speedV = 0.6;
            dmgV = 3;
        } else {
            speedV = 0.2;
            dmgV = 0;
        }
        for (Location loc : pl.mobSpawn) {
            Location useLoc = loc;
            int locX = loc.getBlockX();
            for (int i = 0; i < 4; i++) {
                useLoc.setX(locX);
                locX++;
                spawnZombie(useLoc);
            }
            useLoc.setX(locX);
            spawnSkeleton(useLoc);
        }
    }

    private int randomValue() {
        double i = Math.random() * 100000;
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
        czombie.getAttributes().setMaximumNavigationDistance(1500);
        czombie.getAI().addBehavior(AIleNear);
        czombie.getAI().addBehavior(AIf);
        czombie.getAI().addBehavior(AInear);
        try {
            czombie.getAttributes().getMovementSpeedAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "speed melee", speedV, ModifyOperation.ADD_TO_BASIS_VALUE));
            czombie.getAttributes().getAttackDamageAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "dmg melee", dmgV, ModifyOperation.ADD_TO_BASIS_VALUE));
        } catch (IllegalArgumentException e) {
            pl.getLogger().log(Level.OFF, e.getLocalizedMessage());
        }
        pl.controllablemobs.add(czombie);
    }

    private void spawnSkeleton(Location loc) {
        Skeleton skel = pl.getServer().getWorld(pl.worldName).spawn(loc, Skeleton.class);
        ControllableMob<Skeleton> cskel = ControllableMobs.putUnderControl(skel, true);
        cskel.getAttributes().setMaximumNavigationDistance(1500);
        cskel.getAI().addBehavior(AIleDist);
        cskel.getAI().addBehavior(AIf);
        cskel.getAI().addBehavior(AIdist);
        try {
            cskel.getAttributes().getMovementSpeedAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "speed ranged", speedV, ModifyOperation.ADD_TO_BASIS_VALUE));
            cskel.getAttributes().getAttackDamageAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "dmg ranged", dmgV, ModifyOperation.ADD_TO_BASIS_VALUE));
        } catch (IllegalArgumentException e) {
            pl.getLogger().log(Level.OFF, e.getLocalizedMessage());
        }
        pl.controllablemobs.add(cskel);
    }
}
