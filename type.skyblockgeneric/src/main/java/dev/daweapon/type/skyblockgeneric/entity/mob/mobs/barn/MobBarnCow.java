package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobBarnCow extends MobBarnAnimal {
    public MobBarnCow() {
        super(EntityType.COW);
    }

    @Override
    public String getDisplayName() {
        return "Cow";
    }

    @Override
    protected double health() {
        return 100;
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return List.of(
                        new LootRecord(ItemType.RAW_BEEF, makeAmountBetween(1, 3), 100),
                        new LootRecord(ItemType.LEATHER, makeAmountBetween(1, 2), 100)
                );
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }
}
