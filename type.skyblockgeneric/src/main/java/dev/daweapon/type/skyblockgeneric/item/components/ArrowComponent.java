package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.bow.ArrowEffect;
import org.jetbrains.annotations.Nullable;

public class ArrowComponent extends SkyBlockItemComponent {
    @Getter
    private final @Nullable ArrowEffect effect;

    public ArrowComponent() {
        this(null);
    }

    public ArrowComponent(@Nullable ArrowEffect effect) {
        this.effect = effect;
        addInheritedComponent(new ExtraRarityComponent("ARROW"));
    }
}
