package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class GeorgePets {
    public static final List<ItemType> CANDIDATES = List.of(
            ItemType.AMMONITE_PET, ItemType.ARMADILLO_PET, ItemType.BEE_PET, ItemType.BLUE_WHALE_PET,
            ItemType.CHICKEN_PET, ItemType.ELEPHANT_PET, ItemType.ENDERMAN_PET, ItemType.GIRAFFE_PET,
            ItemType.GRIFFIN_PET, ItemType.JERRY_PET, ItemType.MONKEY_PET, ItemType.PIG_PET,
            ItemType.RABBIT_PET, ItemType.SILVERFISH_PET, ItemType.TIGER_PET, ItemType.WOLF_PET);
    public static final int REQUESTED = 3;

    private GeorgePets() {
    }

    public static List<ItemType> requested(SkyBlockPlayer player) {
        return requested(player.getUuid());
    }

    public static List<ItemType> requested(UUID seed) {
        List<ItemType> shuffled = new ArrayList<>(CANDIDATES);
        Collections.shuffle(shuffled, new Random(seed.getMostSignificantBits() ^ seed.getLeastSignificantBits()));
        return List.copyOf(shuffled.subList(0, REQUESTED));
    }

    public static String describe(List<ItemType> pets) {
        List<String> names = pets.stream().map(pet -> "<a>" + pet.getDisplayName().replace(" Pet", "") + "<f>").toList();
        return String.join(", ", names.subList(0, names.size() - 1)) + " and " + names.getLast();
    }

    public static @Nullable List<SkyBlockItem> findAll(SkyBlockPlayer player) {
        List<SkyBlockItem> found = new ArrayList<>();
        for (ItemType wanted : requested(player)) {
            SkyBlockItem match = player.getPetData().getPetsMap().keySet().stream()
                    .filter(pet -> pet.getAttributeHandler().getPotentialType() == wanted)
                    .filter(pet -> PetLevels.level(pet) == 1)
                    .filter(pet -> !found.contains(pet))
                    .findFirst().orElse(null);
            if (match == null) return null;
            found.add(match);
        }
        return found;
    }
}
