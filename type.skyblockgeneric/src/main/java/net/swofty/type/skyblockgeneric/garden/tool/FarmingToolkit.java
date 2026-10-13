package net.swofty.type.skyblockgeneric.garden.tool;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FarmingToolkit {
    public static final String HANDLER = "FARMING_TOOLKIT";

    public record Slot(String family, String name, int guiSlot) {
    }

    public static final List<Slot> SLOTS = List.of(
            new Slot("THEORETICAL_HOE_WHEAT", "Euclid's Wheat Sickle", 10),
            new Slot("THEORETICAL_HOE_CARROT", "Gauss Carrot Shovel", 11),
            new Slot("THEORETICAL_HOE_POTATO", "Pythagorean Potato Shovel", 12),
            new Slot("PUMPKIN_DICER", "Pumpkin Dicer", 13),
            new Slot("MELON_DICER", "Melon Dicer", 14),
            new Slot("FUNGI_CUTTER", "Fungi Cutter", 15),
            new Slot("CACTUS_KNIFE", "Cactus Knife", 16),
            new Slot("THEORETICAL_HOE_CANE", "Turing Sugar Cane Cutter", 20),
            new Slot("THEORETICAL_HOE_WARTS", "Newton Nether Wart Cutter", 21),
            new Slot("COCO_CHOPPER", "Cocoa Chopper", 22),
            new Slot("THEORETICAL_HOE_SUNFLOWER", "Eclipse Sickle", 23),
            new Slot("THEORETICAL_HOE_WILD_ROSE", "Wild Rose Cutter", 24));

    private FarmingToolkit() {
    }

    public static void registerInteractables() {
        net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry.register(HANDLER,
                net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig.builder()
                        .rightClickHandler((player, _) -> consume(player))
                        .build());
    }

    public static boolean consume(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        if (profile.toolkit().isUnlocked()) {
            player.sendMessage("<c>You have already unlocked this toolkit!");
            return false;
        }
        if (!player.removeItemFromPlayer(net.swofty.commons.skyblock.item.ItemType.FARMING_TOOLKIT, 1)) return false;
        profile.toolkit().setUnlocked(true);
        Gardens.save(player, profile);
        GardenService.syncExperience(player);
        player.sendMessage("<a>You have unlocked the Farming Toolkit! Access it with <e>/farmingtoolkit");
        return true;
    }

    public static @Nullable Slot slotOf(@Nullable String itemId) {
        if (itemId == null) return null;
        FarmingTools.Tool tool = FarmingTools.of(itemId);
        if (tool == null) return null;
        String family = FarmingTools.familyOf(itemId);
        return SLOTS.stream().filter(slot -> slot.family().equals(family)).findFirst().orElse(null);
    }

    public static boolean store(SkyBlockPlayer player, int inventorySlot) {
        GardenProfile profile = Gardens.profile(player);
        ItemStack stack = player.getInventory().getItemStack(inventorySlot);
        if (stack.isAir()) return false;
        SkyBlockItem item = new SkyBlockItem(stack);
        Slot slot = slotOf(item.getAttributeHandler().getTypeAsString());
        if (slot == null) {
            player.sendMessage("<c>That is not a Farming Tool the toolkit can hold!");
            return false;
        }
        if (profile.toolkit().stored(slot.family()) != null) {
            player.sendMessage("<c>The toolkit already holds a " + slot.name() + "<c>!");
            return false;
        }
        profile.toolkit().store(slot.family(), item.toUnderstandable().serialize());
        player.getInventory().setItemStack(inventorySlot, ItemStack.AIR);
        Gardens.save(player, profile);
        return true;
    }

    public static boolean retrieve(SkyBlockPlayer player, Slot slot) {
        GardenProfile profile = Gardens.profile(player);
        String serialized = profile.toolkit().take(slot.family());
        if (serialized == null) return false;
        Gardens.save(player, profile);
        player.addAndUpdateItem(new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(serialized)));
        return true;
    }

    public static @Nullable SkyBlockItem stored(GardenProfile profile, Slot slot) {
        String serialized = profile.toolkit().stored(slot.family());
        return serialized == null ? null : new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(serialized));
    }
}
