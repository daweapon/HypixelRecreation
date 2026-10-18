package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.tab.TablistSkin;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;

public record TabLine(Text text, TablistSkin skin) {

    public static TabLine of(Text text) {
        return new TabLine(text, TablistSkinRegistry.GRAY);
    }

    public static TabLine header(Text text, TablistSkin skin) {
        return new TabLine(text, skin);
    }
}
