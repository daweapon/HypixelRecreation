package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.reforge.ReforgeType;
import dev.daweapon.type.skyblockgeneric.fishing.FishingMedium;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class FishingRodMetadataComponent extends SkyBlockItemComponent {
    private final FishingMedium medium;
    private final int requiredFishingLevel;
    private final boolean rodPartsEnabled;
    private final @Nullable String legacyConversionTarget;
    private final @Nullable String subtitle;
    private final List<String> extraRequirements;

    public FishingRodMetadataComponent(
        FishingMedium medium,
        int requiredFishingLevel,
        boolean rodPartsEnabled,
        @Nullable String legacyConversionTarget,
        @Nullable String subtitle,
        List<String> extraRequirements,
        boolean soulbound
    ) {
        this.medium = medium;
        this.requiredFishingLevel = requiredFishingLevel;
        this.rodPartsEnabled = rodPartsEnabled;
        this.legacyConversionTarget = legacyConversionTarget;
        this.subtitle = subtitle;
        this.extraRequirements = List.copyOf(extraRequirements);
        FishingRodComponent rod = new FishingRodComponent();
        addInheritedComponent(rod);
        rod.getInheritedComponents().forEach(this::addInheritedComponent);
        addInheritedComponent(new EnchantableComponent(List.of(EnchantItemGroups.FISHING_ROD), true));
        addInheritedComponent(new ReforgableComponent(ReforgeType.FISHING_RODS));
        if (soulbound) addInheritedComponent(new DefaultSoulboundComponent(true));
        addInheritedComponent(new ExtraRarityComponent("FISHING ROD"));
    }
}
