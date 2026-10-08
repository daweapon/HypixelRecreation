package net.swofty.type.skyblockgeneric.accessory;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.accessory.effect.AccessoryEffect;
import net.swofty.type.skyblockgeneric.accessory.enrichment.Enrichments;
import net.swofty.type.skyblockgeneric.accessory.effect.AccessoryEffectKind;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * Stats and typed effects of the accessories {@link AccessoryOwnership} counts: each counted accessory
 * once, its {@code default_statistics} plus any {@code STAT} effect whose conditions hold.
 */
public final class AccessoryStats {
    private AccessoryStats() {
    }

    public static ItemStatistics of(SkyBlockPlayer player, List<OwnedAccessory> active,
                                    Function<SkyBlockItem, ItemStatistics> itemExtras) {
        ItemStatistics total = ItemStatistics.empty();
        for (OwnedAccessory owned : active) total = ItemStatistics.add(total, of(player, owned, itemExtras));
        return total;
    }

    public static ItemStatistics of(SkyBlockPlayer player, OwnedAccessory owned,
                                    Function<SkyBlockItem, ItemStatistics> itemExtras) {
        if (!owned.definition().getRealm().isActive()) return ItemStatistics.empty();
        ItemStatistics total = ItemStatistics.add(owned.item().getAttributeHandler().getStatistics(),
                itemExtras.apply(owned.item()));
        total = ItemStatistics.add(total, Enrichments.statistics(owned.item()));
        return ItemStatistics.add(total, effectStatistics(player, owned));
    }

    /** Stats from the accessory's {@code STAT} effects and stat-granting {@code HANDLER} effects that apply now. */
    public static ItemStatistics effectStatistics(SkyBlockPlayer player, OwnedAccessory owned) {
        ItemStatistics total = ItemStatistics.empty();
        if (!owned.definition().getRealm().isActive()) return total;
        for (AccessoryEffect effect : owned.definition().getEffects()) {
            if (effect.kind() == AccessoryEffectKind.STAT && effect.appliesTo(player))
                total = ItemStatistics.add(total, effect.statistics());
            else if (effect.kind() == AccessoryEffectKind.HANDLER)
                total = ItemStatistics.add(total, AccessoryHandlerStats.of(player, effect));
        }
        return total;
    }

    /** Multiplier on damage a mob deals to the player; each matching reduction stacks multiplicatively. */
    public static double damageTakenMultiplier(SkyBlockPlayer player, @Nullable SkyBlockMob mob) {
        double multiplier = 1;
        for (AccessoryEffect effect : effects(player, AccessoryEffectKind.DAMAGE_REDUCTION)) {
            if (effect.matches(mob)) multiplier *= 1 - effect.percent() / 100D;
        }
        return multiplier;
    }

    /** Multiplier on damage the player deals to a mob; matching bonuses add together. */
    public static double damageDealtMultiplier(SkyBlockPlayer player, @Nullable SkyBlockMob mob) {
        double percent = 0;
        for (AccessoryEffect effect : effects(player, AccessoryEffectKind.DAMAGE_BONUS)) {
            if (effect.matches(mob)) percent += effect.percent();
        }
        return 1 + percent / 100D;
    }

    private static List<AccessoryEffect> effects(SkyBlockPlayer player, AccessoryEffectKind kind) {
        return AccessoryOwnership.active(player).stream()
                .filter(owned -> owned.definition().getRealm().isActive())
                .flatMap(owned -> owned.definition().getEffects().stream())
                .filter(effect -> effect.kind() == kind && effect.appliesTo(player))
                .toList();
    }
}
