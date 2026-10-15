package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.minestom.server.item.ItemAnimation;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.List;

public class DisableAnimationComponent extends SkyBlockItemComponent {
    @Getter
    private final List<ItemAnimation> disabledAnimations;

    public DisableAnimationComponent(List<ItemAnimation> disabledAnimations) {
        this.disabledAnimations = disabledAnimations;
    }
}