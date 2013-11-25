package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackMelee;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AILookAtEntity;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AITargetNearest;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.AttributeModifierFactory;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.ModifyOperation;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Zombie;
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
            for (ControllableMob cm : pl.jungleCreeps) {
                if (cm.getEntity().getLocation().distance(loc) > 10) {
                    for (int i = 0; i < 4; i++) {
                        Zombie z = pl.getServer().getWorld(pl.worldName).spawn(loc, Zombie.class);
                        ControllableMob<Zombie> cz = ControllableMobs.putUnderControl(z, true);
                        cz.getAttributes().setMaximumNavigationDistance(8);
                        cz.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 1.0, ModifyOperation.ADD_TO_BASIS_VALUE));
                        cz.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "health max", 8.0, ModifyOperation.ADD_TO_BASIS_VALUE));
                        cz.getAI().addBehavior(new AIAttackMelee(1, 1.3));
                        cz.getAI().addBehavior(new AITargetNearest(2, 4, false));
                        cz.getAI().addBehavior(new AILookAtEntity(3, (float) 6));
                        pl.jungleCreeps.add(cz);
                        pl.controlMobs.put(cz, 0);
                    }
                }
            }
        }
    }
}
