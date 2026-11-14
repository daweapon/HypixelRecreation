package dev.daweapon.type.skyblockgeneric.carnival;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard.DowsingMode;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Shows the Carnival Shovel's current dowsing mode in its lore and model, surviving every item refresh. */
public final class CarnivalShovelDisplay {
    private static final Map<UUID, DowsingMode> MODES = new ConcurrentHashMap<>();
    private static final Map<DowsingMode, String> MODELS = new EnumMap<>(Map.of(
            DowsingMode.MINES, "hypixel_skyblock:item/uncategorized/carnival_shovel_iron",
            DowsingMode.ANCHOR, "hypixel_skyblock:item/uncategorized/carnival_shovel_diamond",
            DowsingMode.TREASURE, "hypixel_skyblock:item/uncategorized/carnival_shovel_gold"));

    private CarnivalShovelDisplay() {}

    public static void initialize() {
        PlayerItemUpdater.registerOverride(CarnivalShovelDisplay::apply);
    }

    public static void show(SkyBlockPlayer player, DowsingMode mode) {
        MODES.put(player.getUuid(), mode);
        refresh(player);
    }

    public static void clear(SkyBlockPlayer player) {
        MODES.remove(player.getUuid());
    }

    private static void refresh(SkyBlockPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || !isShovel(stack)) continue;
            player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, stack, true).build());
            return;
        }
    }

    private static boolean isShovel(ItemStack stack) {
        return new SkyBlockItem(stack).getAttributeHandler().getPotentialType() == ItemType.CARNIVAL_SHOVEL;
    }

    private static ItemStack.Builder apply(SkyBlockPlayer player, SkyBlockItem item, ItemStack.Builder builder) {
        DowsingMode mode = MODES.get(player.getUuid());
        if (mode == null || item.getAttributeHandler().getPotentialType() != ItemType.CARNIVAL_SHOVEL) return builder;
        List<Component> lore = builder.build().get(DataComponents.LORE);
        if (lore != null) builder.set(DataComponents.LORE, withMode(lore, mode));
        return builder.set(DataComponents.ITEM_MODEL, MODELS.get(mode));
    }

    private static List<Component> withMode(List<Component> lore, DowsingMode mode) {
        int modeLine = -1;
        int cycleLine = -1;
        for (int i = 0; i < lore.size(); i++) {
            String plain = PlainTextComponentSerializer.plainText().serialize(lore.get(i));
            if (modeLine < 0 && plain.startsWith("Dowsing Mode:")) modeLine = i;
            if (cycleLine < 0 && plain.startsWith("Right-click to cycle")) cycleLine = i;
        }
        if (cycleLine < 0) return lore;
        int from = modeLine < 0 ? cycleLine : modeLine;
        int to = modeLine < 0 ? cycleLine : cycleLine - 1;
        List<String> block = new ArrayList<>();
        block.add("<e>Dowsing Mode: " + mode.getDisplayName());
        block.addAll(mode.getDescription());
        if (modeLine < 0) block.add("");
        List<Component> updated = new ArrayList<>(lore.subList(0, from));
        block.forEach(line -> updated.add(Text.of(line).asComponent().decoration(TextDecoration.ITALIC, false)));
        updated.addAll(lore.subList(to, lore.size()));
        return updated;
    }
}
