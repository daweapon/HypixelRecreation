package dev.daweapon.type.skyblockgeneric.gui.inventories.hunting;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.*;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHunting;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeFusionService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.hunting.FusionMachine;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// GUI for fusing two hunting attribute shards together
public class GUIAttributeFusion implements StatefulView<GUIAttributeFusion.State> {
    private static final int MAX_LEVEL = 10;

    private static final int[] FIRST_PICK_SLOTS = AttributeGUIItems.CONTENT_SLOTS;
    private static final int[] SECOND_PICK_SLOTS = {28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private static final int[] RESULT_SLOTS = {29, 31, 33};

    public record State(int page, String first, String second, boolean smart, GUIHuntingBox.Sort sort, String query) {
        State withPage(int page) {
            return new State(page, first, second, smart, sort, query);
        }

        State withSelection(String first, String second) {
            return new State(1, first, second, smart, sort, query);
        }
    }

    @Override
    public State initialState() {
        return new State(1, null, null, false, GUIHuntingBox.Sort.ID_ASCENDING, "");
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> state.second() != null ? "Confirm Fusion"
                : state.first() != null ? "Shard Fusion" : "Fusion Box", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onClose(State state, ViewContext ctx, ViewSession.CloseReason reason) {
        FusionMachine.clearTubes((SkyBlockPlayer) ctx.player());
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(Layouts.border(0, 53));
        layout.filler(Layouts.rectangle(10, 43));
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointHunting.HuntingData data = player.getHuntingData();
        AttributeDefinition first = AttributeRegistry.get(state.first());
        AttributeDefinition second = AttributeRegistry.get(state.second());
        FusionMachine.showTubes(player, first, second);

        if (second != null) confirmLayout(layout, state, player, first, second);
        else {
            pickerLayout(layout, state, data, first);
            layout.slot(46, (s, c) -> ItemStacks.item(Material.OAK_SIGN, """
                    <a>Search
                    <7>Search for specific shards in your
                    <7>Fusion Box. You can enter the Shard
                    <7>Name, ID, Family, or Attribute
                    <7>Category!

                    <e>Click to search!"""), (click, c) -> new HypixelSignGUI(c.player())
                    .open(new String[]{"Enter query", state.query()}).thenAccept(q -> {
                        if (q != null) c.session(State.class).setState(new State(1, state.first(), null, state.smart(), state.sort(), q));
                    }));
            layout.slot(50, (s, c) -> GUIHuntingBox.sortItem(state.sort()), (click, c) -> {
                GUIHuntingBox.Sort[] sorts = GUIHuntingBox.Sort.values();
                int direction = click.click() instanceof Click.Right || click.click() instanceof Click.RightShift ? -1 : 1;
                GUIHuntingBox.Sort next = sorts[Math.floorMod(state.sort().ordinal() + direction, sorts.length)];
                c.session(State.class).setState(new State(1, state.first(), null, state.smart(), next, state.query()));
            });
        }
        Components.close(layout, 49);
    }

    private void pickerLayout(ViewLayout<State> layout, State state, DatapointHunting.HuntingData data, AttributeDefinition first) {
        List<AttributeDefinition> available = new ArrayList<>(AttributeRegistry.values().stream()
                .filter(value -> data.shardCount(value.id()) > 0)
                .filter(value -> !state.smart() || data.level(value.id()) >= MAX_LEVEL)
                .filter(value -> state.query().isBlank() || AttributeRegistry.search(state.query()).contains(value)).toList());
        GUIHuntingBox.sortShards(available, state.sort(), data);
        int[] slots = first == null ? FIRST_PICK_SLOTS : SECOND_PICK_SLOTS;
        int start = (state.page() - 1) * slots.length;
        for (int i = 0; i < slots.length; i++) {
            int index = start + i;
            if (index >= available.size()) break;
            AttributeDefinition definition = available.get(index);
            int needed = needed(definition, first);
            boolean enough = data.shardCount(definition.id()) >= needed;
            layout.slot(slots[i],
                    (s, c) -> AttributeGUIItems.fusionShard(definition, 1, "<7>Fusion cost: <a>" + needed, "<7>Owned: <b>" + data.shardCount(definition.id()),
                            enough ? "<e>Click to select!" : "<c>Not enough Shards!"),
                    (click, c) -> {
                        if (!enough) {
                            c.player().sendMessage("<c>You need <e>{} <c>Shards to fuse this one!", needed);
                            return;
                        }
                        c.session(State.class).setState(first == null
                                ? state.withSelection(definition.id().toString(), null) : state.withSelection(first.id().toString(), definition.id().toString()));
                    });
        }
        int pages = Math.max(1, (available.size() + slots.length - 1) / slots.length);
        if (state.page() > 1)
            layout.slot(45, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Previous Page"), (click, c) -> c.session(State.class).setState(state.withPage(state.page() - 1)));
        if (state.page() < pages)
            layout.slot(53, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Next Page"), (click, c) -> c.session(State.class).setState(state.withPage(state.page() + 1)));

        if (first == null) {
            layout.slot(4, (s, c) -> AttributeGUIItems.fusionBoxHead(data.getShards().values().stream().mapToInt(Integer::intValue).sum()));
            layout.slot(51, (s, c) -> smartItem(state, data), (click, c) -> c.session(State.class).setState(
                    new State(1, null, null, !state.smart(), state.sort(), state.query())));
            layout.slot(52, (s, c) -> massSyphonItem(data), (click, c) -> GUIHuntingBox.confirmMassSyphon((SkyBlockPlayer) c.player(), p -> p.openView(new GUIAttributeFusion())));
        } else {
            layout.slot(12, (s, c) -> AttributeGUIItems.fusionShard(first, AttributeFusionService.required(first), "<7>Fusion cost: <a>" + AttributeFusionService.required(first)));
            layout.slot(13, (s, c) -> machineItem());
            layout.slot(48, (s, c) -> ItemStacks.item(Material.STICK, "<c>Reset"), (click, c) -> c.session(State.class).setState(state.withSelection(null, null)));
        }
    }

    private static int needed(AttributeDefinition definition, AttributeDefinition first) {
        return first != null && first.id().equals(definition.id()) ? AttributeFusionService.required(definition) * 2 : AttributeFusionService.required(definition);
    }

    private void confirmLayout(ViewLayout<State> layout, State state, SkyBlockPlayer player, AttributeDefinition first, AttributeDefinition second) {
        DatapointHunting.HuntingData data = player.getHuntingData();
        int firstCost = AttributeFusionService.required(first);
        int secondCost = AttributeFusionService.required(second);
        layout.slot(12, (s, c) -> AttributeGUIItems.fusionShard(first, firstCost, "<7>Fusion cost: <a>" + firstCost));
        layout.slot(13, (s, c) -> machineItem());
        layout.slot(14, (s, c) -> AttributeGUIItems.fusionShard(second, secondCost, "<7>Fusion cost: <a>" + secondCost));
        layout.slot(48, (s, c) -> ItemStacks.item(Material.STICK, "<c>Reset"), (click, c) -> c.session(State.class).setState(state.withSelection(null, null)));
        List<AttributeFusionService.FusionResult> results = AttributeFusionService.results(first, second);
        for (int i = 0; i < results.size() && i < RESULT_SLOTS.length; i++) {
            AttributeFusionService.FusionResult result = results.get(i);
            layout.slot(RESULT_SLOTS[i], (s, c) -> AttributeGUIItems.fusionShard(result.definition(), result.amount(),
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
                            c.session(State.class).setState(state.withSelection(null, null));
                        } else c.player().sendMessage("<c>You no longer have enough Shards for that fusion!");
                    });
        }
    }

    private static ItemStack.Builder machineItem() {
        return ItemStacks.item(Material.POLISHED_BLACKSTONE, """
                <d>Fusion Machine
                <7>Combines your Shards into new
                <7>Shards.""");
    }

    private static ItemStack.Builder smartItem(State state, DatapointHunting.HuntingData data) {
        int smartShards = AttributeRegistry.values().stream()
                .filter(value -> data.level(value.id()) >= MAX_LEVEL && data.shardCount(value.id()) >= AttributeFusionService.required(value))
                .mapToInt(value -> data.shardCount(value.id())).sum();
        return ItemStacks.item(state.smart() ? Material.REINFORCED_DEEPSLATE : Material.DEEPSLATE, """
                <6>Smart Fusions
                <7>Smart fusions pick the shards you
                <c>don't need <7>to syphon anymore, and
                <7>automatically looks for <d><l>NEW <7>and
                <7>useful shards you might want to
                <7>syphon.

                <7>You have <9>%d <7>Shards available for
                <7>Smart Fusion!

                %s""".formatted(smartShards, smartShards == 0 ? "<c>Not enough Shards!" : state.smart() ? "<a>Enabled! <e>Click to disable!" : "<e>Click to enable!"));
    }

    private static ItemStack.Builder massSyphonItem(DatapointHunting.HuntingData data) {
        List<AttributeDefinition> usable = GUIHuntingBox.syphonable(data);
        int count = usable.stream().mapToInt(d -> data.shardCount(d.id())).sum();
        return ItemStacks.item(Material.GOLDEN_APPLE, """
                <6>Mass Syphon
                <7>You have <c>%d Shards <7>across <a>%d
                <7>different Attributes that are not
                <7>maxed out yet.

                %s""".formatted(count, usable.size(), usable.isEmpty() ? "<c>Nothing to Syphon!" : "<e>Click to mass Syphon!"));
    }
}
