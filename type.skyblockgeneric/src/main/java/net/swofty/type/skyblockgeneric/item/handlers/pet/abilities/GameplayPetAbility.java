package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

/**
 * A pet ability that grants no statistics and is instead honoured by the gameplay system it
 * describes, with one value per rarity that does not grow with level.
 */
public final class GameplayPetAbility implements PetAbility {
    private final String name;
    private final RarityValue<Double> value;
    private final String[] description;

    public GameplayPetAbility(String name, RarityValue<Double> value, String... description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        String shown = decimalify(value.getForRarity(instance.getAttributeHandler().getRarity()), 2);
        return Arrays.stream(description).map(line -> line.replace("{0}", shown)).toList();
    }
}
