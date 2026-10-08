package net.swofty.type.skyblockgeneric.mythological.ability;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.MobAttackService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Only every sixth hit gets through its shield (seventh below 66% Health, eighth below 33%), and
 * arrows don't hurt it. It throws players into the air and raises crosses of Iron Blocks that call
 * lightning on anyone standing on them, more of them per stage.
 */
final class GaiaConstructAbility implements MythologicalAbility {
    private static final Map<Rarity, Double> LIGHTNING_DAMAGE = Map.of(
            Rarity.RARE, 5_000D,
            Rarity.EPIC, 7_500D,
            Rarity.LEGENDARY, 10_000D,
            Rarity.MYTHIC, 15_000D);
    private static final int THROW_COOLDOWN_TICKS = 100;
    private static final int CROSS_LIFETIME_TICKS = 60;
    private static final int[] CROSS_INTERVAL_TICKS = {160, 120, 80};
    private static final double CROSS_SCATTER = 4;
    private static final double CROSS_RANGE = 16;

    private record Cross(List<BlockVec> blocks, TemporaryBlocks placed, int expiresAt, Set<UUID> struck) {
    }

    private final List<Cross> crosses = new ArrayList<>();
    private int hitsTaken;
    private int nextThrow;

    private int stage(MythologicalCreatureMob mob) {
        double ratio = mob.healthRatio();
        return ratio > 0.66 ? 0 : ratio > 0.33 ? 1 : 2;
    }

    @Override
    public float modifyIncomingDamage(MythologicalCreatureMob mob, Damage damage, float amount) {
        if (!(damage.getAttacker() instanceof SkyBlockPlayer)) return amount;
        int hitsNeeded = 6 + stage(mob);
        if (++hitsTaken % hitsNeeded == 0) return amount;
        MythologicalCombat.sound(mob, "entity.iron_golem.hurt", 0.8f, 1.6f);
        return 0;
    }

    @Override
    public boolean immuneToArrows() {
        return true;
    }

    @Override
    public void onMeleeHit(MythologicalCreatureMob mob, SkyBlockPlayer target) {
        int now = mob.getFightTicks();
        if (now < nextThrow) return;
        nextThrow = now + THROW_COOLDOWN_TICKS;
        MobAttackService.strike(mob, target);
        MythologicalCombat.launch(target, MythologicalCombat.away(mob, target, 8, 28));
        MythologicalCombat.sound(mob, "entity.iron_golem.attack", 1f, 0.8f);
    }

    @Override
    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        crosses.removeIf(cross -> {
            if (ticksAlive < cross.expiresAt()) return false;
            cross.placed().restoreAll();
            return true;
        });
        for (Cross cross : crosses) {
            for (SkyBlockPlayer player : MythologicalCombat.nearby(mob, CROSS_RANGE + CROSS_SCATTER)) {
                if (cross.struck().contains(player.getUuid())) continue;
                if (!IronCross.isStandingOn(player.getPosition(), cross.blocks())) continue;
                cross.struck().add(player.getUuid());
                MythologicalCombat.lightning(mob.getInstance(), player.getPosition());
                MobAttackService.strike(mob, player, LIGHTNING_DAMAGE.getOrDefault(mob.getVariant().griffinRarity(), 5_000D));
            }
        }
        int stage = stage(mob);
        if (ticksAlive == 0 || ticksAlive % CROSS_INTERVAL_TICKS[stage] != 0) return;
        MythologicalCombat.target(mob)
                .filter(target -> target.getPosition().distance(mob.getPosition()) <= CROSS_RANGE)
                .ifPresent(target -> {
                    for (int i = 0; i <= stage; i++) {
                        Pos at = i == 0 ? target.getPosition() : target.getPosition().add(
                                ThreadLocalRandom.current().nextDouble(-CROSS_SCATTER, CROSS_SCATTER), 0,
                                ThreadLocalRandom.current().nextDouble(-CROSS_SCATTER, CROSS_SCATTER));
                        TemporaryBlocks placed = new TemporaryBlocks();
                        List<BlockVec> blocks = IronCross.raise(mob.getInstance(), at, placed);
                        if (!blocks.isEmpty()) {
                            crosses.add(new Cross(blocks, placed, ticksAlive + CROSS_LIFETIME_TICKS, new HashSet<>()));
                        }
                    }
                    MythologicalCombat.sound(mob, "block.anvil.land", 0.8f, 0.6f);
                });
    }

    @Override
    public void cleanup(MythologicalCreatureMob mob) {
        crosses.forEach(cross -> cross.placed().restoreAll());
        crosses.clear();
    }
}
