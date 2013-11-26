package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackMelee;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackRanged;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIFloat;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AILookAtEntity;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AITargetNearest;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.AttributeModifierFactory;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.ModifyOperation;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
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
        for (Location spawnloc : pl.creepSpawn) {
            spawnloc.getChunk().load();
            boolean spawn = true;
            for (Player p : pl.ingameList.keySet()) {
                if (p.getLocation().distance(spawnloc) < 28) {
                    if (pl.laneCreeps.size() > 0) {
                        for (ControllableMob cm : pl.laneCreeps) {
                            if (cm.getEntity().getLocation().distance(spawnloc) < 18) {
                                spawn = false;
                            }
                        }
                        if (spawn) {
                            spawnLaneCreeps(spawnloc);
                        }
                    } else {
                        spawnLaneCreeps(spawnloc);
                    }
                }
            }
        }
    }

    private void spawnLaneCreeps(Location spawnloc) {
        for (int i = 0; i < 5; i++) {
            Zombie z = pl.getServer().getWorld(pl.worldName).spawn(spawnloc, Zombie.class);
            z.setRemoveWhenFarAway(false);
            ControllableMob<Zombie> cz = ControllableMobs.putUnderControl(z, true);
            cz.getAttributes().setMaximumNavigationDistance(300);
            cz.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 0.4, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "max health", 4, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAI().addBehavior(new AIAttackMelee(1, 1.1));
            cz.getAI().addBehavior(new AITargetNearest(2, 8, false));
            cz.getAI().addBehavior(new AIFloat(3));
            cz.getAI().addBehavior(new AILookAtEntity(4, (float) 8));
            pl.laneCreeps.add(cz);
        }
        for (int i = 0; i < 2; i++) {
            Skeleton s = pl.getServer().getWorld(pl.worldName).spawn(spawnloc, Skeleton.class);
            s.getEquipment().setItemInHand(new ItemStack(Material.BOW));
            s.setRemoveWhenFarAway(false);
            ControllableMob<Skeleton> cs = ControllableMobs.putUnderControl(s, true);
            cs.getAttributes().setMaximumNavigationDistance(1000);
            cs.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 0.2, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "max health", 2, ModifyOperation.ADD_TO_BASIS_VALUE));
            cs.getAI().addBehavior(new AIAttackRanged(1, 1.2, 20));
            cs.getAI().addBehavior(new AITargetNearest(2, 22, false));
            cs.getAI().addBehavior(new AIFloat(3));
            cs.getAI().addBehavior(new AILookAtEntity(4, (float) 24));
            pl.laneCreeps.add(cs);
        }
    }
}
