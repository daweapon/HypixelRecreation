package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.sources;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// The sub-tasks making up a composite XP task, e.g. the seven dragons behind "Slay Dragons".
public class GUIXPSubTasks implements StatefulView<GUIXPSubTasks.State> {

    public record State(SkyBlockXPSource source) {
    }

    @Override
    public State initialState() {
        return new State(SkyBlockXPSource.SLAY_DRAGONS);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText(
                (state, ctx) -> Text.literal(state.source().getGroup().getShortName()
                        + " ➜ " + state.source().getDisplayName()),
                InventoryType.CHEST_6_ROW
        );
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockXPSource source = state.source();
        layout.filler(ItemStacks.filler(source.getGroup().getFiller()));
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> headerItem(source, (SkyBlockPlayer) c.player()));

        List<String> subTasks = source.getSubTasks();
        int[] slots = XPTaskLayout.slots(subTasks.size(), 1, 4);
        for (int i = 0; i < slots.length; i++) {
            String subTask = subTasks.get(i);
            layout.slot(slots[i], (s, c) -> subTaskItem(subTask, source));
        }
    }

    private ItemStack.Builder headerItem(SkyBlockXPSource source, SkyBlockPlayer player) {
        double earned = player.getSkyBlockExperience().getSourceXP(source);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>{} Tasks", source.getSubTasks().size()));
        lore.add(Text.empty());
        source.getTaskDescription().forEach(line -> lore.add(Text.of("<7>" + line)));
        lore.add(Text.empty());
        lore.add(Text.of("<f>Total Progress: <b>{}",
                GUIXPSources.percentage(earned, source.getMaxXP())));
        lore.add(GUIXPSources.detailBar(earned, source.getMaxXP()));

        return GUIXPSources.taskIcon(source, Text.of("<a>{}", source.getDisplayName()), lore);
    }

    private ItemStack.Builder subTaskItem(String subTask, SkyBlockXPSource source) {
        List<Text> lore = List.of(
                Text.of("<c>✖ Not completed"),
                Text.empty(),
                Text.of("<8>{}", source.getAvailability().getDisplayName())
        );

        return ItemStacks.item(Material.GRAY_DYE, 1, Text.of("<7>{}", subTask), lore);
    }
}
