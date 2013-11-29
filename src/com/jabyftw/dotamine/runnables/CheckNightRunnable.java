package com.jabyftw.dotamine.runnables;

import com.jabyftw.dotamine.DotaMine;
import com.jabyftw.dotamine.Jogador;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rafael
 */
public class CheckNightRunnable extends BukkitRunnable {

    private final DotaMine pl;
    private boolean told = false;

    public CheckNightRunnable(DotaMine pl) {
        this.pl = pl;
    }

    @Override
    public void run() {
        if (pl.getServer().getWorld(pl.worldName).getTime() > 13000) {
            for (Jogador j : pl.ingameList.values()) {
                if (j.getAttackType() == 2) { // Ranged
                    j.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10000, 0, false), true);
                    if (!told) {
                        j.getPlayer().sendMessage(pl.getLang("lang.lowRangedNightVision"));
                    }
                }
            }
            told = true;
        } else {
            for (Jogador j : pl.ingameList.values()) {
                if (j.getAttackType() == 2) { // Ranged
                    j.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
                }
            }
        }
    }

}
