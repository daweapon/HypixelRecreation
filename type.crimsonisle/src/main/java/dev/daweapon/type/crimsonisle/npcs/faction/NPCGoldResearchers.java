package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.crimsonisle.faction.GoldResearchNPC;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;

import java.util.List;

// Alchemage above the Scarleton Bank and Baar above the Dragontail Auction House (shared gold research).
public final class NPCGoldResearchers {
    private static final String ARMOUR_LINE = "It seems like the first piece you brought me is only 25% of the magical power, could you get me the leggings?";

    private NPCGoldResearchers() {
    }

    public static class Alchemage extends GoldResearchNPC {
        private static final List<Delivery> CHAIN = chain(
                new Delivery("DIVER_HELMET", 1, "I heard there exists a golden helmet that helps you swim faster in water. I would like to test its magical properties, could you bring me one?"),
                new Delivery("DIVER_LEGGINGS", 1, ARMOUR_LINE));

        public Alchemage() {
            super("<e>Alchemage", CrimsonSkins.ALCHEMAGE, new Pos(-86, 118, -771), CrimsonFaction.MAGE, false);
        }

        @Override
        protected List<Delivery> deliveries() {
            return CHAIN;
        }
    }

    public static class Baar extends GoldResearchNPC {
        private static final List<Delivery> CHAIN = chain(
                new Delivery("DIVER_BOOTS", 1, "I heard there exist golden boots that helps you swim faster in water. I would like to test their magical properties, could you bring me them?"),
                new Delivery("DIVER_CHESTPLATE", 1, ARMOUR_LINE));

        public Baar() {
            super("<e>Baar", CrimsonSkins.BAAR, new Pos(-638, 123, -791), CrimsonFaction.BARBARIAN, true);
        }

        @Override
        protected List<Delivery> deliveries() {
            return CHAIN;
        }
    }
}
