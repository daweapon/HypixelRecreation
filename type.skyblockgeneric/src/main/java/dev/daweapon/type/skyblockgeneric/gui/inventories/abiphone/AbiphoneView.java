package dev.daweapon.type.skyblockgeneric.gui.inventories.abiphone;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.*;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactDirectory;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneItemSettings;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// GUI listing an Abiphone's saved NPC contacts, letting the player call or manage each one
public final class AbiphoneView extends PaginatedView<AbiphoneNPC, AbiphoneView.State> {

    private static final int[] PAGINATED_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText(
                (state, ctx) -> Text.literal(state.abiphone().getCleanName()),
                InventoryType.CHEST_6_ROW
        );
    }

    @Override
    protected int[] getPaginatedSlots() {
        return PAGINATED_SLOTS;
    }

    @Override
    protected ItemStack.Builder renderItem(AbiphoneNPC npc, int index, HypixelPlayer player) {
        return ItemStacks.lore(
                ItemStacks.name(npc.getIcon(), "<f>{}", npc.getName()),
                List.of(
                        Text.of("<7>{}", npc.getDescription()),
                        Text.empty(),
                        Text.key("gui_abiphone.contact_manage_hint"),
                        Text.key("gui_abiphone.contact_call_hint")
                )
        );
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, AbiphoneNPC npc, int index) {
        if (click.click() instanceof Click.Left) {
            ctx.player().closeInventory();
            AbiphoneItemSettings.recordCall(click.state().abiphone(), npc);
            initiateCall(ctx.player(), npc);
        } else if (click.click() instanceof Click.Right) {
            ctx.push(new GUIContactManagementView(), new GUIContactManagementView.State(click.state().abiphone(), npc));
        }
    }

    private void initiateCall(HypixelPlayer player, AbiphoneNPC npc) {
        player.sendMessage(Text.key("gui_abiphone.ring_1"));
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            player.sendMessage(Text.key("gui_abiphone.ring_2"));
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                player.sendMessage(Text.key("gui_abiphone.ring_3"));
                MinecraftServer.getSchedulerManager().buildTask(() -> {
                    npc.onCall(player);
                }).delay(TaskSchedule.seconds(1)).schedule();
            }).delay(TaskSchedule.seconds(1)).schedule();
        }).delay(TaskSchedule.seconds(1)).schedule();
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, AbiphoneNPC item) {
        return !item.getName().plain().toLowerCase().contains(state.query.toLowerCase());
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.close(layout, 49);
        layout.slot(50, (s, c) -> sortButton(s.sortType()), (click, viewCtx) -> {
            SortType[] types = SortType.values();
            int step = click.click() instanceof Click.Right ? types.length - 1 : 1;
            viewCtx.session(Object.class).updateUnchecked(s -> {
                State typed = (State) s;
                return typed.withSortType(types[(typed.sortType().ordinal() + step) % types.length]);
            });
        });

        layout.slot(51, (s, c) -> ItemStacks.item(
                Material.BOOK,
                1,
                Text.key("gui_abiphone.contacts_directory"),
                Text.keyLines("gui_abiphone.contacts_directory.lore",
                        AbiphoneContactDirectory.discovered((SkyBlockPlayer) c.player()),
                        AbiphoneContactDirectory.TOTAL)
        ), (click, viewCtx) -> viewCtx.push(new AbiphoneDirectoryView(), new AbiphoneDirectoryView.State(click.state().abiphone(), (SkyBlockPlayer) viewCtx.player())));
    }

    private ItemStack.Builder sortButton(SortType current) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        for (SortType type : SortType.values()) {
            lore.add(Text.of(type == current ? "<b>▶ {}" : "<7>  {}", type.label));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<b>Right-click to go backwards!"));
        lore.add(Text.of("<e>Click to switch!"));
        return ItemStacks.lore(ItemStacks.name(ItemStack.builder(Material.HOPPER), "<a>Sort"), lore);
    }

    @Override
    protected List<AbiphoneNPC> getFilteredItems(State state) {
        List<AbiphoneNPC> items = super.getFilteredItems(state);
        return AbiphoneItemSettings.sorted(items, state.sortType(), state.abiphone());
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    public record State(
            SkyBlockItem abiphone,
            List<AbiphoneNPC> items,
            int page,
            String query,
            SortType sortType
    ) implements PaginatedState<AbiphoneNPC> {

        public State(SkyBlockItem abiphone) {
            this(abiphone, abiphone.getAttributeHandler().getAbiphoneNPCs(), 0, "", SortType.FIRST_ADDED);
        }

        @Override
        public PaginatedState<AbiphoneNPC> withPage(int page) {
            return new State(abiphone, items, page, query, sortType);
        }

        @Override
        public PaginatedState<AbiphoneNPC> withItems(List<AbiphoneNPC> items) {
            return new State(abiphone, items, page, query, sortType);
        }

        public State withSortType(SortType sortType) {
            return new State(abiphone, items, page, query, sortType);
        }
    }

    public enum SortType {
        FIRST_ADDED("First Added"),
        ALPHABETICAL("Alphabetical"),
        LAST_CALLED("Last Called"),
        MOST_CALLED("Most Called"),
        DO_NOT_DISTURB_FIRST("Do Not Disturb First");

        public final String label;

        SortType(String label) {
            this.label = label;
        }
    }

}

