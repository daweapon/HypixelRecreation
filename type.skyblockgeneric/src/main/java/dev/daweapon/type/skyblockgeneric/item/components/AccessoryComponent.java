package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryPowerRule;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryRealm;
import dev.daweapon.type.skyblockgeneric.accessory.effect.AccessoryEffect;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Marks an item as an accessory and carries its definition from the item YAML. Stats come from the
 * item's {@code default_statistics} and are applied by the Accessory Bag, never by holding the item.
 */
@Getter
public class AccessoryComponent extends SkyBlockItemComponent {
    private final boolean recombobulatable;
    private final boolean dungeon;
    private final AccessoryRealm realm;
    private final AccessoryPowerRule powerRule;
    private final @Nullable Rarity dynamicMaxRarity;
    private final List<AccessoryEffect> effects;

    public AccessoryComponent() {
        this(true, false, AccessoryRealm.ANYWHERE, AccessoryPowerRule.NORMAL, null, List.of());
    }

    public AccessoryComponent(boolean recombobulatable, boolean dungeon, AccessoryRealm realm,
                              AccessoryPowerRule powerRule, @Nullable Rarity dynamicMaxRarity,
                              List<AccessoryEffect> effects) {
        this.recombobulatable = recombobulatable;
        this.dungeon = dungeon;
        this.realm = realm;
        this.powerRule = powerRule;
        this.dynamicMaxRarity = dynamicMaxRarity;
        this.effects = effects;
        addInheritedComponent(new ExtraRarityComponent(dungeon ? "DUNGEON ACCESSORY" : "ACCESSORY"));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new ConstantStatisticsComponent());
    }
}
