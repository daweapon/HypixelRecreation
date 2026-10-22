package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobBarnChicken extends MobBarnAnimal {
    public MobBarnChicken() {
        super(EntityType.CHICKEN);
    }

    @Override
    public String getDisplayName() {
        return "Chicken";
    }

    @Override
    protected double health() {
        return 50;
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return List.of(
                        new LootRecord(ItemType.RAW_CHICKEN, 1, 100),
                        new LootRecord(ItemType.FEATHER, makeAmountBetween(1, 2), 60)
                );
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }
}
