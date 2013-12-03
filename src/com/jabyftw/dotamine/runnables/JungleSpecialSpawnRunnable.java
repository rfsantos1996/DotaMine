package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackMelee;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AILookAtEntity;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AITargetHurtBy;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AITargetNearest;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.AttributeModifierFactory;
import de.ntcomputer.minecraft.controllablemobs.api.attributes.ModifyOperation;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class JungleSpecialSpawnRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public JungleSpecialSpawnRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        for (Location loc : pl.jungleRedSpawn) {
            spawn(loc, 1);
        }
        for (Location loc : pl.jungleBlueSpawn) {
            spawn(loc, 2);
        }
    }

    private void spawn(Location loc, int type) {
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
            spawnJungle(loc, type);
        }
        pl.debug("creepspawn jungle");
    }

    private void spawnJungle(Location loc, int type) {
        Zombie z = pl.getServer().getWorld(pl.worldName).spawn(loc, Zombie.class);
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        LeatherArmorMeta lam = (LeatherArmorMeta) chest.getItemMeta();
        if (type == 1) {
            lam.setColor(Color.RED);
        } else {
            lam.setColor(Color.BLUE);
        }
        chest.setItemMeta(lam);
        z.getEquipment().setChestplate(chest);
        z.getEquipment().setChestplateDropChance(0);
        z.setRemoveWhenFarAway(true);
        z.setCanPickupItems(false);
        ControllableMob<Zombie> cz = ControllableMobs.putUnderControl(z, true);
        cz.getAttributes().setMaximumNavigationDistance(16);
        cz.getAttributes().getKnockbackResistanceAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "knockback res", 0.9, ModifyOperation.ADD_TO_BASIS_VALUE));
        cz.getAttributes().getAttackDamageAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "attack dmg", 8.0, ModifyOperation.ADD_TO_BASIS_VALUE));
        cz.getAttributes().getMaxHealthAttribute().attachModifier(AttributeModifierFactory.create(UUID.randomUUID(), "health max", 43.0, ModifyOperation.ADD_TO_BASIS_VALUE));
        cz.getEntity().setHealth(cz.getEntity().getMaxHealth());
        cz.getAI().addBehavior(new AIAttackMelee(1, 1.3));
        cz.getAI().addBehavior(new AITargetNearest(3, 5, true));
        cz.getAI().addBehavior(new AILookAtEntity(4, (float) 12));
        pl.jungleSpecialCreeps.put(z, type);
        pl.controlMobs.put(z, cz);
    }
}
