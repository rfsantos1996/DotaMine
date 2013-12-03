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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
            loc.getChunk().load();
            boolean playernear = false;
            boolean spawn = true;
            for (Player p : pl.ingameList.keySet()) {
                if (p.getLocation().distance(loc) < 32) {
                    playernear = true;
                }
            }
            if (pl.jungleCreeps.size() > 0 && playernear) {
                for (Entity en : pl.jungleCreeps.keySet()) {
                    if (en.getLocation().distance(loc) < 12 && !en.isDead()) {
                        spawn = false;
                    }
                }
            }
            if (spawn && playernear) {
                spawnJungle(loc);
            }
        }
        pl.debug("creepspawn jungle");
    }

    private void spawnJungle(Location loc) {
        for (int i = 0; i < 2; i++) {
            Zombie z = pl.getServer().getWorld(pl.worldName).spawn(loc, Zombie.class);
            z.setRemoveWhenFarAway(true);
            z.setCanPickupItems(false);
            ControllableMob<Zombie> cz = ControllableMobs.putUnderControl(z, true);
            cz.getAttributes().setMaximumNavigationDistance(8);
            cz.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 0.65, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getAttackDamageAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "attack dmg", 5.0, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "health max", 10.0, ModifyOperation.ADD_TO_BASIS_VALUE));
            cz.getEntity().setHealth(cz.getEntity().getMaxHealth());
            cz.getAI().addBehavior(new AIAttackMelee(1, 1.2));
            cz.getAI().addBehavior(new AITargetNearest(2, 5, true));
            cz.getAI().addBehavior(new AILookAtEntity(3, (float) 12));
            pl.jungleCreeps.put(z, cz);
            pl.controlMobs.put(z, cz);
        }
    }
}
