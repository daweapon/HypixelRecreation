package dev.daweapon.type.skyblockgeneric.century;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class CenturyCakes {
    public static final String RANDOM_BUNDLE_ID = "RANDOM_CENTURY_CAKE_PACK";
    public static final String COMPLETE_BUNDLE_ID = "FULL_CENTURY_CAKE_PACK";
    public static final int RANDOM_BUNDLE_SIZE = 5;
    private static final int MENU_SLOT = 8;
    private static final int MAIN_INVENTORY_SIZE = 36;

    private CenturyCakes() {
    }

    public static List<ItemType> all() {
        return Arrays.stream(CenturyCake.values()).map(CenturyCake::itemType).toList();
    }

    public static ItemType random(@Nullable UUID player) {
        return randomCake(player).itemType();
    }

    public static SkyBlockItem randomCakeSoul(@Nullable UUID player) {
        List<CakeSoulColour> colours = List.of(CakeSoulColour.values());
        CakeSoulColour colour = LootRoll.pick(player, colours, _ -> null, _ -> 1D, ThreadLocalRandom.current());
        return CakeSoul.create(colour, null);
    }

    private static CenturyCake randomCake(@Nullable UUID player) {
        return LootRoll.pick(player, List.of(CenturyCake.values()), CenturyCake::itemType, _ -> 1D, ThreadLocalRandom.current());
    }

    public static void registerInteractables() {
        InteractableRegistry.register(RANDOM_BUNDLE_ID, InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> {
                    List<CenturyCake> cakes = new ArrayList<>();
                    for (int i = 0; i < RANDOM_BUNDLE_SIZE; i++) cakes.add(randomCake(player.getUuid()));
                    open(player, item, cakes, "<6>Random Century Cake Bundle");
                }).build());
        InteractableRegistry.register(COMPLETE_BUNDLE_ID, InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> open(player, item, List.of(CenturyCake.values()), "<d>Complete Century Cake Bundle"))
                .build());
    }

    private static void open(SkyBlockPlayer player, SkyBlockItem item, List<CenturyCake> cakes, String bundleName) {
        if (freeSlots(player) < cakes.size()) {
            player.sendMessage("<c>Oops! You need room for " + cakes.size() + " cakes in your inventory to open this!");
            return;
        }
        TimedConsumables.consumeHeld(player, item);
        player.sendMessage("<e>You opened a " + bundleName + "<e> and obtained:");
        for (CenturyCake cake : cakes) {
            player.addAndUpdateItem(cake.itemType());
            player.sendMessage("<a>" + cake.displayName());
        }
    }

    private static int freeSlots(SkyBlockPlayer player) {
        int free = 0;
        for (int slot = 0; slot < MAIN_INVENTORY_SIZE; slot++) {
            if (slot != MENU_SLOT && player.getInventory().getItemStack(slot).isAir()) free++;
        }
        return free;
    }
}
