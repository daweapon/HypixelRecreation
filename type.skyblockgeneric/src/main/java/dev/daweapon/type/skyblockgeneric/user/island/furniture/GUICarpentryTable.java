package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.SkullHeadComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Map;

public class GUICarpentryTable implements StatefulView<GUICarpentryTable.State> {
    private static final int[] RECIPE_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private static final int PREVIOUS_SLOT = 45;
    private static final int PICKUP_SLOT = 49;
    private static final int NEXT_SLOT = 53;
    private static final int INFO_SLOT = 4;

    private final IslandFurniture owner;
    private final PlacedFurniture table;

    public GUICarpentryTable(IslandFurniture owner, PlacedFurniture table) {
        this.owner = owner;
        this.table = table;
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Carpentry Table", InventoryType.CHEST_6_ROW);
    }

    @Override
    public State initialState() {
        return new State(0, 0);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int level = CarpentryCrafting.level(player);
        List<FurnitureDefinition> unlocked = CarpentryCrafting.unlocked(level);
        int pages = Math.max(1, (unlocked.size() + RECIPE_SLOTS.length - 1) / RECIPE_SLOTS.length);
        int page = Math.min(state.page(), pages - 1);

        Components.fill(layout);
        layout.slot(INFO_SLOT, (_, _) -> info(level, unlocked.size()));

        for (int i = 0; i < RECIPE_SLOTS.length; i++) {
            int index = page * RECIPE_SLOTS.length + i;
            if (index >= unlocked.size()) break;
            FurnitureDefinition definition = unlocked.get(index);
            layout.slot(RECIPE_SLOTS[i], (_, _) -> recipeIcon(player, definition),
                    (click, view) -> {
                        craft(player, definition, click.click());
                        view.session(State.class).setState(new State(click.state().page(), click.state().revision() + 1));
                    });
        }

        if (page > 0) layout.slot(PREVIOUS_SLOT, ItemStacks.item(Material.ARROW, "<a>Previous Page"), current -> new State(page - 1, current.revision()));
        if (page < pages - 1) layout.slot(NEXT_SLOT, ItemStacks.item(Material.ARROW, "<a>Next Page"), current -> new State(page + 1, current.revision()));

        layout.slot(PICKUP_SLOT, (_, _) -> ItemStacks.item(Material.BARRIER, """
                        <c>Pickup Furniture
                        <7>Put this Carpentry Table back in
                        <7>your inventory.

                        <e>Click to pick up!"""),
                (click, _) -> {
                    click.player().closeInventory();
                    FurnitureActions.pickUp(owner, player, table);
                });
        Components.close(layout, 48);
    }

    private static ItemStack.Builder info(int level, int unlockedCount) {
        FurnitureDefinition next = CarpentryCrafting.craftable().stream()
                .filter(definition -> definition.carpentryLevel() > level).findFirst().orElse(null);
        String nextLine = next == null ? "<7>You have unlocked every furniture recipe!"
                : "<7>Next unlock at <a>Carpentry %d<7>.".formatted(next.carpentryLevel());
        return ItemStacks.item(Material.CRAFTING_TABLE, """
                <a>Carpentry Table
                <7>Craft furniture for your island. Left-click
                <7>a recipe to craft one, shift-click to
                <7>craft as many as you can afford.

                <7>Carpentry Level: <a>%d
                <7>Recipes unlocked: <a>%d
                %s""".formatted(level, unlockedCount, nextLine));
    }

    private ItemStack.Builder recipeIcon(SkyBlockPlayer player, FurnitureDefinition definition) {
        StringBuilder lore = new StringBuilder("<8>Furniture\n\n<7>Requires:\n");
        for (Map.Entry<String, Integer> ingredient : definition.recipe().entrySet()) {
            ItemType type = ItemType.get(ingredient.getKey());
            int have = type == null ? 0 : player.getAmountInInventory(type);
            lore.append("<7>- ").append(have >= ingredient.getValue() ? "<a>" : "<c>").append(have).append("<7>/<a>")
                    .append(ingredient.getValue()).append(" <f>").append(StringUtility.toNormalCase(ingredient.getKey())).append('\n');
        }
        int affordable = CarpentryCrafting.affordable(player, definition);
        lore.append('\n').append(affordable > 0 ? "<e>Click to craft!" : "<c>You don't have the ingredients!");
        if (affordable > 1) {
            lore.append('\n').append(CarpentryCrafting.quickCraftingAllowed(player)
                    ? "<e>Shift-click to craft %d!".formatted(affordable)
                    : "<8>Quick Crafting needs Carpentry %d and a VIP rank.".formatted(CarpentryCrafting.QUICK_CRAFTING_LEVEL));
        }
        String name = "<a>" + StringUtility.toNormalCase(definition.itemId());
        SkyBlockItem item = new SkyBlockItem(definition.itemType());
        if (item.hasComponent(SkullHeadComponent.class)) {
            return ItemStacks.head(item.getComponent(SkullHeadComponent.class).getSkullTexture(item), name + "\n" + lore);
        }
        return ItemStacks.item(Material.OAK_PLANKS, name + "\n" + lore);
    }

    private void craft(SkyBlockPlayer player, FurnitureDefinition definition, Click click) {
        int affordable = CarpentryCrafting.affordable(player, definition);
        if (affordable < 1) {
            player.sendMessage("<c>You don't have the ingredients to craft this!");
            return;
        }
        boolean quick = click instanceof Click.LeftShift || click instanceof Click.RightShift;
        if (quick && !CarpentryCrafting.quickCraftingAllowed(player)) {
            player.sendMessage("<c>Quick Crafting needs Carpentry {} and a VIP rank or higher!", CarpentryCrafting.QUICK_CRAFTING_LEVEL);
            return;
        }
        int times = quick ? affordable : 1;
        if (CarpentryCrafting.craft(player, definition, times)) {
            player.sendMessage("<a>You crafted {}x <a>{}<a>!", times, StringUtility.toNormalCase(definition.itemId()));
        }
    }

    public record State(int page, int revision) {
    }
}
