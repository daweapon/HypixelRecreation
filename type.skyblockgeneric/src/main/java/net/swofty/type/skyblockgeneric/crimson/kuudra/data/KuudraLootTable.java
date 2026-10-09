package net.swofty.type.skyblockgeneric.crimson.kuudra.data;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraReward;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record KuudraLootTable(Map<KuudraTier, List<Long>> freeEssence, Map<KuudraTier, Guarantee> guaranteed,
                              List<Entry> primary, List<Entry> secondary) {

    public static final String TEETH_PLAQUE = "TEETH_PLAQUE";

    public record Guarantee(long essence, int teethMin, int teethMax) {
        static Guarantee parse(Object yaml) {
            Map<String, Object> map = Yamls.map(yaml);
            List<Integer> teeth = Yamls.integers(map.get("teeth"));
            return new Guarantee(Yamls.longValue(map, "essence", 0), teeth.getFirst(), teeth.getLast());
        }
    }

    /**
     * One weighted row of the Paid chest's primary or secondary roll. Armour rows name the base piece and
     * resolve to the run tier's variant; book rows may take their level from the tier (Basic I … Infernal V).
     */
    public record Entry(@Nullable String itemId, int amount, long essence, @Nullable String enchantment,
                        boolean levelFromTier, int level, boolean tieredArmor, List<Double> weights,
                        @Nullable String requires) {

        static Entry parse(Map<String, Object> yaml) {
            Object level = yaml.get("level");
            boolean fromTier = "TIER".equals(level);
            return new Entry(Yamls.string(yaml, "item"), Yamls.integer(yaml, "amount", 1),
                    Yamls.longValue(yaml, "essence", 0), Yamls.string(yaml, "enchantment"), fromTier,
                    fromTier || level == null ? 0 : ((Number) level).intValue(), false,
                    weights(yaml.get("weight")), Yamls.string(yaml, "requires"));
        }

        static Entry armor(String baseId, double weight) {
            return new Entry(baseId, 1, 0, null, false, 0, true,
                    Collections.nCopies(KuudraTier.values().length, weight), null);
        }

        private static List<Double> weights(Object yaml) {
            if (yaml instanceof Number single) return Collections.nCopies(KuudraTier.values().length, single.doubleValue());
            List<Double> weights = Yamls.list(yaml).stream().map(each -> ((Number) each).doubleValue()).toList();
            if (weights.size() != KuudraTier.values().length) {
                throw new IllegalArgumentException("Kuudra loot weights need one value per tier: " + weights);
            }
            return weights;
        }

        public double weight(KuudraTier tier) {
            return weights.get(tier.ordinal());
        }

        public boolean isEssence() {
            return essence > 0;
        }

        public @Nullable String itemId(KuudraTier tier) {
            if (itemId == null || !tieredArmor || tier == KuudraTier.BASIC) return itemId;
            return tier.name() + "_" + itemId;
        }

        public @Nullable ItemType itemType(KuudraTier tier) {
            String id = itemId(tier);
            return id == null ? null : ItemType.get(id);
        }

        public int level(KuudraTier tier) {
            return levelFromTier ? tier.ordinal() + 1 : level;
        }

        /** Whether this server can hand the row out at all: an essence amount, or an item (and enchantment) it has. */
        public boolean exists(KuudraTier tier) {
            if (isEssence()) return true;
            if (itemType(tier) == null) return false;
            if (enchantment == null) return true;
            try {
                EnchantmentType.valueOf(enchantment);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        public KuudraReward reward(KuudraTier tier) {
            if (isEssence()) return KuudraReward.essence(essence);
            if (enchantment != null) return KuudraReward.book(enchantment, level(tier));
            return KuudraReward.item(itemId(tier), amount);
        }
    }

    static KuudraLootTable parse(Map<String, Object> yaml) {
        Map<String, Object> free = Yamls.map(yaml.get("free_chest"));
        Map<String, Object> paid = Yamls.map(yaml.get("paid_chest"));

        List<Entry> primary = new ArrayList<>();
        double armorWeight = Yamls.decimal(paid, "armor_weight", 0);
        for (String set : Yamls.strings(paid.get("armor_sets"))) {
            for (String piece : Yamls.strings(paid.get("armor_pieces"))) primary.add(Entry.armor(set + "_" + piece, armorWeight));
        }
        Yamls.maps(paid.get("primary")).forEach(entry -> primary.add(Entry.parse(entry)));

        return new KuudraLootTable(
                Yamls.byTier(free.get("essence"), value -> Yamls.list(value).stream().map(each -> ((Number) each).longValue()).toList()),
                Yamls.byTier(paid.get("guaranteed"), Guarantee::parse),
                List.copyOf(primary),
                Yamls.maps(paid.get("secondary")).stream().map(Entry::parse).toList());
    }

    /** The rows a tier's roll may land on: weighted for that tier, existing on this server, and unlocked. */
    public static List<Entry> candidates(List<Entry> entries, KuudraTier tier, boolean teethPlaque) {
        return entries.stream()
                .filter(entry -> entry.weight(tier) > 0)
                .filter(entry -> entry.exists(tier))
                .filter(entry -> !TEETH_PLAQUE.equals(entry.requires()) || teethPlaque)
                .toList();
    }
}
