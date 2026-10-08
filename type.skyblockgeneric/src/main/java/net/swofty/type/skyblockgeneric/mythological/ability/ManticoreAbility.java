package net.swofty.type.skyblockgeneric.mythological.ability;

import net.minestom.server.particle.Particle;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.MobAttackService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fatal Sting: green particles gather at the Manticore's head, then it looses a barrage of arrows.
 * A player who is struck takes heavy damage, cannot heal, and dies unless the Manticore is slain
 * before the sting's timer runs out.
 */
final class ManticoreAbility implements MythologicalAbility {
    private static final String STING = "Fatal Sting";
    private static final String HEALING_BLOCKED = "<c>The <2>Fatal Sting <c>of the <5>Manticore <c>disabled all healing!";
    private static final int STING_INTERVAL_TICKS = 400;
    private static final int FIRST_STING_TICKS = 200;
    private static final int WIND_UP_TICKS = 60;
    private static final int BARRAGE_ARROWS = 8;
    private static final double BARRAGE_SPREAD_DEGREES = 14;
    private static final double ARROW_SPEED = 1.8;
    private static final double STING_RANGE = 24;
    private static final double STING_DAMAGE_MULTIPLIER = 4;
    private static final int STING_TIMER_SECONDS = 30;

    private final Map<UUID, Integer> stungUntil = new HashMap<>();
    private int windUpEndsAt = -1;

    @Override
    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        tickStings(mob, ticksAlive);
        if (windUpEndsAt >= 0) {
            if (ticksAlive % 4 == 0) {
                MythologicalCombat.particles(mob.getInstance(), Particle.HAPPY_VILLAGER,
                        mob.getPosition().add(0, mob.getEyeHeight() + 0.3, 0), 0.3f, 6);
            }
            if (ticksAlive >= windUpEndsAt) {
                windUpEndsAt = -1;
                barrage(mob, ticksAlive);
            }
            return;
        }
        if (ticksAlive >= FIRST_STING_TICKS && (ticksAlive - FIRST_STING_TICKS) % STING_INTERVAL_TICKS == 0
                && MythologicalCombat.target(mob).isPresent()) {
            windUpEndsAt = ticksAlive + WIND_UP_TICKS;
        }
    }

    private void barrage(MythologicalCreatureMob mob, int ticksAlive) {
        MythologicalCombat.target(mob)
                .filter(target -> target.getPosition().distance(mob.getPosition()) <= STING_RANGE)
                .ifPresent(target -> {
                    MythologicalCombat.sound(mob, "entity.skeleton.shoot", 1f, 0.6f);
                    for (int i = 0; i < BARRAGE_ARROWS; i++) {
                        MobProjectile.arrowSpread(mob, target, ARROW_SPEED, BARRAGE_SPREAD_DEGREES,
                                player -> sting(mob, player));
                    }
                });
    }

    private void sting(MythologicalCreatureMob mob, SkyBlockPlayer player) {
        if (stungUntil.containsKey(player.getUuid())) return;
        MobAttackService.strike(mob, player,
                mob.getStatistics().getOverall(ItemStatistic.DAMAGE) * STING_DAMAGE_MULTIPLIER);
        if (player.isDead()) return;
        stungUntil.put(player.getUuid(), mob.getFightTicks() + STING_TIMER_SECONDS * 20);
        player.setHealingModifier(STING, new SkyBlockPlayer.HealingModifier(0, HEALING_BLOCKED));
        player.sendMessage("<2><l>FATAL STING! <c>You were fatally stung by a <5>Manticore<c>! Kill it before the time runs out or <c><l>die!");
    }

    private void tickStings(MythologicalCreatureMob mob, int ticksAlive) {
        if (stungUntil.isEmpty()) return;
        for (Map.Entry<UUID, Integer> entry : Map.copyOf(stungUntil).entrySet()) {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(entry.getKey());
            if (player == null || player.isDead()) {
                stungUntil.remove(entry.getKey());
                continue;
            }
            int remaining = entry.getValue() - ticksAlive;
            if (remaining <= 0) {
                release(entry.getKey());
                MythologicalCombat.slay(mob, player);
            } else if (remaining % 20 == 0) {
                SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.DEFENSE,
                        Text.of("<2><l>FATAL STING <c>{}s", remaining / 20), 25, 5);
            }
        }
    }

    private void release(UUID id) {
        stungUntil.remove(id);
        SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(id);
        if (player != null) player.removeHealingModifier(STING);
    }

    @Override
    public void cleanup(MythologicalCreatureMob mob) {
        Map.copyOf(stungUntil).keySet().forEach(this::release);
    }
}
