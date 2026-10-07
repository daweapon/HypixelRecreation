package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.PaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointPetData;
import net.swofty.type.skyblockgeneric.data.datapoints.PetVisibility;
import net.swofty.type.skyblockgeneric.entity.PetEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetScore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Paginated GUI listing the player's owned pets, laid out like Hypixel's Pets menu
public class GUIPets extends PaginatedView<SkyBlockItem, GUIPets.PetsState> {
    private static final int[] PAGINATED_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    @Override
    public ViewConfiguration<PetsState> configuration() {
        return ViewConfiguration.withText(
            (state, ctx) -> Text.key("gui_sbmenu.pets.title", state.page() + 1,
                Math.max(1, (int) Math.ceil((double) getFilteredItems(state).size() / PAGINATED_SLOTS.length))),
                InventoryType.CHEST_6_ROW
        );
    }

    @Override
    protected int[] getPaginatedSlots() {
        return PAGINATED_SLOTS;
    }

    @Override
    protected List<SkyBlockItem> getFilteredItems(PetsState state) {
        List<SkyBlockItem> pets = new ArrayList<>(state.items().stream()
                .filter(item -> !shouldFilterFromSearch(state, item))
                .toList());
        pets.sort(Comparator.comparing((SkyBlockItem pet) -> !state.favorites().contains(pet))
                .thenComparing(state.sortType().comparator));
        return pets;
    }

    @Override
    protected ItemStack.Builder renderItem(SkyBlockItem item, int index, HypixelPlayer player) {
        DatapointPetData.UserPetData petData = ((SkyBlockPlayer) player).getPetData();
        boolean active = petData.getEnabledPet() == item;

        ItemStack.Builder itemStack = new NonPlayerItemUpdater(item).getUpdatedItem();
        List<Text> lore = ItemStacks.loreText(itemStack);
        lore.add(Text.literal(" "));
        if (petData.isFavorite(item)) lore.add(Text.key("gui_sbmenu.pets.favorite"));
        int shareSlot = petData.getExpShareSlot(item);
        if (shareSlot >= 0) lore.add(Text.key("gui_sbmenu.pets.in_exp_share", shareSlot + 1));
        if (active) {
            ItemStacks.enchanted(itemStack);
            lore.add(Text.key("gui_sbmenu.pets.click_to_despawn"));
        } else {
            lore.add(Text.key("gui_sbmenu.pets.click_to_summon"));
        }
        lore.add(Text.key("gui_sbmenu.pets.click_to_favorite"));
        lore.add(Text.key("gui_sbmenu.pets.click_to_convert"));
        return ItemStacks.lore(itemStack, lore);
    }

    @Override
    protected void onItemClick(ClickContext<PetsState> click, ViewContext ctx, SkyBlockItem item, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointPetData.UserPetData petData = player.getPetData();

        switch (click.click()) {
            case Click.LeftShift _ -> {
                boolean favorite = petData.toggleFavorite(item);
                player.sendMessage(Text.key(favorite ? "gui_sbmenu.pets.msg.favorited" : "gui_sbmenu.pets.msg.unfavorited",
                        item.getDisplayName()));
            }
            case Click.Right _ -> convertToItem(player, item);
            default -> toggleSummon(player, item);
        }
        ctx.session(PetsState.class).update(s -> s.refreshedFrom(player));
    }

    private void toggleSummon(SkyBlockPlayer player, SkyBlockItem item) {
        DatapointPetData.UserPetData petData = player.getPetData();
        if (petData.getEnabledPet() == item) {
            petData.deselectCurrent();
            petData.updatePetEntityImpl(player);
            player.sendMessage(Text.key("gui_sbmenu.pets.msg.deselected", item.getDisplayName()));
            return;
        }

        petData.setEnabled(item, true);
        petData.updatePetEntityImpl(player);
        player.sendMessage(Text.key("gui_sbmenu.pets.msg.selected", item.getDisplayName()));
    }

    private void convertToItem(SkyBlockPlayer player, SkyBlockItem item) {
        if (!player.canFitItem(PlayerItemUpdater.playerUpdate(player, item.getItemStack(), true).build())) {
            player.sendMessage(Text.key("gui_sbmenu.pets.msg.inventory_full"));
            return;
        }

        player.getPetData().removePet(item);
        player.addAndUpdateItem(item);
        player.sendMessage(Text.key("gui_sbmenu.pets.msg.picked_up", item.getDisplayName()));
    }

    @Override
    protected boolean shouldFilterFromSearch(PetsState state, SkyBlockItem item) {
        return !StringUtility.stripColor(item.getDisplayName()).toLowerCase().contains(state.query().toLowerCase());
    }

    @Override
    protected void layoutCustom(ViewLayout<PetsState> layout, PetsState state, ViewContext ctx) {
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> {
            SkyBlockItem enabled = ((SkyBlockPlayer) c.player()).getPetData().getEnabledPet();
            Text selectedPet = enabled == null ? Text.of("<c>None") : Text.literal(enabled.getDisplayName());
            return ItemStacks.item(Material.BONE, 1,
                Text.key("gui_sbmenu.pets.info"),
                Text.keyLines("gui_sbmenu.pets.info.lore", selectedPet));
        });

        layout.slot(7, (s, c) -> ItemStacks.item(Material.YELLOW_DYE, 1,
                        Text.key("gui_sbmenu.pets.exp_sharing"),
                        Text.keyLines("gui_sbmenu.pets.exp_sharing.lore")),
                (click, c) -> c.push(new GUIPetExpSharing()));

        layout.slot(47, (s, c) -> ItemStacks.item(Material.DIAMOND, 1,
                Text.key("gui_sbmenu.pets.score"), petScoreLore((SkyBlockPlayer) c.player())));

        layout.slot(50, (s, c) -> ItemStacks.item(Material.OAK_SIGN, 1,
                        Text.key("gui_sbmenu.pets.search"),
                        Text.keyLines("gui_sbmenu.pets.search.lore")),
                (click, c) -> new HypixelSignGUI(c.player()).open(new String[]{"Enter pet name", ""})
                        .thenAccept(line -> {
                            PetsState current = click.state();
                            c.replace(new GUIPets(), current.withQuery(line == null ? "" : line.trim()));
                        }));

        layout.slot(51, (s, c) -> visibilityButton((SkyBlockPlayer) c.player()), (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            DatapointPetData.UserPetData petData = player.getPetData();
            if (click.click() instanceof Click.Right) {
                petData.setNameTags(!petData.isNameTags());
            } else {
                petData.setVisibility(petData.getVisibility().next(false));
            }
            PetEntityImpl.refreshFor(player);
            c.session(PetsState.class).refresh();
        });

        layout.slot(52, (s, c) -> {
            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(" "));
            for (SortType sortType : SortType.values()) {
                lore.add(Text.of(sortType == s.sortType() ? "<3>▶ {}" : "<7>  {}", sortType.label));
            }
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_sbmenu.pets.sort.right_click"));
            lore.add(Text.key("gui_sbmenu.pets.sort.click"));
            return ItemStacks.item(Material.HOPPER, 1, Text.key("gui_sbmenu.pets.sort"), lore);
        }, (click, c) -> {
            boolean backwards = click.click() instanceof Click.Right;
            c.session(PetsState.class).update(s -> s.withSortType(s.sortType().next(backwards)));
        });
    }

    private static List<Text> petScoreLore(SkyBlockPlayer player) {
        int score = PetScore.of(player);
        List<Text> lore = new ArrayList<>(Text.keyLines("gui_sbmenu.pets.score.lore"));
        boolean marked = false;
        for (int i = 0; i < PetScore.MAGIC_FIND_THRESHOLDS.size(); i++) {
            int threshold = PetScore.MAGIC_FIND_THRESHOLDS.get(i);
            boolean next = !marked && score < threshold;
            marked |= next;
            lore.add(Text.of("<6>{} Score<7>: <8>+<b>{} Magic Find{}", threshold, i + 1, next ? " <5>«" : ""));
        }
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_sbmenu.pets.score.yours", score));
        return lore;
    }

    private static ItemStack.Builder visibilityButton(SkyBlockPlayer player) {
        DatapointPetData.UserPetData petData = player.getPetData();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(" "));
        for (PetVisibility visibility : PetVisibility.values()) {
            lore.add(Text.of(visibility == petData.getVisibility() ? "<3>▶ {}" : "<7>  {}", visibility.getLabel()));
        }
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_sbmenu.pets.visibility.name_tags",
                petData.isNameTags() ? Text.of("<a>Enabled") : Text.of("<c>Disabled")));
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_sbmenu.pets.visibility.right_click"));
        lore.add(Text.key("gui_sbmenu.pets.visibility.click"));
        return ItemStacks.item(Material.STONE_BUTTON, 1, Text.key("gui_sbmenu.pets.visibility"), lore);
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    public static PetsState createInitialState(SkyBlockPlayer player) {
        return new PetsState(List.of(), List.of(), 0, "", SortType.RARITY).refreshedFrom(player);
    }

    public record PetsState(
            List<SkyBlockItem> items,
            List<SkyBlockItem> favorites,
            int page,
            String query,
            SortType sortType
    ) implements PaginatedState<SkyBlockItem> {
        @Override
        public PaginatedState<SkyBlockItem> withPage(int page) {
            return new PetsState(items, favorites, page, query, sortType);
        }

        @Override
        public PaginatedState<SkyBlockItem> withItems(List<SkyBlockItem> items) {
            return new PetsState(items, favorites, page, query, sortType);
        }

        public PetsState withSortType(SortType sortType) {
            return new PetsState(items, favorites, page, query, sortType);
        }

        public PetsState withQuery(String query) {
            return new PetsState(items, favorites, 0, query, sortType);
        }

        public PetsState refreshedFrom(SkyBlockPlayer player) {
            DatapointPetData.UserPetData petData = player.getPetData();
            List<SkyBlockItem> pets = List.copyOf(petData.getPetsMap().keySet());
            return new PetsState(pets, pets.stream().filter(petData::isFavorite).toList(), page, query, sortType);
        }
    }

    public enum SortType {
        RARITY("Rarity", Comparator.comparing((SkyBlockItem pet) -> pet.getAttributeHandler().getRarity()).reversed()
                .thenComparing(GUIPets::petName)),
        ALPHABETICAL("Alphabetical", Comparator.comparing(GUIPets::petName)),
        PET_EXP("Pet Exp", Comparator.comparingDouble((SkyBlockItem pet) ->
                pet.getAttributeHandler().getPetData().getExperience()).reversed()),
        SKILL("Skill", Comparator.comparing((SkyBlockItem pet) -> pet.getComponent(PetComponent.class).getSkillCategory())
                .thenComparing(GUIPets::petName));

        private final String label;
        private final Comparator<SkyBlockItem> comparator;

        SortType(String label, Comparator<SkyBlockItem> comparator) {
            this.label = label;
            this.comparator = comparator;
        }

        public SortType next(boolean backwards) {
            SortType[] values = values();
            return values[Math.floorMod(ordinal() + (backwards ? -1 : 1), values.length)];
        }
    }

    private static String petName(SkyBlockItem pet) {
        return pet.getComponent(PetComponent.class).getPetName();
    }
}
