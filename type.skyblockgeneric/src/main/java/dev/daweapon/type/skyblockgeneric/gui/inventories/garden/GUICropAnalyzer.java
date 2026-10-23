package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.analyzer.AnalyzerService;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.MutationCatalogue;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICropAnalyzer extends StatelessView {
    private static final int[] RING = {12, 13, 14, 21, 23, 30, 31, 32};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Crop Analyzer"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        for (int slot : RING) {
            layout.slot(slot, (_, c) -> ItemStacks.filler(AnalyzerService.heldMutation((SkyBlockPlayer) c.player()) == null
                    ? Material.BLUE_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE));
        }
        layout.slot(8, (_, _) -> ItemStacks.item(Material.MAP, Text.of("<a>Sky Mutations"), List.of(
                Text.of("<8>(From SkyHanni)"),
                Text.empty(),
                Text.of("<7>Click here to open"),
                Text.of("<7>guides and other useful"),
                Text.of("<7>information about mutations"),
                Text.of("<7>on <a>skymutations.eu"))));
        layout.slot(22, (_, c) -> mutationSlot((SkyBlockPlayer) c.player()), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            MutationCatalogue.Mutation mutation = AnalyzerService.heldMutation(player);
            if (mutation == null) {
                player.sendMessage(Text.of("<c>Hold a Mutation in your hand to analyze it!"));
                return;
            }
            if (!AnalyzerService.canAfford(player, mutation.coinsCost())) {
                player.sendMessage(Text.of("<c>You need {} Coins to analyze this!", StringUtility.commaify(mutation.coinsCost())));
                return;
            }
            if (AnalyzerService.analyzed(player, mutation)) {
                AnalyzerService.complete(player, mutation);
                c.session(DefaultState.class).refresh();
            } else {
                c.push(new GUIDna(mutation));
            }
        });
        layout.slot(48, (_, c) -> rewards((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUIDnaMilestones()));
        Components.close(layout, 49);
        layout.slot(50, (_, c) -> allMutations((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUIAllMutations()));
        layout.slot(51, (_, c) -> mass((SkyBlockPlayer) c.player()), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            var entries = AnalyzerService.massCandidates(player);
            if (entries.isEmpty()) {
                player.sendMessage(Text.of("<c>You have no analyzed Mutations to mass analyze!"));
                return;
            }
            if (!AnalyzerService.massAnalyze(player)) {
                player.sendMessage(Text.of("<c>You need {} Coins to do that!", StringUtility.commaify(AnalyzerService.massCost(entries))));
            }
            c.session(DefaultState.class).refresh();
        });
    }

    private ItemStack.Builder mutationSlot(SkyBlockPlayer player) {
        MutationCatalogue.Mutation mutation = AnalyzerService.heldMutation(player);
        if (mutation == null) {
            return ItemStacks.item(Material.BARRIER, Text.of("<c>No Mutation"), List.of(
                    Text.of("<7>Hold a Mutation in your hand,"),
                    Text.of("<7>then click here to analyze it.")));
        }
        boolean analyzed = AnalyzerService.analyzed(player, mutation);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(analyzed ? "<7>You have already analyzed this mutation." : "<7>Complete DNA Sequencing to analyze it."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rewards <c>{} Copper", StringUtility.commaify(AnalyzerService.copperFor(player, mutation, !analyzed))));
        lore.add(Text.of("<7>Cost <6>{} Coins", StringUtility.commaify(mutation.coinsCost())));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to analyze!"));
        return ItemStacks.item(Material.PLAYER_HEAD, Text.of("<a>" + mutation.name()), lore);
    }

    private ItemStack.Builder rewards(SkyBlockPlayer player) {
        var data = GreenhouseService.data(player);
        int totalXp = AnalyzerService.MILESTONE_TARGETS.length * AnalyzerService.SKYBLOCK_XP_PER_MILESTONE;
        return ItemStacks.item(Material.GOLD_BLOCK, Text.of("<6>Mutation Rewards"), List.of(
                Text.of("<7>When you analyze new <c>mutations<7>, you"),
                Text.of("<7>will receive <a>new rewards<7> for your"),
                Text.of("<7>efforts!"),
                Text.empty(),
                Text.of("<7>Total XP: <b>{}/{}", data.getDnaClaimed() * AnalyzerService.SKYBLOCK_XP_PER_MILESTONE, totalXp),
                AnalyzerService.milestoneClaimable(data) ? Text.of("<a>You have a reward to claim!") : Text.empty(),
                Text.empty(),
                Text.of("<e>Click to view!")));
    }

    private ItemStack.Builder allMutations(SkyBlockPlayer player) {
        var data = GreenhouseService.data(player);
        return ItemStacks.item(Material.PAINTING, Text.of("<a>All Mutations"), List.of(
                Text.empty(),
                Text.of("<c>Mutations <7>have a chance to spread"),
                Text.of("<7>from growing <a>crops <7>to nearby empty"),
                Text.of("<7>plots within the <a>Greenhouse<7>."),
                Text.of("<7>Each <c>mutation <7>requires a specific"),
                Text.of("<7>combination of <a>crops <7>to spread."),
                Text.empty(),
                Text.of("<7>For detailed requirements and"),
                Text.of("<7>spread conditions, check out your"),
                Text.of("<7>unlocked <c>mutations<7>!"),
                Text.empty(),
                Text.of("<7>Total Analyzed: <b>{}/{}", AnalyzerService.milestoneProgress(data), MutationCatalogue.get().all().size()),
                Text.empty(),
                Text.of("<e>Click to view!")));
    }

    private ItemStack.Builder mass(SkyBlockPlayer player) {
        var entries = AnalyzerService.massCandidates(player);
        List<Text> lore = new ArrayList<>(List.of(Text.of("<7>Grab <c>mutations<7> from your inventory"), Text.of("<7>and sacks to be mass analyzed."), Text.empty(),
                Text.of("<7>Source: <a>Inventory + Sacks"), Text.empty()));
        if (entries.isEmpty()) {
            lore.add(Text.of("<c>You have no analyzed Mutations!"));
        } else {
            lore.add(Text.of("<7>You have: "));
            for (AnalyzerService.MassEntry entry : entries) {
                lore.add(Text.of("  <a>{}<7>x {}{} <7>worth <c>{} Copper", entry.count(), rarityColour(entry.mutation().rarity()), entry.mutation().name(),
                        StringUtility.commaify(AnalyzerService.copperFor(player, entry.mutation(), false) * entry.count())));
            }
            lore.add(Text.empty());
            lore.add(Text.of("<7>Totaling <c>{} Copper<7>!", StringUtility.commaify(AnalyzerService.massCopper(player, entries))));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Cost"));
            lore.add(Text.of("<6>{} Coins", StringUtility.commaify(AnalyzerService.massCost(entries))));
            lore.add(Text.empty());
            lore.add(Text.of("<e>Left-Click to analyze!"));
        }
        return ItemStacks.item(Material.PLAYER_HEAD, Text.of("<a>Mass Analyze"), lore);
    }

    private static String rarityColour(String rarity) {
        return switch (rarity) {
            case "UNCOMMON" -> "<a>";
            case "RARE" -> "<9>";
            case "EPIC" -> "<5>";
            case "LEGENDARY" -> "<6>";
            default -> "<f>";
        };
    }
}
