package dev.daweapon.type.skyblockgeneric.crimson.quest;

import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.ReputationTier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Rolls a day's five quests. The tiers come from the reputation table; the slots then get different
 * categories where the available pool allows it (a randomised search), repeating a category only when it
 * can't. One category per slot is an estimate based on observed boards.
 */
public final class FactionQuestGenerator {
    private FactionQuestGenerator() {
    }

    public static List<FactionQuest> roll(CrimsonFaction faction, ReputationTier title, Predicate<FactionQuest> available,
                                          Random random) {
        List<FactionQuestTier> tiers = FactionQuestTier.mixFor(title);
        List<List<FactionQuest>> candidates = new ArrayList<>();
        for (FactionQuestTier tier : tiers) {
            candidates.add(FactionQuestCatalogue.all().stream()
                    .filter(quest -> quest.tier() == tier)
                    .filter(quest -> quest.faction() == null || quest.faction() == faction)
                    .filter(available)
                    .toList());
        }

        List<FactionQuestType[]> orders = new ArrayList<>();
        for (int i = 0; i < tiers.size(); i++) {
            List<FactionQuestType> types = new ArrayList<>(candidates.get(i).stream().map(FactionQuest::type).distinct().toList());
            Collections.shuffle(types, random);
            orders.add(types.toArray(FactionQuestType[]::new));
        }
        FactionQuestType[] assignment = new FactionQuestType[tiers.size()];
        boolean distinct = assign(orders, 0, assignment);

        List<FactionQuest> picked = new ArrayList<>();
        for (int i = 0; i < tiers.size(); i++) {
            List<FactionQuest> pool = candidates.get(i).stream().filter(quest -> !picked.contains(quest)).toList();
            if (pool.isEmpty()) continue;
            FactionQuestType type = distinct ? assignment[i] : null;
            List<FactionQuest> ofType = type == null ? pool : pool.stream().filter(quest -> quest.type() == type).toList();
            if (ofType.isEmpty()) ofType = pool;
            picked.add(ofType.get(random.nextInt(ofType.size())));
        }
        picked.sort(Comparator.comparing((FactionQuest quest) -> quest.tier().ordinal()).thenComparing(quest -> quest.type().ordinal()));
        return picked;
    }

    private static boolean assign(List<FactionQuestType[]> orders, int slot, FactionQuestType[] assignment) {
        if (slot == orders.size()) return true;
        for (FactionQuestType type : orders.get(slot)) {
            if (contains(assignment, slot, type)) continue;
            assignment[slot] = type;
            if (assign(orders, slot + 1, assignment)) return true;
        }
        assignment[slot] = null;
        return false;
    }

    private static boolean contains(FactionQuestType[] assignment, int upTo, FactionQuestType type) {
        for (int i = 0; i < upTo; i++) if (assignment[i] == type) return true;
        return false;
    }
}
