package dev.daweapon.type.skyblockgeneric.gui.inventories.hunting;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.*;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHunting;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeFusionService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// GUI for fusing two hunting attribute shards together
public class GUIAttributeFusion implements StatefulView<GUIAttributeFusion.State> {
    private static final int MAX_LEVEL = 10;

    public record State(int page, String first, String second, boolean smart) {
    }

    @Override
    public State initialState() {
        return new State(1, null, null, false);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Fusion Box", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(Layouts.border(0, 53));
        layout.filler(Layouts.rectangle(10, 43));
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointHunting.HuntingData data = player.getHuntingData();
        AttributeDefinition first = AttributeRegistry.get(state.first());
        AttributeDefinition second = AttributeRegistry.get(state.second());
        if (first == null || second == null) {
            List<AttributeDefinition> available = AttributeRegistry.values().stream()
                    .filter(value -> !state.smart() || data.level(value.id()) >= MAX_LEVEL)
                    .filter(value -> data.shardCount(value.id()) >= (value.id().toString().equals(state.first())
                            ? AttributeFusionService.required(value) * 2 : AttributeFusionService.required(value))).toList();
            int start = (state.page() - 1) * AttributeGUIItems.CONTENT_SLOTS.length;
            for (int i = 0; i < AttributeGUIItems.CONTENT_SLOTS.length; i++) {
                int index = start + i;
                if (index >= available.size()) continue;
                AttributeDefinition definition = available.get(index);
                layout.slot(AttributeGUIItems.CONTENT_SLOTS[i],
                        (s, c) -> AttributeGUIItems.fusionShard(definition, AttributeFusionService.required(definition),
                                "<7>Fusion cost: <a>" + AttributeFusionService.required(definition), "<7>Owned: <b>" + data.shardCount(definition.id()), "<e>Click to select!"),
                        (click, c) -> c.session(State.class).setState(first == null
                                ? new State(1, definition.id().toString(), null, state.smart()) : new State(1, first.id().toString(), definition.id().toString(), state.smart())));
            }
            int pages = AttributeGUIItems.pages(available.size());
            if (state.page() > 1)
                layout.slot(45, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Previous Page"),
                        (click, c) -> c.session(State.class).setState(new State(state.page() - 1, state.first(), state.second(), state.smart())));
            if (state.page() < pages)
                layout.slot(53, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Next Page"),
                        (click, c) -> c.session(State.class).setState(new State(state.page() + 1, state.first(), state.second(), state.smart())));
        } else {
            List<AttributeFusionService.FusionResult> results = AttributeFusionService.results(first, second);
            int[] slots = {29, 31, 33};
            for (int i = 0; i < results.size(); i++) {
                AttributeFusionService.FusionResult result = results.get(i);
                layout.slot(slots[i], (s, c) -> AttributeGUIItems.fusionShard(result.definition(), result.amount(),
                                "<7>Result: <a>" + result.amount() + "x " + switch (result.type()) {
                                    case ID -> "ID Fusion";
                                    case SPECIAL -> "Special Fusion";
                                    case CHAMELEON -> "Chameleon Fusion";
                                }, "<e>Click to fuse!"),
                        (click, c) -> {
                            boolean newShard = data.shardCount(result.definition().id()) == 0;
                            if (AttributeFusionService.fuse(player, first, second, result)) {
                                player.getAchievementHandler().addProgressByTrigger("skyblock.evolution_trigger", 1);
                                dev.daweapon.type.generic.event.HypixelEventHandler.callCustomEvent(new dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent(
                                        player, result.definition(), result.amount(), dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent.Source.FUSION, false));
                                if (result.type() == AttributeFusionService.FusionType.CHAMELEON) dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers.fire(player, "fusion.chameleon");
                                if (newShard) player.getAchievementHandler().completeAchievement("skyblock.amalgamation");
                                c.player().sendMessage("<a>Fusion complete! You received <e>{}x <a>{}!",
                                        result.amount(), result.definition().shardName());
                                c.session(State.class).setState(new State(1, null, null, state.smart()));
                            } else c.player().sendMessage("<c>You no longer have enough Shards for that fusion!");
                        });
            }
        }
        layout.slot(4, (s, c) -> ItemStacks.item(Material.BARREL, """
                <a>Fusion Box
                <7>To begin a fusion you need to select
                <7>a <9>Shard <7>from your box.

                <7>You'll then need to select other
                <9>Shards <7>to fuse them with.

                <7>Once done, you'll be given from 1
                <7>to 3 result shards to choose from!

                <7>Shards: <2>{}""", data.getShards().values().stream().mapToInt(Integer::intValue).sum()));
        if (first != null)
            layout.slot(2, (s, c) -> AttributeGUIItems.fusionShard(first, AttributeFusionService.required(first), "<7>Fusion cost: <a>" + AttributeFusionService.required(first)));
        if (second != null)
            layout.slot(6, (s, c) -> AttributeGUIItems.fusionShard(second, AttributeFusionService.required(second), "<7>Fusion cost: <a>" + AttributeFusionService.required(second)));
        if (first != null) layout.slot(46, (s, c) -> ItemStacks.item(Material.RED_DYE, "<c>Reset"),
                (click, c) -> c.session(State.class).setState(new State(1, null, null, state.smart())));
        int smartShards = AttributeRegistry.values().stream()
                .filter(value -> data.level(value.id()) >= MAX_LEVEL && data.shardCount(value.id()) >= AttributeFusionService.required(value))
                .mapToInt(value -> data.shardCount(value.id())).sum();
        layout.slot(51, (s, c) -> ItemStacks.item(state.smart() ? Material.REINFORCED_DEEPSLATE : Material.DEEPSLATE, """
                <6>Smart Fusions
                <7>Smart fusions pick the shards you
                <c>don't need <7>to syphon anymore, and
                <7>automatically looks for <d><l>NEW <7>and
                <7>useful shards you might want to
                <7>syphon.

                <7>You have <9>%d <7>Shards available for
                <7>Smart Fusion!

                %s""".formatted(smartShards, smartShards == 0 ? "<c>Not enough Shards!" : state.smart() ? "<a>Enabled! <e>Click to disable!" : "<e>Click to enable!")),
                (click, c) -> c.session(State.class).setState(new State(1, null, null, !state.smart())));
        Components.close(layout, 49);
    }
}
