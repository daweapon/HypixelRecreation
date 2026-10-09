package net.swofty.type.skyblockgeneric.crimson.kuudra;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraChestStorage;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraRewards;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraRun;
import net.swofty.type.skyblockgeneric.crimson.quest.FactionQuestService;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.event.custom.KuudraClearedEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Per-profile Kuudra clears, the boss collection they add up to, and the tier requirements Crag checks.
 * The faction questline owner registers its completion check with {@link #registerQuestlineCheck};
 * until then, having joined a faction stands in for it.
 */
public final class KuudraProgress {
    public static final String KUUDRA_CONUNDRUM = "skyblock.kuudra_conundrum";
    private static final String CLEARED = "kuudra:cleared:";
    private static final String COLLECTION_REWARDED = "kuudra:collection_rewarded";
    private static volatile Predicate<SkyBlockPlayer> questlineCheck =
            player -> CrimsonReputationService.faction(player) != null;

    private KuudraProgress() {
    }

    public static void registerQuestlineCheck(Predicate<SkyBlockPlayer> check) {
        questlineCheck = check;
    }

    /** Whether the faction questline is done up to its first-Kuudra step, which is what Crag asks for. */
    public static boolean isQuestlineReady(SkyBlockPlayer player) {
        return questlineCheck.test(player);
    }

    public static boolean hasCleared(SkyBlockPlayer player, KuudraTier tier) {
        return DatapointCrimsonIsle.of(player).getNumber(CLEARED + tier.name()) > 0;
    }

    public static boolean hasDefeatedKuudra(SkyBlockPlayer player) {
        return hasCleared(player, KuudraTier.BASIC);
    }

    public static Set<KuudraTier> cleared(SkyBlockPlayer player) {
        Set<KuudraTier> cleared = EnumSet.noneOf(KuudraTier.class);
        for (KuudraTier tier : KuudraTier.values()) if (hasCleared(player, tier)) cleared.add(tier);
        return cleared;
    }

    public static long clears(SkyBlockPlayer player, KuudraTier tier) {
        return DatapointCrimsonIsle.of(player).getNumber(CLEARED + tier.name());
    }

    public static long collectionPoints(SkyBlockPlayer player) {
        Map<KuudraTier, Long> clears = new EnumMap<>(KuudraTier.class);
        for (KuudraTier tier : KuudraTier.values()) clears.put(tier, clears(player, tier));
        return collectionPoints(clears);
    }

    public static long collectionPoints(Map<KuudraTier, Long> clears) {
        long points = 0;
        for (Map.Entry<KuudraTier, Long> entry : clears.entrySet()) {
            points += entry.getValue() * KuudraCollectionTier.pointsFor(entry.getKey());
        }
        return points;
    }

    /**
     * Everything a won run pays, from any SkyBlock server: the clear, first-clear and collection SkyBlock XP,
     * collection tier rewards, Faction Quest progress and the daily reputation, the questline's first-Kuudra
     * step, Kuudra Conundrum, the Tentacle Dye roll, and the run's chests stored at Vesuvius.
     */
    public static KuudraRun completeRun(SkyBlockPlayer player, KuudraTier tier) {
        recordClear(player, tier);
        syncSkyBlockXP(player);
        grantCollectionRewards(player);
        FactionQuestService.onKuudraCleared(player, tier);
        CrimsonQuestlines.onFirstKuudraDefeated(player);
        player.getAchievementHandler().completeAchievement(KUUDRA_CONUNDRUM);
        rollTentacleDye(player, tier);

        KuudraRun run = KuudraRewards.roll(player, tier);
        KuudraChestStorage.store(player, run);
        return run;
    }

    /** Records a win and tells listeners. The fight calls {@link #completeRun}, which does this first. */
    public static void recordClear(SkyBlockPlayer player, KuudraTier tier) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().merge(CLEARED + tier.name(), 1L, Long::sum));
        HypixelEventHandler.callCustomEvent(new KuudraClearedEvent(player, tier));
    }

    /** Defeat Kuudra (first clear per tier) and the Kuudra boss collection, never lowering a maxed total. */
    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        int firstClears = 0;
        for (KuudraTier tier : cleared(player)) firstClears += tier.getFirstClearSkyBlockXP();
        raise(player, SkyBlockXPSource.DEFEAT_KUUDRA, firstClears);
        raise(player, SkyBlockXPSource.BOSS_COLLECTIONS, KuudraCollectionTier.skyBlockXP(collectionPoints(player)));
    }

    private static void raise(SkyBlockPlayer player, SkyBlockXPSource source, double amount) {
        var experience = player.getSkyBlockExperience();
        if (amount > experience.getSourceXP(source)) experience.setSourceXP(source, amount);
    }

    /** Pays each collection tier reached since the last call, once. */
    public static void grantCollectionRewards(SkyBlockPlayer player) {
        int rewarded = (int) DatapointCrimsonIsle.of(player).getNumber(COLLECTION_REWARDED);
        int reached = KuudraCollectionTier.reachedCount(collectionPoints(player));
        if (reached <= rewarded) return;
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(COLLECTION_REWARDED, (long) reached));
        for (int i = rewarded; i < reached; i++) grant(player, KuudraCollectionTier.values()[i]);
    }

    private static void grant(SkyBlockPlayer player, KuudraCollectionTier tier) {
        player.sendMessage("<6><l>  COLLECTION LEVEL UP <c>Kuudra <8>" + (tier.ordinal() == 0 ? "0" : KuudraCollectionTier.values()[tier.ordinal() - 1].name())
                + "➜<e>" + tier.name());
        player.sendMessage("<a><l>  REWARDS");
        tier.getRewards().forEach(reward -> player.sendMessage("    " + reward));
        player.sendMessage("    <8>+<b>" + tier.getSkyBlockXP() + " SkyBlock XP");

        ItemType type = ItemType.get(tier.getItemId());
        if (type != null) {
            SkyBlockItem item = new SkyBlockItem(type);
            if (type == ItemType.KUUDRA_PET) item.getAttributeHandler().setRarity(Rarity.COMMON);
            player.addAndUpdateItem(item);
        }
        Essences.add(player, EssenceType.CRIMSON, tier.getEssence());
        player.addAndUpdateItem(ItemType.KUUDRA_TEETH, tier.getTeeth());
    }

    private static void rollTentacleDye(SkyBlockPlayer player, KuudraTier tier) {
        double chance = KuudraConfig.get().tier(tier).tentacleDyeChancePercent();
        if (!LootRoll.roll(player.getUuid(), ItemType.TENTACLE_DYE, chance)) return;
        player.addAndUpdateItem(ItemType.TENTACLE_DYE);
        player.sendMessage("<d><l>WOW! <7>You found a <6>Tentacle Dye<7>!");
    }

    /** /maxprofile: a clear of every tier and enough Infernal clears to max the collection; keys stay unbought. */
    public static void maxOut(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> {
            for (KuudraTier tier : KuudraTier.values()) data.getNumbers().merge(CLEARED + tier.name(), 1L, Math::max);
            long needed = KuudraCollectionTier.V.getRequiredProgress() / KuudraCollectionTier.pointsFor(KuudraTier.INFERNAL);
            data.getNumbers().merge(CLEARED + KuudraTier.INFERNAL.name(), needed, Math::max);
        });
        syncSkyBlockXP(player);
        grantCollectionRewards(player);
        player.getAchievementHandler().completeAchievement(KUUDRA_CONUNDRUM);
    }

    public static List<Requirement> requirements(SkyBlockPlayer player, KuudraTier tier) {
        List<Requirement> requirements = new ArrayList<>();
        int reputation = CrimsonReputationService.highest(player);
        switch (tier) {
            case BASIC -> requirements.add(new Requirement("Have Elle take you to/Kuudra!", hasDefeatedKuudra(player)));
            case HOT -> requirements.add(new Requirement("Complete the main quest!", isQuestlineReady(player)));
            default -> {
            }
        }
        if (tier != KuudraTier.BASIC) {
            requirements.add(new Requirement(tier.getRequiredReputation() + " Reputation with/Mages or Barbarians.",
                    reputation >= tier.getRequiredReputation()));
            requirements.add(new Requirement("Beat the previous tier.", hasCleared(player, tier.previous())));
        }
        return requirements;
    }

    public static boolean meetsRequirements(SkyBlockPlayer player, KuudraTier tier) {
        return requirements(player, tier).stream().allMatch(Requirement::met);
    }

    /** One wiki requirement line; a "/" marks where Hypixel wraps it in the menu. */
    public record Requirement(String text, boolean met) {
        public List<String> lore() {
            String colour = met ? "<a>" : "<c>";
            String[] parts = text.split("/");
            List<String> lines = new ArrayList<>();
            lines.add(colour + (met ? "✔ " : "✗ ") + parts[0]);
            for (int i = 1; i < parts.length; i++) lines.add(colour + parts[i]);
            return lines;
        }
    }
}
