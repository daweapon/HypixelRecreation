package dev.daweapon.type.kuudra.fight;

import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraPerkShop;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What each player bought in the run's Perk Shop (research K1 4): one route for the whole run, levels in that
 * route's perks, Auto Revives and the one-off items. Purchases are paid from the player's token balance.
 */
public final class KuudraPerks {
    public enum Result {BOUGHT, NOT_ENOUGH_TOKENS, ROUTE_ALREADY_CHOSEN, NO_ROUTE, WRONG_ROUTE, MAXED, UNKNOWN}

    private static final Pattern NUMBER = Pattern.compile("-?[\\d,]*\\.?\\d+");

    private final KuudraPerkShop shop;
    private final KuudraTokens tokens;
    private final int routeCost;
    private final Map<UUID, Loadout> loadouts = new ConcurrentHashMap<>();

    public KuudraPerks(KuudraPerkShop shop, KuudraTokens tokens, int routeCost) {
        this.shop = shop;
        this.tokens = tokens;
        this.routeCost = routeCost;
    }

    public int routeCost() {
        return routeCost;
    }

    public static final class Loadout {
        private @Nullable String route;
        private final Map<String, Integer> levels = new ConcurrentHashMap<>();
        private int autoRevives;
        private int humanCannonballs;

        public @Nullable String route() {
            return route;
        }

        public int level(String perkId) {
            return levels.getOrDefault(perkId, 0);
        }

        public int autoRevives() {
            return autoRevives;
        }

        public boolean useAutoRevive() {
            if (autoRevives <= 0) return false;
            autoRevives--;
            return true;
        }

        public int humanCannonballs() {
            return humanCannonballs;
        }

        public boolean useHumanCannonball() {
            if (humanCannonballs <= 0) return false;
            humanCannonballs--;
            return true;
        }
    }

    public Loadout of(UUID player) {
        return loadouts.computeIfAbsent(player, _ -> new Loadout());
    }

    public KuudraPerkShop shop() {
        return shop;
    }

    public Result chooseRoute(UUID player, String routeId) {
        if (shop.route(routeId) == null) return Result.UNKNOWN;
        Loadout loadout = of(player);
        if (loadout.route != null) return Result.ROUTE_ALREADY_CHOSEN;
        if (!tokens.spend(player, routeCost)) return Result.NOT_ENOUGH_TOKENS;
        loadout.route = routeId;
        return Result.BOUGHT;
    }

    public Result upgrade(UUID player, String perkId) {
        Loadout loadout = of(player);
        if (loadout.route == null) return Result.NO_ROUTE;
        KuudraPerkShop.Perk perk = perk(loadout.route, perkId);
        if (perk == null) return Result.WRONG_ROUTE;
        int next = loadout.level(perkId) + 1;
        if (next > perk.maxLevel()) return Result.MAXED;
        if (!tokens.spend(player, perk.cost(next))) return Result.NOT_ENOUGH_TOKENS;
        loadout.levels.put(perkId, next);
        return Result.BOUGHT;
    }

    /** A basic purchase (Auto Revive, Human Cannonball, Elle's tools) or the player's route special, paid in tokens. */
    public Result buy(UUID player, String purchaseId) {
        Loadout loadout = of(player);
        KuudraPerkShop.Purchase purchase = shop.basic().stream().filter(each -> each.id().equals(purchaseId)).findFirst().orElse(null);
        if (purchase == null) {
            if (loadout.route == null) return Result.NO_ROUTE;
            KuudraPerkShop.Route route = shop.route(loadout.route);
            purchase = route.specials().stream().filter(each -> each.id().equals(purchaseId)).findFirst().orElse(null);
            if (purchase == null) return Result.WRONG_ROUTE;
        }
        if (purchase.currency() != KuudraPerkShop.Currency.TOKENS) return Result.UNKNOWN;
        if (!tokens.spend(player, cost(player, purchase))) return Result.NOT_ENOUGH_TOKENS;
        switch (purchase.id()) {
            case "AUTO_REVIVE" -> loadout.autoRevives++;
            case "HUMAN_CANNONBALL" -> loadout.humanCannonballs++;
            default -> {
            }
        }
        return Result.BOUGHT;
    }

    /** Human Cannonball is cheaper for Specialists (K1 4). */
    public int cost(UUID player, KuudraPerkShop.Purchase purchase) {
        if (purchase.id().equals("HUMAN_CANNONBALL")) {
            String route = of(player).route;
            KuudraPerkShop.Route chosen = route == null ? null : shop.route(route);
            if (chosen != null && chosen.humanCannonballCost() != null) return chosen.humanCannonballCost();
        }
        return purchase.cost();
    }

    public @Nullable KuudraPerkShop.Perk perk(String routeId, String perkId) {
        KuudraPerkShop.Route route = shop.route(routeId);
        if (route == null) return null;
        return route.perks().stream().filter(perk -> perk.id().equals(perkId)).findFirst().orElse(null);
    }

    /** The first number of the player's level in a perk ("+25%" is 25, "0.7s" is 0.7), or 0 without it. */
    public double value(UUID player, String perkId) {
        return number(player, perkId, 0);
    }

    /** The second number of a perk value ("2.1% + 600,000" gives 600,000), or 0. */
    public double secondValue(UUID player, String perkId) {
        return number(player, perkId, 1);
    }

    private double number(UUID player, String perkId, int index) {
        Loadout loadout = of(player);
        int level = loadout.level(perkId);
        if (level <= 0 || loadout.route == null) return 0;
        KuudraPerkShop.Perk perk = perk(loadout.route, perkId);
        if (perk == null) return 0;
        List<Double> numbers = numbers(perk.value(level));
        return index < numbers.size() ? numbers.get(index) : 0;
    }

    static List<Double> numbers(String text) {
        List<Double> numbers = new ArrayList<>();
        Matcher matcher = NUMBER.matcher(text);
        while (matcher.find()) {
            String found = matcher.group().replace(",", "");
            if (!found.isEmpty() && !found.equals("-")) numbers.add(Math.abs(Double.parseDouble(found)));
        }
        return numbers;
    }

    /** The share (0.1 for "+10%") a percentage perk gives. */
    public double fraction(UUID player, String perkId) {
        return value(player, perkId) / 100;
    }
}
