package dev.daweapon.type.skyblockgeneric.economy;

import dev.daweapon.commons.economy.CoinFlowStore.Kind;

import java.util.List;
import java.util.Map;

public final class CoinFlowCategories {
    public record Classified(Kind kind, String category) {}

    private record Rule(List<String> classes, Kind kind, String category) {}

    private static final List<Rule> RULES = List.of(
            new Rule(List.of("BazaarConnector", "GUIBazaar"), Kind.TRANSFER, "Bazaar"),
            new Rule(List.of("AuctionBidding", "AuctionClaims"), Kind.TRANSFER, "Auction House"),
            new Rule(List.of("AuctionCreation"), Kind.SINK, "Auction listing fees"),
            new Rule(List.of("TradeSession", "TradeView", "TradeExchange"), Kind.TRANSFER, "Player trades"),
            new Rule(List.of("GUIBankerDeposit", "GUIBankerWithdraw", "PersonalBankService", "MissionDepositCoinsInBank",
                    "CoopBankCommand"), Kind.TRANSFER, "Bank deposits and withdrawals"),
            new Rule(List.of("CoinsCommand", "MaxStickService", "GUICentauriCoinGenerator", "PlayerWipeService",
                    "WipeSnapshotService", "AdminPlayerActionHandler"), Kind.STAFF, "Staff tools"));

    private static final Map<String, String> NAMES = Map.ofEntries(
            Map.entry("CoinDropHandler", "Mob kill coins"),
            Map.entry("CoinsPerKillHandler", "Mob kill coins"),
            Map.entry("CoinsPerMobLevelHandler", "Mob kill coins"),
            Map.entry("KillComboHandler", "Kill combos"),
            Map.entry("MinionAction", "Minion sales"),
            Map.entry("MinionShippingExtension", "Minion sales"),
            Map.entry("ShopView", "NPC shops"),
            Map.entry("ConfirmBuyView", "NPC shops"),
            Map.entry("CoinShopPrice", "NPC shops"),
            Map.entry("ReforgeStoneComponent", "Reforging"),
            Map.entry("GUIReforge", "Reforging"),
            Map.entry("MythologicalCoins", "Mythological Ritual"),
            Map.entry("GriffinBurrowService", "Mythological Ritual"),
            Map.entry("ActionSkillMiningHandler", "Mining coins"),
            Map.entry("ActionCharmingMobKill", "Mob kill coins"),
            Map.entry("SkillCategory", "Skill level rewards"),
            Map.entry("DarkAuctionHandler", "Dark Auction"),
            Map.entry("SlayerService", "Slayer quests"));

    private static final List<String> STAFF_PACKAGES = List.of(".staff.", ".centauri.");
    private static final List<String> PREFIXES = List.of("GUI", "NPC", "Action");
    private static final List<String> SUFFIXES = List.of("Service", "Handler", "Extension", "Component", "View", "Command", "Rewards");

    private CoinFlowCategories() {
    }

    public static Classified classify(String className, double delta) {
        String simple = simpleName(className);
        for (Rule rule : RULES) {
            for (String prefix : rule.classes()) {
                if (simple.startsWith(prefix)) return new Classified(rule.kind(), rule.category());
            }
        }
        for (String staffPackage : STAFF_PACKAGES) {
            if (className.contains(staffPackage)) return new Classified(Kind.STAFF, "Staff tools");
        }
        return new Classified(delta >= 0 ? Kind.SOURCE : Kind.SINK, NAMES.getOrDefault(simple, readable(simple)));
    }

    static String simpleName(String className) {
        String simple = className.substring(className.lastIndexOf('.') + 1);
        int inner = simple.indexOf('$');
        return inner > 0 ? simple.substring(0, inner) : simple;
    }

    static String readable(String simple) {
        String name = simple;
        for (String prefix : PREFIXES) {
            if (name.startsWith(prefix) && name.length() > prefix.length() && Character.isUpperCase(name.charAt(prefix.length()))) {
                name = name.substring(prefix.length());
                break;
            }
        }
        for (String suffix : SUFFIXES) {
            if (name.endsWith(suffix) && name.length() > suffix.length()) {
                name = name.substring(0, name.length() - suffix.length());
                break;
            }
        }
        return name.replaceAll("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", " ");
    }
}
