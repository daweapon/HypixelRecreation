package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.century.CakeSoul;
import dev.daweapon.type.skyblockgeneric.century.CakeSoulColour;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class CakeSoulComponent extends SkyBlockItemComponent {

    public CakeSoulComponent() {
        addInheritedComponent(new CustomDisplayNameComponent(item -> {
            CakeSoulColour colour = CakeSoul.colour(item);
            return colour == null ? Text.of("Cake Soul") : Text.of("Cake Soul ({})", colour.displayName());
        }));
        addInheritedComponent(new LoreUpdateComponent(new LoreConfig(this::lore, null), false));
    }

    private List<String> lore(SkyBlockItem item, SkyBlockPlayer player) {
        List<String> lines = new ArrayList<>();
        CakeSoulColour colour = CakeSoul.colour(item);
        if (colour != null) lines.add("<7>Color: " + colour.tag() + colour.displayName());
        String owner = CakeSoul.owner(item);
        if (owner != null) lines.add("<7>Cake owned by <f>" + owner);
        return lines;
    }
}
