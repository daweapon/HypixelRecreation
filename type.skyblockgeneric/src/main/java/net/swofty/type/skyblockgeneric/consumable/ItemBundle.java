package net.swofty.type.skyblockgeneric.consumable;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum ItemBundle {
    ECCENTRIC_PAINTING_BUNDLE("ECCENTRIC_PAINTING", null, 0, 9),
    ENCHANTED_BOOK_BUNDLE_BIG_BRAIN(null, EnchantmentType.BIG_BRAIN, 3, 5),
    ENCHANTED_BOOK_BUNDLE_CHIMERA(null, EnchantmentType.CHIMERA, 5, 1),
    ENCHANTED_BOOK_BUNDLE_ICE_COLD(null, EnchantmentType.ICE_COLD, 5, 4),
    ENCHANTED_BOOK_BUNDLE_POWER(null, EnchantmentType.POWER, 7, 1),
    ENCHANTED_BOOK_BUNDLE_PRISMATIC(null, EnchantmentType.PRISMATIC, 5, 1),
    ENCHANTED_BOOK_BUNDLE_QUANTUM(null, EnchantmentType.QUANTUM, 3, 1),
    ENCHANTED_BOOK_BUNDLE_RAINBOW(null, EnchantmentType.RAINBOW, 3, 1),
    ENCHANTED_BOOK_BUNDLE_REFLECTION(null, EnchantmentType.REFLECTION, 1, 3),
    ENCHANTED_BOOK_BUNDLE_SMALL_BRAIN(null, EnchantmentType.SMALL_BRAIN, 3, 1),
    ENCHANTED_BOOK_BUNDLE_THE_ONE(null, EnchantmentType.THE_ONE, 4, 1),
    ENCHANTED_BOOK_BUNDLE_VICIOUS(null, EnchantmentType.VICIOUS, 3, 5);

    private final @Nullable String itemId;
    private final @Nullable EnchantmentType enchantment;
    private final int level;
    private final int count;

    ItemBundle(@Nullable String itemId, @Nullable EnchantmentType enchantment, int level, int count) {
        this.itemId = itemId;
        this.enchantment = enchantment;
        this.level = level;
        this.count = count;
    }

    public int count() {
        return count;
    }

    public int level() {
        return level;
    }

    public @Nullable EnchantmentType enchantment() {
        return enchantment;
    }

    public @Nullable String itemId() {
        return itemId;
    }

    public List<SkyBlockItem> contents() {
        List<SkyBlockItem> items = new ArrayList<>();
        if (enchantment == null) {
            SkyBlockItem stack = new SkyBlockItem(ItemType.get(itemId));
            stack.setAmount(count);
            items.add(stack);
            return items;
        }
        for (int i = 0; i < count; i++) {
            SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
            book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(enchantment, level));
            items.add(book);
        }
        return items;
    }

    public static void registerInteractables() {
        for (ItemBundle bundle : values()) {
            InteractableRegistry.register(bundle.name(), InteractableItemConfig.builder()
                    .rightClickHandler((player, item) -> bundle.open(player, item)).build());
        }
    }

    private void open(SkyBlockPlayer player, SkyBlockItem item) {
        TimedConsumables.consumeHeld(player, item);
        player.addAndUpdateItem(contents());
        player.sendMessage("<a>You unwrapped the bundle!");
    }
}
