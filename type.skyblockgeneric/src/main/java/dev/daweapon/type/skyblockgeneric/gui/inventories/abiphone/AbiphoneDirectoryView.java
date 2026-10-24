package dev.daweapon.type.skyblockgeneric.gui.inventories.abiphone;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.PaginatedView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContact;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactCatalogue;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactDirectory;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactXP;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import dev.daweapon.type.skyblockgeneric.abiphone.ChatAnswers;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AbiphoneDirectoryView extends PaginatedView<AbiphoneContact, AbiphoneDirectoryView.State> {

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText((state, ctx) -> Text.literal("Contacts Directory"), InventoryType.CHEST_6_ROW);
    }

    @Override
    protected int[] getPaginatedSlots() {
        return DEFAULT_SLOTS;
    }

    @Override
    protected ItemStack.Builder renderItem(AbiphoneContact contact, int index, HypixelPlayer player) {
        AbiphoneNPC npc = AbiphoneRegistry.getFromId(contact.id());
        ItemStack.Builder icon = npc != null ? npc.getIcon() : ItemStack.builder(Material.PLAYER_HEAD);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>{}", contact.description()));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Difficulty: {}", contact.difficulty().markup()));
        lore.add(Text.of("<7>Usual location: <b>{}", contact.location()));
        if (!contact.requirement().isBlank()) {
            lore.add(Text.empty());
            lore.add(Text.of("<7>Requirement: <f>{}", contact.requirement()));
        }
        lore.add(Text.empty());
        lore.add(status(contact, npc, (SkyBlockPlayer) player));
        return ItemStacks.lore(ItemStacks.name(icon, "<f>{}", contact.name()), lore);
    }

    private Text status(AbiphoneContact contact, AbiphoneNPC npc, SkyBlockPlayer player) {
        if (npc == null) return Text.of("<c>Not available yet!");
        if (AbiphoneContactXP.unlocked(player).contains(contact.id())) return Text.of("<a>✔ Contact unlocked");
        return Text.of("<e>Not unlocked yet");
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, AbiphoneContact contact, int index) {
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, AbiphoneContact contact) {
        if (!contact.name().toLowerCase().contains(state.query().toLowerCase())) return true;
        return switch (state.filter()) {
            case ALL -> false;
            case NOT_ADDED -> onPhone(state, contact);
            case COMPLETED_NOT_ADDED -> onPhone(state, contact) || !state.unlocked().contains(contact.id());
        };
    }

    private static boolean onPhone(State state, AbiphoneContact contact) {
        return state.abiphone().getAttributeHandler().getAbiphoneNPCs().stream().anyMatch(npc -> npc.getId().equals(contact.id()));
    }

    @Override
    protected List<AbiphoneContact> getFilteredItems(State state) {
        Comparator<AbiphoneContact> byName = Comparator.comparing(contact -> contact.name().toLowerCase());
        Comparator<AbiphoneContact> order = switch (state.sort()) {
            case A_TO_Z -> byName;
            case Z_TO_A -> byName.reversed();
            case DIFFICULTY -> Comparator.comparing(AbiphoneContact::difficulty).thenComparing(byName);
            case USUAL_LOCATION -> Comparator.comparing((AbiphoneContact contact) -> contact.location().toLowerCase()).thenComparing(byName);
        };
        return super.getFilteredItems(state).stream().sorted(order).toList();
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (!Components.back(layout, 48, ctx)) Components.close(layout, 48);
        Components.close(layout, 49);

        layout.slot(50, (s, c) -> cycleButton(Material.HOPPER, "Sort", s.sort().label, Sort.values(), s.sort()),
                (click, viewCtx) -> viewCtx.session(Object.class).updateUnchecked(o -> {
                    State typed = (State) o;
                    return typed.withSort(next(Sort.values(), typed.sort(), click.click()));
                }));

        layout.slot(51, (s, c) -> cycleButton(Material.ENDER_EYE, "Filter", s.filter().label, Filter.values(), s.filter()),
                (click, viewCtx) -> viewCtx.session(Object.class).updateUnchecked(o -> {
                    State typed = (State) o;
                    return typed.withFilter(next(Filter.values(), typed.filter(), click.click()));
                }));

        layout.slot(52, (s, c) -> ItemStacks.lore(ItemStacks.name(ItemStack.builder(Material.OAK_SIGN), "<a>Search"), List.of(
                Text.of("<7>Find a contact by name."),
                Text.empty(),
                Text.of("<7>Current: <b>{}", s.query().isEmpty() ? "None" : s.query()),
                Text.empty(),
                Text.of("<e>Click to search!"),
                Text.of("<b>Right-click to clear!"))),
                (click, viewCtx) -> {
                    if (click.click() instanceof Click.Right) {
                        viewCtx.session(Object.class).updateUnchecked(o -> ((State) o).withQuery(""));
                        return;
                    }
                    viewCtx.player().closeInventory();
                    viewCtx.player().sendMessage("<e>Type the contact's name in chat to search.");
                    State current = click.state();
                    ChatAnswers.expectText(player, query -> MinecraftServer.getSchedulerManager().scheduleNextTick(
                            () -> player.openView(new AbiphoneDirectoryView(), current.withQuery(query).withPage(0))));
                });

        layout.slot(47, (s, c) -> ItemStacks.lore(ItemStacks.name(ItemStack.builder(Material.BOOK), "<a>Your Contacts"), List.of(
                Text.of("<7>Contacts unlocked: <a>{}<b>/{}", AbiphoneContactDirectory.discovered(player), AbiphoneContactDirectory.TOTAL),
                Text.of("<7>Available now: <a>{}<b>/{}",
                        AbiphoneContactCatalogue.all().stream().filter(AbiphoneContact::available).count(), AbiphoneContactDirectory.TOTAL))));
    }

    private static <E extends Enum<E>> ItemStack.Builder cycleButton(Material material, String name, String current, E[] values, E selected) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        for (E value : values) lore.add(Text.of(value == selected ? "<b>▶ {}" : "<7>  {}", label(value)));
        lore.add(Text.empty());
        lore.add(Text.of("<b>Right-click to go backwards!"));
        lore.add(Text.of("<e>Click to switch!"));
        return ItemStacks.lore(ItemStacks.name(ItemStack.builder(material), "<a>{}", name), lore);
    }

    private static String label(Enum<?> value) {
        return value instanceof Sort sort ? sort.label : ((Filter) value).label;
    }

    private static <E extends Enum<E>> E next(E[] values, E current, Click click) {
        int step = click instanceof Click.Right ? values.length - 1 : 1;
        return values[(current.ordinal() + step) % values.length];
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    public enum Sort {
        A_TO_Z("A to Z"),
        Z_TO_A("Z to A"),
        DIFFICULTY("Difficulty"),
        USUAL_LOCATION("Usual Location");

        public final String label;

        Sort(String label) {
            this.label = label;
        }
    }

    public enum Filter {
        ALL("All Contacts"),
        NOT_ADDED("Not Added"),
        COMPLETED_NOT_ADDED("Completed Quest but Not Added");

        public final String label;

        Filter(String label) {
            this.label = label;
        }
    }

    public record State(SkyBlockItem abiphone, List<AbiphoneContact> items, int page, String query, Sort sort, Filter filter,
                        List<String> unlocked) implements PaginatedState<AbiphoneContact> {

        public State(SkyBlockItem abiphone, SkyBlockPlayer player) {
            this(abiphone, AbiphoneContactCatalogue.all(), 0, "", Sort.A_TO_Z, Filter.ALL, List.copyOf(AbiphoneContactXP.unlocked(player)));
        }

        @Override
        public State withPage(int page) {
            return new State(abiphone, items, page, query, sort, filter, unlocked);
        }

        @Override
        public State withItems(List<AbiphoneContact> items) {
            return new State(abiphone, items, page, query, sort, filter, unlocked);
        }

        public State withQuery(String query) {
            return new State(abiphone, items, 0, query, sort, filter, unlocked);
        }

        public State withSort(Sort sort) {
            return new State(abiphone, items, 0, query, sort, filter, unlocked);
        }

        public State withFilter(Filter filter) {
            return new State(abiphone, items, 0, query, sort, filter, unlocked);
        }
    }
}
