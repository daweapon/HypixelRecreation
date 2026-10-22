package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobBarnPig extends MobBarnAnimal {
    public MobBarnPig() {
        super(EntityType.PIG);
    }

    @Override
    public String getDisplayName() {
        return "Pig";
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
                        new LootRecord(ItemType.RAW_PORKCHOP, makeAmountBetween(1, 3), 100)
                );
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }
}
