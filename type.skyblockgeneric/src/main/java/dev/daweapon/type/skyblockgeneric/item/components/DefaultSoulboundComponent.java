package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

public class DefaultSoulboundComponent extends SkyBlockItemComponent {
    @Getter
    private final boolean coopAllowed;

    public DefaultSoulboundComponent(boolean coopAllowed) {
        this.coopAllowed = coopAllowed;
    }
}