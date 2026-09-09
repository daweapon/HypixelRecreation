package net.swofty.type.skyblockgeneric.deletor;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.gui.inventories.deletor.GUIPersonalDeletor;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

/**
 * Handles the passive "junk deletion" behavior of the Personal Deletor accessory line.
 */
public final class PersonalDeletorService {
    private static final Map<Material, DatapointToggles.Toggles.ToggleType> JUNK_TOGGLES = Map.of(
            Material.COBBLESTONE, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_COBBLESTONE,
            Material.DIRT, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_DIRT,
            Material.NETHERRACK, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_NETHERRACK,
            Material.ROTTEN_FLESH, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_ROTTEN_FLESH,
            Material.GRAVEL, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_GRAVEL,
            Material.SAND, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_SAND,
            Material.STRING, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_STRING,
            Material.BONE, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_BONE
    );

    private PersonalDeletorService() {}

    public static void initialize() {
        InteractableRegistry.register("PERSONAL_DELETOR_INTERACT", InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> GUIPersonalDeletor.open(player))
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

            int maxDeletionsThisTick = tier;
            int deleted = 0;

            for (int slot = 0; slot < 36 && deleted < maxDeletionsThisTick; slot++) {
                ItemStack stack = player.getInventory().getItemStack(slot);
                if (stack.isAir()) continue;

                DatapointToggles.Toggles.ToggleType toggle = JUNK_TOGGLES.get(stack.material());
                if (toggle != null && player.getToggles().get(toggle)) {
                    player.getInventory().setItemStack(slot, ItemStack.AIR);
                    deleted++;
                }
            }
        }
    }

    private static int getEquippedTier(SkyBlockPlayer player) {
        for (SkyBlockItem accessory : player.getAccessoryBag().getUniqueAccessories()) {
            ItemType type = accessory.getAttributeHandler().getPotentialType();
            if (type == null) continue;
            switch (type) {
                case PERSONAL_DELETOR_4000 -> { return 1; }
                case PERSONAL_DELETOR_5000 -> { return 2; }
                case PERSONAL_DELETOR_6000 -> { return 3; }
                case PERSONAL_DELETOR_7000 -> { return 4; }
                default -> {}
            }
        }
        return 0;
    }
}