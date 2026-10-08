package net.swofty.type.skyblockgeneric.slayer.inferno;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Inferno Demonlord's Hellion Shield. Anything but a direct melee hit from a Blaze Slayer dagger
 * attuned to the shield's current attunement loses 99% of its damage. Every 8 matching hits the shield
 * cycles to its next attunement.
 */
public final class HellionShield {
    public static final int HITS_PER_SWITCH = 8;
    private static final float BLOCKED_MULTIPLIER = 0.01F;

    private final List<HellionAttunement> cycle;
    private final Runnable onSwitch;
    private int index;
    private int hits;

    public HellionShield(List<HellionAttunement> cycle, Runnable onSwitch) {
        this.cycle = List.copyOf(cycle);
        this.onSwitch = onSwitch;
    }

    public HellionAttunement current() {
        return cycle.get(index);
    }

    public int hits() {
        return hits;
    }

    public float filter(SkyBlockMob target, Damage damage, float amount) {
        if (DaggerVengeance.isApplying()) return amount;
        SkyBlockPlayer player = directDaggerHitter(damage);
        if (player == null) return amount * BLOCKED_MULTIPLIER;
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        BlazeDagger dagger = BlazeDagger.of(held);
        if (dagger == null || dagger.attunement(held) != current()) return amount * BLOCKED_MULTIPLIER;

        DaggerVengeance.recordHit(player, target, dagger, amount);
        if (++hits >= HITS_PER_SWITCH) {
            hits = 0;
            index = (index + 1) % cycle.size();
            DaggerVengeance.shieldSwitched(target);
            onSwitch.run();
        }
        return amount;
    }

    public String header() {
        return current().colour() + "<l>" + current().displayName().toUpperCase() + " <r><8>(" + hits + "/" + HITS_PER_SWITCH + ")";
    }

    private static @Nullable SkyBlockPlayer directDaggerHitter(Damage damage) {
        if (damage.getType() != DamageType.PLAYER_ATTACK || AttackService.isExtraHit()) return null;
        return damage.getAttacker() instanceof SkyBlockPlayer player ? player : null;
    }
}
