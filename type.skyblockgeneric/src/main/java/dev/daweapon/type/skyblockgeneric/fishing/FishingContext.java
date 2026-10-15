package dev.daweapon.type.skyblockgeneric.fishing;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.FishingBaitComponent;
import dev.daweapon.type.skyblockgeneric.item.components.FishingRodPartComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

// Bundles all the state (player, rod, bait/parts, region, hotspot buffs) needed to resolve what
// a fishing cast catches.
public record FishingContext(
    SkyBlockPlayer player,
    SkyBlockItem rod,
    FishingMedium medium,
    @Nullable FishingBaitComponent bait,
    @Nullable FishingRodPartComponent hook,
    @Nullable FishingRodPartComponent line,
    @Nullable FishingRodPartComponent sinker,
    @Nullable String regionId,
    Pos hookPosition,
    boolean hotspotActive,
    ItemStatistics hotspotBuffs,
    long castDurationMs
) {
}
