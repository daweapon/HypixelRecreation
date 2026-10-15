package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.List;

public class ExtraUnderNameComponent extends SkyBlockItemComponent {
    @Getter
    private final List<String> displays;

    public ExtraUnderNameComponent(String display) {
        this.displays = List.of(display);
    }

    public ExtraUnderNameComponent(List<String> displays) {
        this.displays = displays;
    }
}
