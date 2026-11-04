package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public class GUIGiftBagOfTheCentury implements View<GUIGiftBagOfTheCentury.State> {
    public static final String ITEM_ID = "GIFTBAG_OF_THE_CENTURY";
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};
    private static final int CLOSE_SLOT = 49;

    public record State(boolean claimed) {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(ITEM_ID, InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> player.openView(new GUIGiftBagOfTheCentury(), new State(false)))
                .build());
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Gift Bag of the Century", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        List<String> prizes = CenturyRaffleConfig.get().pool(PrizeTier.MINOR);
        for (int i = 0; i < prizes.size() && i < SLOTS.length; i++) {
            String prize = prizes.get(i);
            layout.slot(SLOTS[i], (_, _) -> display(prize), (c, view) -> {
                if (c.state().claimed()) return;
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
                if (held.getAttributeHandler().getPotentialType() != ItemType.get(ITEM_ID)) {
                    player.sendMessage("<c>Hold your Gift Bag of the Century to pick a reward!");
                    player.closeInventory();
                    return;
                }
                view.session(State.class).setState(new State(true));
                TimedConsumables.consumeHeld(player, held);
                SkyBlockItem reward = RaffleClaims.resolve(player.getUuid(), prize);
                player.addAndUpdateItem(reward);
                player.sendMessage("<e>You opened your <5>Gift Bag of the Century <e>and picked <a>" + reward.getDisplayName() + "<e>!");
                player.closeInventory();
            });
        }
        Components.close(layout, CLOSE_SLOT);
    }

    private static ItemStack.Builder display(String prize) {
        return switch (prize) {
            case CenturyRaffleConfig.RANDOM_CENTURY_CAKE -> ItemStacks.item(Material.CAKE, """
                    <a>Random Century Cake
                    <7>One of the <a>20 <7>Century Cakes.

                    <e>Click to pick!""");
            case CenturyRaffleConfig.RANDOM_CAKE_SOUL -> ItemStacks.item(Material.PINK_DYE, """
                    <d>Random Cake Soul
                    <7>A Cake Soul of a random colour.

                    <e>Click to pick!""");
            default -> {
                ItemType type = ItemType.get(prize);
                ItemStack.Builder builder = type == null ? ItemStacks.item(Material.BARRIER, "<c>" + prize)
                        : new NonPlayerItemUpdater(new SkyBlockItem(type)).getUpdatedItem();
                yield ItemStacks.appendLore(builder, List.of(Text.empty(), Text.of("<e>Click to pick!")));
            }
        };
    }
}
