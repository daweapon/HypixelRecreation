package dev.daweapon.type.skyblockgeneric.slayer;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Optional;

/**
 * Hypixel's slayer miniboss rule: after a valid kill, a 20% roll spawns one, but only once 19% of the
 * quest's Combat XP has been earned since the last one and at least a second has passed. Both are kept
 * on the quest, so switching lobbies doesn't reset them.
 */
public final class SlayerMinibossSpawner {
    private static final double SPAWN_CHANCE = 20;
    private static final double STRONGER_CHANCE = 20;
    private static final double PROGRESS_SINCE_LAST = 0.19;
    private static final long MINIMUM_GAP_MILLIS = 1_000L;

    private SlayerMinibossSpawner() {
    }

    public static SlayerQuest maybeSpawn(SkyBlockPlayer player, SlayerDefinition definition, SlayerQuest quest,
                                         SlayerTierDefinition tier, Pos at) {
        List<SlayerMiniboss> candidates = definition.minibosses().stream()
            .filter(miniboss -> miniboss.tiers().contains(quest.tier()))
            .toList();
        if (candidates.isEmpty()) return quest;

        long now = System.currentTimeMillis();
        if (quest.combatXp() - quest.lastMinibossXp() < tier.requiredCombatXp() * PROGRESS_SINCE_LAST) return quest;
        if (now - quest.lastMinibossAt() < MINIMUM_GAP_MILLIS) return quest;
        if (!LootRoll.roll(player.getUuid(), null, SPAWN_CHANCE)) return quest;

        Optional<SlayerMiniboss> stronger = candidates.stream().filter(SlayerMiniboss::stronger).findFirst();
        SlayerMiniboss chosen = stronger.isPresent() && LootRoll.roll(player.getUuid(), null, STRONGER_CHANCE)
            ? stronger.get()
            : candidates.stream().filter(miniboss -> !miniboss.stronger()).findFirst().orElse(candidates.getFirst());

        Instance instance = player.getInstance();
        if (instance == null) return quest;
        SlayerMinibossMob.create(chosen, player.getUuid()).setInstance(instance, at);
        player.sendMessage(Text.of("<c><l>SLAYER MINI-BOSS</l> <5>{} <7>has spawned!", chosen.name()));
        return quest.markMinibossSpawned(now);
    }
}
