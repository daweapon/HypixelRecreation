package net.swofty.type.skyblockgeneric.compactor;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.gui.inventories.compactor.GUIPersonalCompactor;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

/**
 * Handles the passive "auto-compacting" behavior of the Personal Compactor accessory line.
 * Converts every 9 of a raw material into 1 of its block form, matching vanilla 3x3 crafting ratios.
 */
public final class PersonalCompactorService {
    private static final Map<Material, Material> COMPACTION_PAIRS = Map.of(
            Material.IRON_INGOT, Material.IRON_BLOCK,
            Material.GOLD_INGOT, Material.GOLD_BLOCK,
            Material.DIAMOND, Material.DIAMOND_BLOCK,
            Material.EMERALD, Material.EMERALD_BLOCK,
            Material.COAL, Material.COAL_BLOCK,
            Material.REDSTONE, Material.REDSTONE_BLOCK,
            Material.LAPIS_LAZULI, Material.LAPIS_BLOCK
    );

    private static final Map<Material, DatapointToggles.Toggles.ToggleType> COMPACTION_TOGGLES = Map.of(
            Material.IRON_INGOT, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_IRON,
            Material.GOLD_INGOT, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_GOLD,
            Material.DIAMOND, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_DIAMOND,
            Material.EMERALD, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_EMERALD,
            Material.COAL, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_COAL,
            Material.REDSTONE, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_REDSTONE,
            Material.LAPIS_LAZULI, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_LAPIS
    );

    private PersonalCompactorService() {}

    public static void initialize() {
        InteractableRegistry.register("PERSONAL_COMPACTOR_INTERACT", InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> GUIPersonalCompactor.open(player))
                .build());

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            tick();
            return TaskSchedule.seconds(1);
        }, ExecutionType.TICK_START);
    }

    private static void tick() {
        List<SkyBlockPlayer> loadedPlayers = SkyBlockGenericLoader.getLoadedPlayers();
        for (SkyBlockPlayer player : loadedPlayers) {
            int tier = getEquippedTier(player);
            if (tier == 0) continue;

            int maxCompactionsThisTick = tier;
            int compacted = 0;

            for (int slot = 0; slot < 36 && compacted < maxCompactionsThisTick; slot++) {
                ItemStack stack = player.getInventory().getItemStack(slot);
                if (stack.isAir()) continue;

                Material compactedMaterial = COMPACTION_PAIRS.get(stack.material());
                if (compactedMaterial == null) continue;

                DatapointToggles.Toggles.ToggleType toggle = COMPACTION_TOGGLES.get(stack.material());
                if (toggle == null || !player.getToggles().get(toggle)) continue;

                int amount = stack.amount();
                if (amount < 9) continue;

                int groups = amount / 9;
                int remainder = amount % 9;

                player.getInventory().setItemStack(slot,
                        remainder > 0 ? ItemStack.of(stack.material(), remainder) : ItemStack.AIR);
                player.getInventory().addItemStack(ItemStack.of(compactedMaterial, groups));
                compacted++;
            }
        }
    }

    private static int getEquippedTier(SkyBlockPlayer player) {
        for (SkyBlockItem accessory : player.getAccessoryBag().getUniqueAccessories()) {
            ItemType type = accessory.getAttributeHandler().getPotentialType();
            if (type == null) continue;
            switch (type) {
                case PERSONAL_COMPACTOR_4000 -> { return 1; }
                case PERSONAL_COMPACTOR_5000 -> { return 2; }
                case PERSONAL_COMPACTOR_6000 -> { return 3; }
                case PERSONAL_COMPACTOR_7000 -> { return 4; }
                default -> {}
            }
        }
        return 0;
    }
}