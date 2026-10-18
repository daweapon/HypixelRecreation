package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class MinionUpgradeRules {
    public static boolean insertableWithoutComponent(ItemType type) {
        return type == ItemType.KRAMPUS_HELMET || type == ItemType.HUNTER_KNIFE;
    }

    private static final Set<ItemType> SOULFLOW_ENGINES = Set.of(ItemType.LESSER_SOULFLOW_ENGINE, ItemType.SOULFLOW_ENGINE);
    private static final Set<ItemType> ONE_PER_PROFILE = Set.of(ItemType.POTATO_SPREADING, ItemType.SUPER_JACOB_SYSTEM);

    private MinionUpgradeRules() {
    }

    public static ItemType canonical(ItemType type) {
        return type == ItemType.FLY_CATCHER ? ItemType.FLYCATCHER_UPGRADE : type;
    }

    public static boolean allows(SkyBlockPlayer player, IslandMinionData.IslandMinion minion, ItemType type, SkyBlockItem item) {
        if (item.hasComponent(ItemRequirementsComponent.class)
                && !item.getComponent(ItemRequirementsComponent.class).ensureCanUse(player)) {
            return false;
        }
        String refusal = refusal(player.getSkyBlockIsland().getMinionData(), minion, type);
        if (refusal == null) return true;
        player.sendMessage(refusal);
        return false;
    }

    public static @Nullable String refusal(IslandMinionData island, IslandMinionData.IslandMinion minion, ItemType type) {
        if (SOULFLOW_ENGINES.contains(type)
                && SOULFLOW_ENGINES.stream().anyMatch(engine -> minion.getExtensionData().hasMinionUpgrade(engine))) {
            return "<c>This minion already has a Soulflow Engine.";
        }
        if (type == ItemType.SUPER_JACOB_SYSTEM && !MinionCategories.isFarming(minion.getMinion())) {
            return "<c>The Super Jacob Entertainment System only works in Farming Minions.";
        }
        if (ONE_PER_PROFILE.contains(type) && island != null
                && island.getMinions().stream().anyMatch(other -> other.getExtensionData().hasMinionUpgrade(type))) {
            return "<c>You can only use one " + type.getDisplayName() + " per profile.";
        }
        return null;
    }
}
