package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.NearestPlayerTarget;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.mob.ShaftMob;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import org.jetbrains.annotations.NotNull;

import java.util.List;

abstract class GlaciteShaftMob extends ShaftMob {
    private static final double AGGRO_RANGE = 24;
    private static final String HEAD = "d2a0057edd8e8480f0ea2a78c1ad88e401c4844f4458aeb4e33ac93a58b8c11a";

    protected GlaciteShaftMob(EntityType entityType) {
        super(entityType);
    }

    protected String texture() {
        return HEAD;
    }

    @Override
    protected long powder() {
        return 50;
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return new GUIMaterial(texture());
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of(new LastEntityDamagerTarget(this, 32), new NearestPlayerTarget(this, AGGRO_RANGE));
    }

    @Override
    public SkyBlockLootTable getLootTable() {
        return new SkyBlockLootTable() {
            @Override
            public @NotNull List<LootRecord> getLootTable() {
                return List.of(
                        new LootRecord(ItemType.ENCHANTED_GLACITE, 1, 100),
                        new LootRecord(ItemType.FINE_AQUAMARINE_GEM, 1, 5),
                        new LootRecord(ItemType.FINE_CITRINE_GEM, 1, 5),
                        new LootRecord(ItemType.FINE_ONYX_GEM, 1, 5),
                        new LootRecord(ItemType.FINE_PERIDOT_GEM, 1, 5),
                        new LootRecord(ItemType.GLACITE_JEWEL, 1, 4));
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }
}
