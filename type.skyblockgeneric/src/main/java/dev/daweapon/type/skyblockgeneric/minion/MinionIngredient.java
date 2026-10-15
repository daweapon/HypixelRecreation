package dev.daweapon.type.skyblockgeneric.minion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;

@AllArgsConstructor
@Getter
public class MinionIngredient {
    private ItemType item;
    private Integer amount;
}
