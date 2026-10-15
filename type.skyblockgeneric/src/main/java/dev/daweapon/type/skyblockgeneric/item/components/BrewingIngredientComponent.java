package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

@Getter
public class BrewingIngredientComponent extends SkyBlockItemComponent {
    private final int brewingTimeSeconds;
    private final String potionEffect;
    private final int effectDuration;
    private final int effectAmplifier;
    private final int alchemyXp;

    public BrewingIngredientComponent(int brewingTimeSeconds, String potionEffect,
                                       int effectDuration, int effectAmplifier, int alchemyXp) {
        this.brewingTimeSeconds = brewingTimeSeconds;
        this.potionEffect = potionEffect;
        this.effectDuration = effectDuration;
        this.effectAmplifier = effectAmplifier;
        this.alchemyXp = alchemyXp;

        addInheritedComponent(new ExtraUnderNameComponent("Brewing Ingredient"));
    }
}
