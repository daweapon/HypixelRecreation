package net.swofty.type.kuudra.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.kuudra.fight.KuudraFight;
import net.swofty.type.kuudra.fight.KuudraPerks;
import net.swofty.type.kuudra.fight.world.KuudraItems;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraPerkShop;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The Kuudra Perk Menu, laid out as in the Burning capture. Before a route is chosen the four routes sit on the
 * second row and the basic purchases on the fourth; afterwards each party member's upgrades head runs down the
 * left, the player's own route perks fill the middle and the basic purchases move to the right.
 */
public class GUIKuudraPerkShop extends StatelessView {
    private static final int[] ROUTE_SLOTS = {10, 12, 14, 16};
    private static final Material[] ROUTE_ICONS = {Material.PLAYER_HEAD, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_SWORD,
            Material.PLAYER_HEAD};
    private static final String[] ROUTE_COLOURS = {"<6><l>", "<d><l>", "<c><l>", "<a><l>"};
    private static final int[] BASIC_SLOTS_BEFORE = {29, 30, 32, 33};
    private static final int REMOTE_SLOT = 31;
    private static final int[] MEMBER_SLOTS = {10, 19, 28, 37};
    private static final int[] PERK_SLOTS = {12, 13, 14, 21, 22, 23, 30, 31, 32};
    private static final int[] BASIC_SLOTS_AFTER = {16, 25, 34, 43};
    private static final Map<String, Material> PERK_ICONS = Map.ofEntries(
            Map.entry("ACCELERATED_SHOT", Material.FEATHER), Map.entry("BLAST_RADIUS", Material.TNT),
            Map.entry("CANNON_PROFICIENCY", Material.BLAZE_POWDER), Map.entry("MULTI_SHOT", Material.FIRE_CHARGE),
            Map.entry("RAPID_FIRE", Material.NETHER_STAR), Map.entry("STEADY_AIM", Material.SPECTRAL_ARROW),
            Map.entry("SWEEPING_EDGE", Material.IRON_SWORD), Map.entry("FREEZING_TOUCH", Material.PACKED_ICE),
            Map.entry("BONUS_DAMAGE", Material.DIAMOND_SWORD), Map.entry("ANTIBIOTIC", Material.SLIME_BALL),
            Map.entry("BLIGHT_SLAYER", Material.ROTTEN_FLESH), Map.entry("STEADY_HANDS", Material.CHEST),
            Map.entry("BALLISTA_MECHANIC", Material.ANVIL), Map.entry("BOMBERMAN", Material.GUNPOWDER),
            Map.entry("MINING_FRENZY", Material.IRON_PICKAXE), Map.entry("HEALING_AURA", Material.GOLDEN_APPLE),
            Map.entry("MANA_AURA", Material.LAPIS_LAZULI), Map.entry("PROTECTIVE_AURA", Material.SHIELD),
            Map.entry("FASTER_RESPAWN", Material.CLOCK), Map.entry("MINIATURE_NUKE", Material.TNT),
            Map.entry("REVIVE_FINAL_KILLED", Material.TOTEM_OF_UNDYING), Map.entry("REVIVE_DEAD", Material.GOLDEN_CARROT));

    private final KuudraFight fight;

    public GUIKuudraPerkShop(KuudraFight fight) {
        this.fight = fight;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Perk Menu", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
        KuudraPerks.Loadout mine = fight.perks().of(viewer.getUuid());
        KuudraPerkShop.Route route = mine.route() == null ? null : fight.shop().route(mine.route());
        List<KuudraPerkShop.Purchase> basic = fight.shop().basic().stream()
                .filter(purchase -> purchase.currency() == KuudraPerkShop.Currency.TOKENS).toList();

        if (route == null) {
            List<KuudraPerkShop.Route> routes = fight.shop().routes();
            for (int i = 0; i < routes.size() && i < ROUTE_SLOTS.length; i++) {
                KuudraPerkShop.Route each = routes.get(i);
                int index = i;
                layout.slot(ROUTE_SLOTS[i], routeItem(each, index), (_, c) -> c.push(new GUIConfirmRoute(fight, each)));
            }
            placeBasics(layout, viewer, basic, BASIC_SLOTS_BEFORE);
        } else {
            placeMembers(layout, viewer);
            List<Object> entries = new ArrayList<>(route.perks());
            entries.addAll(route.specials());
            for (int i = 0; i < entries.size() && i < PERK_SLOTS.length; i++) {
                if (entries.get(i) instanceof KuudraPerkShop.Perk perk) {
                    layout.slot(PERK_SLOTS[i], (_, _) -> perkItem(perk, fight.perks().of(viewer.getUuid()).level(perk.id())),
                            (_, c) -> purchase(viewer, c, perk.name(), () -> fight.perks().upgrade(viewer.getUuid(), perk.id())));
                } else if (entries.get(i) instanceof KuudraPerkShop.Purchase special) {
                    layout.slot(PERK_SLOTS[i], purchaseItem(special, special.cost(), null), (_, c) -> buySpecial(viewer, c, special));
                }
            }
            placeBasics(layout, viewer, basic, BASIC_SLOTS_AFTER);
        }
        fight.shop().basic().stream().filter(purchase -> purchase.currency() == KuudraPerkShop.Currency.COINS).findFirst()
                .ifPresent(remote -> layout.slot(REMOTE_SLOT, ItemStacks.item(Material.ENDER_CHEST, """
                        <a>{}
                        <7>{}

                        <7>Cost: <6>{} Coins

                        <e>Click to purchase!""", remote.name(), remote.effect(), String.format("%,d", remote.cost())),
                        (_, c) -> buyRemote(viewer, c, remote)));
    }

    private ItemStack.Builder routeItem(KuudraPerkShop.Route route, int index) {
        StringBuilder lore = new StringBuilder("<a>" + route.name() + " Route\n" + ROUTE_COLOURS[index] + route.name()
                + "\n<7>" + route.description() + "\n\n<7>Perks:");
        for (KuudraPerkShop.Perk perk : route.perks()) {
            lore.append("\n<8>• <b>").append(perk.name()).append(" <8>(I-").append(roman(perk.maxLevel())).append(")");
        }
        for (KuudraPerkShop.Purchase special : route.specials()) {
            lore.append("\n<8>• <b>").append(special.name()).append(" <8>(Consumable)");
        }
        lore.append("\n\n<7>Cost to unlock: <5>").append(fight.perks().routeCost()).append(" Tokens")
                .append("\n<7>You cannot change routes once you")
                .append("\n<7>have selected one!")
                .append("\n\n<e>Click to unlock!");
        return ItemStacks.item(ROUTE_ICONS[index], lore.toString());
    }

    private void placeMembers(ViewLayout<DefaultState> layout, SkyBlockPlayer viewer) {
        List<UUID> members = new ArrayList<>(fight.run().roster());
        members.sort(Comparator.comparing((UUID uuid) -> !uuid.equals(viewer.getUuid())).thenComparing(UUID::toString));
        for (int i = 0; i < members.size() && i < MEMBER_SLOTS.length; i++) {
            UUID member = members.get(i);
            layout.slot(MEMBER_SLOTS[i], (_, _) -> memberItem(member, member.equals(viewer.getUuid())));
        }
    }

    private ItemStack.Builder memberItem(UUID member, boolean you) {
        SkyBlockPlayer online = fight.run().onlineMembers().stream().filter(p -> p.getUuid().equals(member)).findFirst().orElse(null);
        KuudraPerks.Loadout loadout = fight.perks().of(member);
        KuudraPerkShop.Route route = loadout.route() == null ? null : fight.shop().route(loadout.route());
        StringBuilder text = new StringBuilder((online == null ? "<7>Offline" : online.getFullDisplayName().toString())
                + "<f>'s<a> Upgrades" + (you ? "<8> (YOU)" : ""));
        text.append("\n<7>Current Tokens:\n<5>").append(fight.tokens().balance(member)).append(" Tokens")
                .append("\n\n<7>Selected Route:\n").append(route == null ? "<c>None" : "<6>" + route.name());
        if (route != null) {
            text.append("\n\n<7>Perk Levels:");
            for (KuudraPerkShop.Perk perk : route.perks()) {
                int level = loadout.level(perk.id());
                text.append("\n<8>• <f>").append(perk.name()).append(": ").append(level == 0 ? "<8>-" : "<a>" + roman(level));
            }
        }
        return ItemStacks.item(Material.PLAYER_HEAD, text.toString());
    }

    private void placeBasics(ViewLayout<DefaultState> layout, SkyBlockPlayer viewer, List<KuudraPerkShop.Purchase> basic, int[] slots) {
        for (int i = 0; i < basic.size() && i < slots.length; i++) {
            KuudraPerkShop.Purchase purchase = basic.get(i);
            layout.slot(slots[i], (_, _) -> purchaseItem(purchase, fight.perks().cost(viewer.getUuid(), purchase),
                            purchase.id().equals("AUTO_REVIVE") ? fight.perks().of(viewer.getUuid()).autoRevives() : null),
                    (_, c) -> buyBasic(viewer, c, purchase));
        }
    }

    private static ItemStack.Builder perkItem(KuudraPerkShop.Perk perk, int level) {
        Material material = PERK_ICONS.getOrDefault(perk.id(), Material.PAPER);
        if (level >= perk.maxLevel()) {
            return ItemStacks.item(material, "<a>{} {}\n<7>{}\n\n<7>Current: <a>{}\n\n<a>MAXED", perk.name(), roman(level),
                    perk.effect(), perk.value(level));
        }
        String current = level == 0 ? "0" : perk.value(level);
        return ItemStacks.item(material, "<a>{} {}\n<7>{}\n\n<7>{} <8>→ <a>{}\n\n<7>Cost: <5>{} Tokens\n\n<e>Click to upgrade!",
                perk.name(), roman(level + 1), perk.effect(), current, perk.value(level + 1), perk.cost(level + 1));
    }

    private static ItemStack.Builder purchaseItem(KuudraPerkShop.Purchase purchase, int cost, @Nullable Integer owned) {
        Material material = switch (purchase.id()) {
            case "AUTO_REVIVE" -> Material.TOTEM_OF_UNDYING;
            case "HUMAN_CANNONBALL" -> Material.FIREWORK_STAR;
            case "ELLES_LAVA_ROD" -> Material.FISHING_ROD;
            case "ELLES_PICKAXE" -> Material.IRON_PICKAXE;
            default -> PERK_ICONS.getOrDefault(purchase.id(), Material.GOLDEN_APPLE);
        };
        String ownedLine = owned == null ? "" : "\n<7>Owned " + purchase.name() + "s: <a>" + owned;
        String specialist = purchase.id().equals("HUMAN_CANNONBALL") ? "\n<8><o>Half price for Specialist route players!" : "";
        return ItemStacks.item(material, "<a>{}\n<7>{}{}\n\n<7>Cost: <5>{} Tokens{}\n\n<e>Click to purchase!",
                purchase.name(), purchase.effect(), ownedLine, cost, specialist);
    }

    private void buyBasic(SkyBlockPlayer viewer, ViewContext ctx, KuudraPerkShop.Purchase purchase) {
        purchase(viewer, ctx, purchase.name(), () -> {
            KuudraPerks.Result result = fight.perks().buy(viewer.getUuid(), purchase.id());
            if (result == KuudraPerks.Result.BOUGHT) {
                switch (purchase.id()) {
                    case "ELLES_LAVA_ROD" -> KuudraItems.give(viewer, KuudraItems.Kind.ELLES_LAVA_ROD);
                    case "ELLES_PICKAXE" -> KuudraItems.give(viewer, KuudraItems.Kind.ELLES_PICKAXE);
                    default -> {
                    }
                }
            }
            return result;
        });
    }

    private void buySpecial(SkyBlockPlayer viewer, ViewContext ctx, KuudraPerkShop.Purchase special) {
        boolean revive = special.id().startsWith("REVIVE_");
        boolean finalKilled = special.id().equals("REVIVE_FINAL_KILLED");
        if (revive && fight.isFinalGhost(viewer)) {
            viewer.sendMessage(KuudraConfig.get().dialogue().announcement("final_revive_denied", Map.of()));
            return;
        }
        if (revive && !fight.hasGhost(finalKilled)) {
            viewer.sendMessage("<c>Nobody needs reviving!");
            return;
        }
        purchase(viewer, ctx, special.name(), () -> {
            KuudraPerks.Result result = fight.perks().buy(viewer.getUuid(), special.id());
            if (result == KuudraPerks.Result.BOUGHT) {
                if (special.id().equals("MINIATURE_NUKE")) KuudraItems.give(viewer, KuudraItems.Kind.MINIATURE_NUKE);
                if (revive) fight.revive(viewer, finalKilled);
            }
            return result;
        });
    }

    private void buyRemote(SkyBlockPlayer viewer, ViewContext ctx, KuudraPerkShop.Purchase remote) {
        synchronized (fight.run()) {
            if (viewer.getCoins() < remote.cost()) {
                viewer.sendMessage("<c>You cannot afford this!");
                return;
            }
            viewer.setCoins(viewer.getCoins() - remote.cost());
            KuudraItems.give(viewer, KuudraItems.Kind.REMOTE_PERK_SHOP);
            viewer.sendMessage("<a>You purchased " + remote.name() + "!");
        }
        ctx.session(Object.class).refresh();
    }

    void purchase(SkyBlockPlayer viewer, ViewContext ctx, String name, Supplier<KuudraPerks.Result> action) {
        KuudraPerks.Result result;
        synchronized (fight.run()) {
            result = action.get();
        }
        String message = switch (result) {
            case BOUGHT -> "<a>You purchased " + name + "!";
            case NOT_ENOUGH_TOKENS -> "<c>You don't have enough Tokens!";
            case ROUTE_ALREADY_CHOSEN -> "<c>You have already chosen a route!";
            case NO_ROUTE -> "<c>Choose a route first!";
            case WRONG_ROUTE -> "<c>That perk isn't part of your route!";
            case MAXED -> "<c>That perk is already maxed!";
            case UNKNOWN -> null;
        };
        if (message != null) viewer.sendMessage(message);
        ctx.session(Object.class).refresh();
    }

    static String roman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            default -> String.valueOf(level);
        };
    }
}
