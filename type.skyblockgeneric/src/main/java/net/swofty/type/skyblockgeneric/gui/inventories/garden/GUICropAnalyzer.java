package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.analyzer.AnalyzerService;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import net.swofty.type.skyblockgeneric.garden.greenhouse.MutationCatalogue;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICropAnalyzer extends StatelessView {
    private static final int[] RING = {3, 4, 5, 12, 14, 21, 22, 23};

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
        layout.slot(13, (_, c) -> mutationSlot((SkyBlockPlayer) c.player()));
        layout.slot(15, (_, c) -> analyze((SkyBlockPlayer) c.player()), (_, c) -> {
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
        layout.slot(47, (_, _) -> ItemStacks.item(Material.REDSTONE_TORCH, Text.of("<a>How this works"), List.of(
                Text.of("<7>Hold a Mutation in your hand, then"),
                Text.of("<7>analyze it for <6>Copper<7>."),
                Text.empty(),
                Text.of("<7>The first time you analyze a mutation"),
                Text.of("<7>you complete a DNA Sequencing puzzle"),
                Text.of("<7>and earn a big first-time bonus."))));
        layout.slot(48, (_, c) -> rewards((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUIDnaMilestones()));
        Components.close(layout, 49);
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
            return ItemStacks.item(Material.BARRIER, Text.of("<c>No Mutation"), List.of(Text.of("<7>Hold a Mutation in your hand.")));
        }
        return ItemStacks.item(Material.PLAYER_HEAD, Text.of("<a>" + mutation.name()), List.of(
                Text.of("<7>Rewards <6>{} Copper", StringUtility.commaify(AnalyzerService.copperFor(player, mutation, !AnalyzerService.analyzed(player, mutation)))),
                Text.of(AnalyzerService.analyzed(player, mutation) ? "<a>Already analyzed" : "<e>Not analyzed yet")));
    }

    private ItemStack.Builder analyze(SkyBlockPlayer player) {
        MutationCatalogue.Mutation mutation = AnalyzerService.heldMutation(player);
        if (mutation == null) {
            return ItemStacks.item(Material.GRAY_CONCRETE, Text.of("<c>Analyze Mutation"), List.of(Text.of("<7>Hold a Mutation in your hand.")));
        }
        boolean analyzed = AnalyzerService.analyzed(player, mutation);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(analyzed ? "<7>You have already analyzed this mutation." : "<7>Complete DNA Sequencing to analyze it."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Cost <6>{} Coins", StringUtility.commaify(mutation.coinsCost())));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to analyze!"));
        return ItemStacks.item(Material.LIGHT_GRAY_CONCRETE, Text.of("<a>Analyze Mutation"), lore);
    }

    private ItemStack.Builder rewards(SkyBlockPlayer player) {
        var data = GreenhouseService.data(player);
        return ItemStacks.item(Material.CHEST, Text.of("<6>Mutation Rewards"), List.of(
                Text.of("<7>When you analyze new mutations, you will"),
                Text.of("<7>receive new rewards for your efforts!"),
                Text.empty(),
                Text.of("<7>Unique analyzed: <a>{}", AnalyzerService.milestoneProgress(data)),
                AnalyzerService.milestoneClaimable(data) ? Text.of("<a>You have a reward to claim!") : Text.empty(),
                Text.of("<e>Click to view!")));
    }

    private ItemStack.Builder mass(SkyBlockPlayer player) {
        var entries = AnalyzerService.massCandidates(player);
        List<Text> lore = new ArrayList<>(List.of(Text.of("<7>Grab Mutations from your inventory to be"), Text.of("<7>mass analyzed."), Text.empty()));
        if (entries.isEmpty()) {
            lore.add(Text.of("<c>You have no analyzed Mutations!"));
        } else {
            lore.add(Text.of("<7>You have:"));
            for (AnalyzerService.MassEntry entry : entries) {
                lore.add(Text.of("<8>{}x <a>{} <7>worth <6>{} Copper", entry.count(), entry.mutation().name(),
                        StringUtility.commaify(AnalyzerService.copperFor(player, entry.mutation(), false) * entry.count())));
            }
            lore.add(Text.empty());
            lore.add(Text.of("<7>Totaling <6>{} Copper", StringUtility.commaify(AnalyzerService.massCopper(player, entries))));
            lore.add(Text.of("<7>Cost <6>{} Coins", StringUtility.commaify(AnalyzerService.massCost(entries))));
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to analyze!"));
        }
        return ItemStacks.item(Material.HOPPER, Text.of("<a>Mass Analyze"), lore);
    }
}
