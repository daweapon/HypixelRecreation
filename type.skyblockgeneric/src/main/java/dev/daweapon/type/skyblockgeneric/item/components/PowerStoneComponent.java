package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.type.skyblockgeneric.accessory.power.PowerStones;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;

public class PowerStoneComponent extends SkyBlockItemComponent {
    public PowerStoneComponent() {
        addInheritedComponent(new ExtraRarityComponent("POWER STONE"));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new LoreUpdateComponent(new LoreConfig((item, player) -> PowerStones.lore(item), null), false));
    }
}
