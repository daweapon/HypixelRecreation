package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PlantType(String id, String name, @Nullable GardenCrop crop, boolean mutation, int stages, int baseYield,
                        List<GreenhouseEffect> effects, String surface, @Nullable String rarity) {
    public boolean baseCrop() {
        return crop != null;
    }

    public int footprint() {
        if (!mutation) return 1;
        MutationCatalogue.Mutation entry = MutationCatalogue.get().mutation(id);
        return entry == null ? 1 : entry.footprint();
    }
}
