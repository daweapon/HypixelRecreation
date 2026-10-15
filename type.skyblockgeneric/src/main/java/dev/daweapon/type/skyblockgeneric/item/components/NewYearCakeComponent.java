package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.commons.text.Text;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NewYearCakeComponent extends SkyBlockItemComponent {

    public NewYearCakeComponent() {
        addInheritedComponent(new CustomDisplayNameComponent((item) -> {
            int year = item.getAttributeHandler().getNewYearCakeYear();
            return Text.of("<c>New Year Cake (Year {})", year);
        }));
        addInheritedComponent(new LoreUpdateComponent(
                new LoreConfig(this::lore, null),
                false
        ));
        addInheritedComponent(new TrackedUniqueComponent());
    }

    private List<String> lore(SkyBlockItem item, SkyBlockPlayer player) {
        int year = item.getAttributeHandler().getNewYearCakeYear();
        return List.of(
                "<7>Given to every player as a",
                "<7>celebration for the " + StringUtility.ntify(year) + " Skyblock",
                "<7>year!"
        );
    }

}