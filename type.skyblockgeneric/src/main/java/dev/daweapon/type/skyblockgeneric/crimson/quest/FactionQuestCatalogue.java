package dev.daweapon.type.skyblockgeneric.crimson.quest;

import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestTier.A;
import static dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestTier.B;
import static dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestTier.C;
import static dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestTier.D;
import static dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestTier.S;

/**
 * The full Faction Quest pool. The quest names are SkyHanni-REPO's list (13 trophy fish, 14 fetch items, 5
 * minibosses, the Rescue Mission, 7 Dojo tests and 5 Kuudra tiers). No source gives each quest's tier or
 * amount, so the tier each entry sits at and every amount below are estimates: trophy fish by their rarity,
 * fetch amounts by how hard the item is to farm, Dojo grade equal to the quest tier with harder tests higher,
 * miniboss counts of 1 or 2, Kuudra tiers by their reputation gate, and Rescue tiers per the forum guide
 * (Mage C-A in The Bastion, Barbarian D-A in the Cathedral).
 */
public final class FactionQuestCatalogue {
    private static final Map<String, FactionQuest> QUESTS = new LinkedHashMap<>();

    static {
        fishing(D, "SULPHUR_SKITTER", "Sulphur Skitter");
        fishing(D, "STEAMING_HOT_FLOUNDER", "Steaming-Hot Flounder");
        fishing(D, "GUSHER", "Gusher");
        fishing(D, "BLOBFISH", "Blobfish");
        fishing(C, "SLUGFISH", "Slugfish");
        fishing(C, "FLYFISH", "Flyfish");
        fishing(B, "LAVAHORSE", "Lavahorse");
        fishing(B, "MANA_RAY", "Mana Ray");
        fishing(B, "VOLCANIC_STONEFISH", "Volcanic Stonefish");
        fishing(A, "SKELETON_FISH", "Skeleton Fish");
        fishing(A, "MOLDFIN", "Moldfin");
        fishing(A, "SOUL_FISH", "Soul Fish");
        fishing(S, "GOLDEN_FISH", "Golden Fish");

        fetch(D, "MYCELIUM", "Mycelium", 128);
        fetch(D, "RED_SAND", "Red Sand", 128);
        fetch(D, "GUNPOWDER", "Gunpowder", 64);
        fetch(D, "SULPHUR", "Sulphur", 20);
        fetch(C, "MAGMAG", "Magmag", 20);
        fetch(C, "SPECTRE_DUST", "Spectre Dust", 20);
        fetch(C, "DIGESTED_MUSHROOMS", "Digested Mushrooms", 20);
        fetch(C, "KADA_LEAD", "Kada Lead", 20);
        fetch(C, "WITHER_SOUL", "Wither Soul", 20);
        fetch(C, "BURNING_EYE", "Burning Eye", 20);
        fetch(B, "MUTATED_BLAZE_ASHES", "Mutated Blaze Ashes", 5);
        fetch(B, "GAZING_PEARL", "Gazing Pearl", 5);
        fetch(B, "TENTACLE_MEAT", "Tentacle Meat", 10);
        fetch(A, "TENTACLE_MEAT", "Tentacle Meat", 20);
        fetch(A, "CORRUPTED_FRAGMENT", "Corrupted Fragment", 5);
        fetch(S, "CORRUPTED_FRAGMENT", "Corrupted Fragment", 10);

        for (CrimsonMiniboss miniboss : CrimsonMiniboss.values()) {
            boolean magmaBoss = miniboss == CrimsonMiniboss.MAGMA_BOSS;
            miniboss(magmaBoss ? C : D, miniboss, 1);
            miniboss(magmaBoss ? A : B, miniboss, 2);
        }

        rescue(CrimsonFaction.BARBARIAN, D, C, B, A);
        rescue(CrimsonFaction.MAGE, C, B, A);

        dojo(D, DojoTest.FORCE, DojoTest.STAMINA, DojoTest.MASTERY);
        dojo(C, DojoTest.FORCE, DojoTest.STAMINA, DojoTest.MASTERY, DojoTest.DISCIPLINE);
        dojo(B, DojoTest.MASTERY, DojoTest.DISCIPLINE, DojoTest.SWIFTNESS, DojoTest.CONTROL);
        dojo(A, DojoTest.DISCIPLINE, DojoTest.SWIFTNESS, DojoTest.CONTROL, DojoTest.TENACITY);
        dojo(S, DojoTest.SWIFTNESS, DojoTest.CONTROL, DojoTest.TENACITY);

        kuudra(B, KuudraTier.BASIC);
        kuudra(A, KuudraTier.HOT);
        kuudra(A, KuudraTier.BURNING);
        kuudra(S, KuudraTier.FIERY);
        kuudra(S, KuudraTier.INFERNAL);
    }

    private FactionQuestCatalogue() {
    }

    public static List<FactionQuest> all() {
        return Collections.unmodifiableList(new ArrayList<>(QUESTS.values()));
    }

    public static @Nullable FactionQuest get(String id) {
        return QUESTS.get(id);
    }

    private static void fishing(FactionQuestTier tier, String fishId, String name) {
        add(new FactionQuest("FISHING_" + fishId, FactionQuestType.FISHING, tier, name, fishId, 1, null));
    }

    private static void fetch(FactionQuestTier tier, String itemId, String name, int amount) {
        add(new FactionQuest("FETCH_" + itemId + "_" + tier.name(), FactionQuestType.FETCH, tier, name, itemId, amount, null));
    }

    private static void miniboss(FactionQuestTier tier, CrimsonMiniboss miniboss, int kills) {
        add(new FactionQuest("MINIBOSS_" + miniboss.name() + "_" + kills, FactionQuestType.MINIBOSS, tier,
                miniboss.getDisplayName(), miniboss.name(), kills, null));
    }

    private static void rescue(CrimsonFaction faction, FactionQuestTier... tiers) {
        for (FactionQuestTier tier : tiers) {
            add(new FactionQuest("RESCUE_" + faction.name() + "_" + tier.name(), FactionQuestType.RESCUE, tier,
                    "Rescue Mission", faction.name(), 1, faction));
        }
    }

    private static void dojo(FactionQuestTier tier, DojoTest... tests) {
        for (DojoTest test : tests) {
            add(new FactionQuest("DOJO_" + test.name() + "_" + tier.name(), FactionQuestType.DOJO, tier,
                    test.getDisplayName() + " Rank " + tier.name(), test.name(), 1, null));
        }
    }

    private static void kuudra(FactionQuestTier tier, KuudraTier kuudra) {
        add(new FactionQuest("KUUDRA_" + kuudra.name(), FactionQuestType.KUUDRA, tier,
                kuudra.getDisplayName() + " Kuudra", kuudra.name(), 1, null));
    }

    private static void add(FactionQuest quest) {
        if (QUESTS.put(quest.id(), quest) != null) throw new IllegalStateException("Duplicate faction quest " + quest.id());
    }
}
