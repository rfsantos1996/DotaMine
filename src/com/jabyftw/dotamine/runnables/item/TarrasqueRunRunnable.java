package com.jabyftw.dotamine.runnables.item;

import com.jabyftw.dotamine.DotaMine;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class TarrasqueRunRunnable extends BukkitRunnable {

    private final DotaMine pl;

    public TarrasqueRunRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        if (pl.hasTarrasque.size() > 0) {
            for (Player p : pl.hasTarrasque) {
                if (!pl.tarrasqueRecentlyDamaged.contains(p) && pl.checkForTarrasque(p)) {
                    p.removePotionEffect(PotionEffectType.REGENERATION);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 4, 1), true);
                }
            }
        }
    }
}
