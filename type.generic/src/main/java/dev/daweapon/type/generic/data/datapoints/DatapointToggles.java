package dev.daweapon.type.generic.data.datapoints;

import lombok.AllArgsConstructor;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.generic.data.Datapoint;

import java.util.HashMap;
import java.util.Map;

// Datapoint storing a player's per-feature boolean toggle preferences, serialized as a comma-separated key:value list.
public class DatapointToggles extends Datapoint<DatapointToggles.Toggles> {

    public DatapointToggles(String key, DatapointToggles.Toggles value) {
        super(key, value, new Serializer<>() {
            @Override
            public String serialize(Toggles value) {
                StringBuilder builder = new StringBuilder();

                for (Map.Entry<Toggles.ToggleType, Boolean> entry : value.toggles.entrySet()) {
                    builder.append(entry.getKey().name()).append(":").append(entry.getValue()).append(",");
                }

                return builder.toString();
            }

            @Override
            public Toggles deserialize(String json) {
                Toggles toggles = new Toggles(new HashMap<>());
                String[] split = json.split(",");

                if (split.length == 1 && split[0].isEmpty()) {
                    return toggles;
                }

                for (String s : split) {
                    String[] split1 = s.split(":");
                    toggles.toggles.put(Toggles.ToggleType.valueOf(split1[0]), Boolean.parseBoolean(split1[1]));
                }

                return toggles;
            }

            @Override
            public Toggles clone(Toggles value) {
                return new Toggles(new HashMap<>(value.toggles));
            }
        });
    }

    public DatapointToggles(String key) {
        this(key, new Toggles(new HashMap<>()));
    }

    @AllArgsConstructor
    public static class Toggles {
        private Map<ToggleType, Boolean> toggles;

        public boolean get(ToggleType type) {
            return toggles.getOrDefault(type, type.defaultValue);
        }

        public void set(ToggleType type, boolean value) {
            toggles.put(type, value);
        }

        public boolean inverse(ToggleType type) {
            // toggles.put(type, !toggles.getOrDefault(type, type.defaultValue));
			boolean newValue = !toggles.getOrDefault(type, type.defaultValue);
			toggles.put(type, newValue);
			return newValue;
        }

        public enum ToggleType {
            PURCHASE_CONFIRMATION_BITS(true),
            SKYBLOCK_LEVELS_IN_CHAT(true),
            DISABLE_DROP_MESSAGES(false),
            HAS_SPOKEN_TO_BEA(false),
            HAS_SPOKEN_TO_GAVIN(false),
            HAS_VISITED_DARK_AUCTION(false),
            HAS_SPOKEN_TO_TIA(false),
            HAS_SPOKEN_TO_LIFT_OPERATOR(false),
            HAS_SPOKEN_TO_BUBU(false),
            HAS_SPOKEN_TO_DULIN(false),
            HAS_SPOKEN_TO_DULIN_AT_CAMP(false),
            HAS_SPOKEN_TO_SOR_HEN(false),
            HAS_SPOKEN_TO_FOSSIL_MUNCHER(false),
            HAS_SPOKEN_TO_DR_STONE(false),
            HAS_SPOKEN_TO_RESEARCHER_BERYL(false),
            HAS_SPOKEN_TO_LAZY_MINER(false),
            HAS_SPOKEN_TO_BAKER(false),
            HAS_SPOKEN_TO_FISHERMAN_GERALD(false),
            HAS_SPOKEN_TO_RUSTY(false),
            HAS_SPOKEN_TO_RUSTY_ABOUT_PICKAXE(false),
            HAS_SPOKEN_TO_RUSTY_ABOUT_ABIPHONE(false),
            HAS_GIVEN_WALTER_CUBE(false),
            HAS_GIVEN_BUILDER_BUILDERS_WAND(false),
            HAS_SPOKEN_TO_CURATOR(false),
            HAS_SPOKEN_TO_MADAME_ELEANOR(false),
            HAS_DONE_COAL_TRADE_WITH_BLACKSMITH(false),
            HAS_SPOKEN_TO_SEYMOUR(false),
            HAS_SPOKEN_TO_FISH_MERCHANT(false),
            HAS_SPOKEN_TO_GOLD_FORGER(false),
            HAS_SPOKEN_TO_IRON_FORGER(false),
            HAS_SPOKEN_TO_GOLD_BLACKSMITH(false),
            HAS_SPOKEN_TO_FARM_MERCHANT(false),
            HAS_SPOKEN_TO_ADVENTURER(false),
            HAS_SPOKEN_TO_EINARY(false),
            HAS_SAID_MOLBERT_IS_MOLE(false),
            HAS_SPOKEN_TO_LUMBER_MERCHANT(false),
            HAS_SPOKEN_TO_MINE_MERCHANT(false),
            HAS_SPOKEN_TO_MASTER_TACTICIAN_FUNK(false),
            HAS_SPOKEN_TO_WEAPONSMITH(false),
            HAS_SPOKEN_TO_BUILDER(false),
            HAS_SPOKEN_TO_WOOL_WEAVER(false),
            HAS_SPOKEN_TO_MAD_REDSTONE_ENGINEER(false),
            HAS_SPOKEN_TO_ZOG(false),
            HAS_SPOKEN_TO_JAX(false),
            HAS_REALLY_SPOKEN_TO_JAX(false),
            HAS_SPOKEN_TO_ALCHEMIST(false),
            HAS_SPOKEN_TO_BRAMASS_BEASTSLAYER(false),
            HAS_SPOKEN_TO_RICK(false),
            HAS_SPOKEN_TO_MICHAEL(false),
            HAS_SPOKEN_TO_IKE(false),
            HAS_SPOKEN_TO_EMISSARY_BRAUM(false),
            HAS_SPOKEN_TO_GEO(false),
            HAS_SPOKEN_TO_AMELIA(false),
            HAS_SPOKEN_TO_CHRISTOPHER(false),
            HAS_BEEN_REFUSED_BY_DAMIA(false),
            HAS_SPOKEN_TO_DAMIA(false),
            HAS_SPOKEN_TO_FANN(false),
            HAS_SPOKEN_TO_GWYNNIE(false),
            HAS_CLAIMED_ANGLER_ANGUS_BAIT(false),
            PAPER_ICONS(false),
			LOBBY_SHOW_PLAYERS(true),
			AUTO_ACCEPT_QUESTS(false),
            RUSTY_PURCHASE_CONFIRMATION(false),
            RUSTY_SORT_BY_RARITY(false),
            HAS_SPOKEN_TO_FISHERWOMAN_ENID(false),
            HAS_SPOKEN_TO_CAPTAIN_BAHA(false),
            HAS_SPOKEN_TO_RODDY(false),
            HAS_SPOKEN_TO_MADDOX(false),
            HAS_SPOKEN_TO_MALIK(false),
            HAS_SPOKEN_TO_MORT(false),
            HAS_SPOKEN_TO_OPHELIA(false),
            HAS_SPOKEN_TO_TAYLOR(false),
            HAS_SPOKEN_TO_VINCENT(false),
            HAS_SPOKEN_TO_WIZARD(false),
            HAS_SPOKEN_TO_RICHARD(false),
            HAS_SPOKEN_TO_ZARINA(false),
            HAS_SPOKEN_TO_LIZ(false),
            HAS_CAUGHT_FIRST_FISH(false),
            HAS_UNLOCKED_SHIP(false),
            HAS_VISITED_BACKWATER_BAYOU(false),
            HAS_SPOKEN_TO_FROSTY(false),
            HAS_SPOKEN_TO_FROZEN_ALEX(false),
            HAS_GIVEN_FROZEN_ALEX_LAVA_BUCKET(false),
            PERSONAL_DELETOR_COBBLESTONE(true),
            PERSONAL_DELETOR_DIRT(true),
            PERSONAL_DELETOR_NETHERRACK(true),
            PERSONAL_DELETOR_ROTTEN_FLESH(true),
            PERSONAL_DELETOR_GRAVEL(true),
            PERSONAL_DELETOR_SAND(true),
            PERSONAL_DELETOR_STRING(true),
            PERSONAL_DELETOR_BONE(true),
            PERSONAL_COMPACTOR_IRON(true),
            PERSONAL_COMPACTOR_GOLD(true),
            PERSONAL_COMPACTOR_DIAMOND(true),
            PERSONAL_COMPACTOR_EMERALD(true),
            PERSONAL_COMPACTOR_COAL(true),
            PERSONAL_COMPACTOR_REDSTONE(true),
            PERSONAL_COMPACTOR_LAPIS(true),
            HALVE_DARK_AUCTION_CALENDAR_ENTRIES(false),
            HAS_SPOKEN_TO_HATTIE(false),
            HAS_SPOKEN_TO_JOTRAELINE(false),
            HAS_SPOKEN_TO_BRYNMOR(false);

            private final boolean defaultValue;

            ToggleType(boolean defaultValue) {
                this.defaultValue = defaultValue;
            }
        }
    }
}
