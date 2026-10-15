package dev.daweapon.type.skyblockgeneric.crimson.kuudra.data;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record KuudraPerkShop(List<Purchase> basic, List<Route> routes) {

    public enum Currency { TOKENS, COINS }

    /** A one-off purchase: the basic perks and each route's special. */
    public record Purchase(String id, String name, int cost, Currency currency, String effect) {
        static Purchase parse(Map<String, Object> yaml) {
            return new Purchase(Yamls.string(yaml, "id"), Yamls.string(yaml, "name"), Yamls.integer(yaml, "cost", 0),
                    Currency.valueOf(Yamls.string(yaml, "currency")), Yamls.string(yaml, "effect"));
        }
    }

    public record Perk(String id, String name, String effect, List<String> values, List<Integer> costs) {
        static Perk parse(Map<String, Object> yaml) {
            Perk perk = new Perk(Yamls.string(yaml, "id"), Yamls.string(yaml, "name"), Yamls.string(yaml, "effect"),
                    Yamls.strings(yaml.get("values")), Yamls.integers(yaml.get("costs")));
            if (perk.values.size() != perk.costs.size()) {
                throw new IllegalArgumentException("Perk " + perk.id + " has " + perk.values.size() + " values but "
                        + perk.costs.size() + " costs");
            }
            return perk;
        }

        public int maxLevel() {
            return costs.size();
        }

        public int cost(int level) {
            return costs.get(level - 1);
        }

        public String value(int level) {
            return values.get(level - 1);
        }
    }

    public record Route(String id, String name, String description, List<Purchase> specials,
                        @Nullable Integer humanCannonballCost, List<Perk> perks) {
        static Route parse(Map<String, Object> yaml) {
            Object cannonball = yaml.get("human_cannonball_cost");
            return new Route(Yamls.string(yaml, "id"), Yamls.string(yaml, "name"), Yamls.string(yaml, "description"),
                    Yamls.maps(yaml.get("specials")).stream().map(Purchase::parse).toList(),
                    cannonball == null ? null : ((Number) cannonball).intValue(),
                    Yamls.maps(yaml.get("perks")).stream().map(Perk::parse).toList());
        }
    }

    static KuudraPerkShop parse(Map<String, Object> yaml) {
        return new KuudraPerkShop(
                Yamls.maps(yaml.get("basic")).stream().map(Purchase::parse).toList(),
                Yamls.maps(yaml.get("routes")).stream().map(Route::parse).toList());
    }

    public @Nullable Route route(String id) {
        return routes.stream().filter(route -> route.id().equals(id)).findFirst().orElse(null);
    }
}
