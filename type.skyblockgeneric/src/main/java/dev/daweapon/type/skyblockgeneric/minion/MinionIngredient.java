package dev.daweapon.type.skyblockgeneric.minion;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.MinionComponent;

import java.util.Map;
import java.util.function.Function;

@Getter
public class MinionIngredient {
    private final ItemType item;
    private final Integer amount;
    private final Integer minionTier;

    public MinionIngredient(ItemType item, Integer amount) {
        this(item, amount, null);
    }

    private MinionIngredient(ItemType item, Integer amount, Integer minionTier) {
        this.item = item;
        this.amount = amount;
        this.minionTier = minionTier;
    }

    public static MinionIngredient minion(ItemType minion, int tier) {
        return new MinionIngredient(minion, 1, tier);
    }

    public static MinionIngredient parse(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            return minion(ItemType.valueOf(map.get("minion").toString()), Integer.parseInt(map.get("tier").toString()));
        }
        String[] parts = raw.toString().split(":");
        return new MinionIngredient(ItemType.valueOf(parts[0]), Integer.parseInt(parts[1]));
    }

    public boolean isMinion() {
        return minionTier != null;
    }

    public ItemQuantifiable toQuantifiable() {
        if (!isMinion()) return new ItemQuantifiable(item, amount);
        SkyBlockItem minion = new SkyBlockItem(item);
        minion.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(minionTier, 0));
        return new ItemQuantifiable(minion, 1);
    }

    public static Function<SkyBlockItem, Boolean> exactTier(int tier) {
        return slotItem -> !slotItem.hasComponent(MinionComponent.class)
                || slotItem.getAttributeHandler().getMinionData().tier() == tier;
    }
}
