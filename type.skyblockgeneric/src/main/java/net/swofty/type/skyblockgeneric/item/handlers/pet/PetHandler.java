package net.swofty.type.skyblockgeneric.item.handlers.pet;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.*;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;

import java.util.List;

public enum PetHandler {
    BEE(List.of(
            at(new HiveAbility(), Rarity.COMMON),
            at(new BusyBuzzBuzzAbility(), Rarity.RARE),
            at(new HoneyHarvesterAbility(), Rarity.LEGENDARY),
            at(new PoweredByPollenAbility(), Rarity.MYTHIC)
    )),
    GRANDMA_WOLF(List.of(
            at(new KillComboAbility(), Rarity.COMMON)
    )),
    CHICKEN(List.of(
            at(new FreeRangeAbility(), Rarity.COMMON),
            at(new EggstraLootAbility(), Rarity.RARE),
            at(new LightFeetAbility(), Rarity.LEGENDARY)
    )),
    // The 6 Traveling Zoo pets (Roadmap #21) - stat-only for now, no ability kit sourced.
    ZOO(List.of()),
    // Black Cat (Roadmap #19, Fear Mongerer shop) - real Hypixel's unique Hunter/Omen/Supernatural
    // ability tree isn't implemented, stat-only like ZOO above.
    SPOOKY(List.of());

    private final List<AbilityEntry> abilities;

    PetHandler(List<AbilityEntry> abilities) {
        this.abilities = abilities;
    }

    public List<PetAbility> getAbilities(SkyBlockItem item) {
        Rarity rarity = item.getAttributeHandler().getRarity();
        return abilities.stream()
                .filter(e -> rarity.isAtLeast(e.minimumRarity()))
                .map(AbilityEntry::ability)
                .toList();
    }

    public record AbilityEntry(PetAbility ability, Rarity minimumRarity) {
    }

    private static AbilityEntry at(PetAbility ability, Rarity rarity) {
        return new AbilityEntry(ability, rarity);
    }
}
