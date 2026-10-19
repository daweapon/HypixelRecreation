package dev.daweapon.type.theend.altar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DraconicAltarRules {
    public static final double BONUS_CHANCE_PERCENT = 45;

    public record Bonus(String itemId, int min, int max, double weight) {
    }

    public record Sacrifice(String itemId, int essence, List<Bonus> bonuses) {
        public List<Bonus> withNothing() {
            List<Bonus> entries = new ArrayList<>();
            double total = bonuses.stream().mapToDouble(Bonus::weight).sum();
            for (Bonus bonus : bonuses) entries.add(new Bonus(bonus.itemId(), bonus.min(), bonus.max(), bonus.weight() / total * BONUS_CHANCE_PERCENT));
            entries.add(new Bonus(null, 0, 0, 100 - BONUS_CHANCE_PERCENT));
            return entries;
        }
    }

    private static final Map<String, Integer> DRAGON_PIECES = Map.of("HELMET", 25, "CHESTPLATE", 40, "LEGGINGS", 35, "BOOTS", 20);
    private static final Map<String, Integer> SUPERIOR_PIECES = Map.of("HELMET", 50, "CHESTPLATE", 80, "LEGGINGS", 70, "BOOTS", 40);
    private static final List<String> VARIANTS = List.of("STRONG", "SUPERIOR", "YOUNG", "OLD", "PROTECTOR", "UNSTABLE", "WISE");
    private static final List<String> ENDER_GEAR = List.of("END_HELMET", "END_CHESTPLATE", "END_LEGGINGS", "END_BOOTS",
            "ENDER_CLOAK", "ENDER_BELT", "ENDER_NECKLACE", "ENDER_GAUNTLET", "ENDER_MONOCLE");

    private DraconicAltarRules() {
    }

    public static Optional<Sacrifice> of(String itemId) {
        for (String variant : VARIANTS) {
            for (String piece : DRAGON_PIECES.keySet()) {
                if (!itemId.equals(variant + "_DRAGON_" + piece)) continue;
                int essence = (variant.equals("SUPERIOR") ? SUPERIOR_PIECES : DRAGON_PIECES).get(piece);
                return Optional.of(new Sacrifice(itemId, essence, List.of(
                        new Bonus(variant + "_FRAGMENT", 15, 20, 81.93),
                        new Bonus("RITUAL_RESIDUE", 1, 1, 10.84),
                        new Bonus("SUMMONING_EYE", 1, 1, 4.82),
                        new Bonus("DRAGON_HORN", 1, 1, 2.41))));
            }
        }
        if (itemId.equals("ASPECT_OF_THE_DRAGON")) {
            return Optional.of(new Sacrifice(itemId, 60, List.of(
                    new Bonus("SUPERIOR_FRAGMENT", 10, 15, 80),
                    new Bonus("RITUAL_RESIDUE", 1, 1, 10.59),
                    new Bonus("SUMMONING_EYE", 1, 1, 4.71),
                    new Bonus("DRAGON_CLAW", 1, 1, 2.35),
                    new Bonus("DRAGON_HORN", 1, 1, 2.35))));
        }
        if (ENDER_GEAR.contains(itemId)) {
            return Optional.of(new Sacrifice(itemId, 3, List.of(
                    new Bonus("ENCHANTED_ENDER_PEARL", 2, 6, 95),
                    new Bonus("ENCHANTED_EYE_OF_ENDER", 1, 1, 4),
                    new Bonus("SUMMONING_EYE", 1, 1, 1))));
        }
        if (itemId.equals("END_STONE_BOW")) {
            return Optional.of(new Sacrifice(itemId, 3, List.of(
                    new Bonus("ENCHANTED_END_STONE", 4, 6, 99),
                    new Bonus("SUMMONING_EYE", 1, 1, 1))));
        }
        if (itemId.equals("OBSIDIAN_CHESTPLATE")) {
            return Optional.of(new Sacrifice(itemId, 3, List.of(
                    new Bonus("ENCHANTED_OBSIDIAN", 3, 4, 98.95),
                    new Bonus("SUMMONING_EYE", 1, 1, 1.05))));
        }
        return Optional.empty();
    }
}
