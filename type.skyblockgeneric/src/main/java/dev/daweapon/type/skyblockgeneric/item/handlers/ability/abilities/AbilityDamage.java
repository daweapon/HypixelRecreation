package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.DamageIndicator;

/** Magic damage from item abilities, using the wiki's Damage Calculation § Ability Damage formula. */
public final class AbilityDamage {
    private AbilityDamage() {
    }

    public static double of(SkyBlockPlayer player, double baseDamage, double intelligenceScaling) {
        ItemStatistics statistics = player.getStatistics().allStatistics();
        double intelligence = Math.max(0, statistics.getOverall(ItemStatistic.INTELLIGENCE));
        double bonusAbilityDamage = statistics.getOverall(ItemStatistic.ABILITY_DAMAGE);
        return ElectionPerks.spellDamage(baseDamage)
                * (1 + intelligence / 100 * intelligenceScaling)
                * (1 + bonusAbilityDamage / 100);
    }

    public static void hitArea(SkyBlockPlayer player, Point center, double radius, String abilityName,
                               double baseDamage, double intelligenceScaling) {
        float damage = (float) of(player, baseDamage, intelligenceScaling);
        int hit = 0;
        for (Entity entity : player.getInstance().getNearbyEntities(center, radius)) {
            if (!(entity instanceof SkyBlockMob mob) || mob.isDead()) continue;
            mob.damage(new Damage(DamageType.MAGIC, player, player, center, damage));
            new DamageIndicator().damage(damage).pos(mob.getPosition()).critical(false).display(player.getInstance());
            hit++;
        }
        if (hit > 0) {
            player.sendMessage("<7>Your " + abilityName + " hit <c>" + hit + " <7>" + (hit == 1 ? "enemy" : "enemies")
                    + " for <c>" + StringUtility.commaify(damage * hit) + " <7>damage.");
        }
    }
}
