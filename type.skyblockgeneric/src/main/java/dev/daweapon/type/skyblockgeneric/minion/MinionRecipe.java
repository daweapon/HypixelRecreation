package dev.daweapon.type.skyblockgeneric.minion;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Getter
// Builds the crafting-shape ingredient map for upgrading a minion to a given tier number.
public class MinionRecipe {
    private final Function<MinionRecipeData, Map> recipeFunction;

    MinionRecipe(Function<MinionRecipeData, Map> recipeFunction) {
        this.recipeFunction = recipeFunction;
    }

    public static MinionRecipe fromNumber(int number) {
        try {
            return new MinionRecipe((materials) -> {
                Map<Character, ItemQuantifiable> map = new HashMap<>();
                map.put('A', materials.minionIngredients().get(number).toQuantifiable());
                if (number != 0) {
                    ItemType itemType = materials.minionItem();
                    SkyBlockItem item = new SkyBlockItem(itemType);
                    item.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(number, 1));
                    map.put('B', new ItemQuantifiable(item, 1));
                } else {
                    map.put('B', new ItemQuantifiable(materials.firstBaseItem(), 1));
                }
                return map;
            });
        } catch (IndexOutOfBoundsException e) {
            return new MinionRecipe((materials) -> {
                Map<Character, ItemQuantifiable> map = new HashMap<>();
                map.put('A', materials.minionIngredients().get(1).toQuantifiable());
                map.put('B', new ItemQuantifiable(materials.firstBaseItem(), 1));
                return map;
            });
        }
    }

    public record MinionRecipeData(List<MinionIngredient> minionIngredients, ItemType firstBaseItem, ItemType minionItem) {}
}
