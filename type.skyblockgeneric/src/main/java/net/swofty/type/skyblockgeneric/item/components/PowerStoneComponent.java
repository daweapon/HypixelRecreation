package net.swofty.type.skyblockgeneric.item.components;

import net.swofty.type.skyblockgeneric.accessory.power.PowerStones;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;

public class PowerStoneComponent extends SkyBlockItemComponent {
    public PowerStoneComponent() {
        addInheritedComponent(new ExtraRarityComponent("POWER STONE"));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new LoreUpdateComponent(new LoreConfig((item, player) -> PowerStones.lore(item), null), false));
    }
}
