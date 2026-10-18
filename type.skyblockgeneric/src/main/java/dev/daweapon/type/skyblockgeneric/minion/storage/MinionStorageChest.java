package dev.daweapon.type.skyblockgeneric.minion.storage;

import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import lombok.Getter;
import net.minestom.server.coordinate.Vec;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class MinionStorageChest {
    private final MinionStorageTier tier;
    private final Vec position;
    private final List<ItemQuantifiable> items;

    public MinionStorageChest(MinionStorageTier tier, Vec position, List<ItemQuantifiable> items) {
        this.tier = tier;
        this.position = position;
        this.items = items;
    }

    public int storedAmount() {
        return items.stream().mapToInt(ItemQuantifiable::getAmount).sum();
    }

    public int freeAmount() {
        return Math.max(0, tier.capacityItems() - storedAmount());
    }

    public boolean add(SkyBlockItem item) {
        if (item.getAmount() > freeAmount()) return false;
        for (ItemQuantifiable entry : items) {
            if (entry.matchesType(item)) {
                entry.setAmount(entry.getAmount() + item.getAmount());
                return true;
            }
        }
        items.add(new ItemQuantifiable(item.getAttributeHandler().getPotentialType(), item.getAmount()));
        return true;
    }

    public boolean isAt(int x, int y, int z) {
        return position.blockX() == x && position.blockY() == y && position.blockZ() == z;
    }

    public String serialize() {
        JSONArray stored = new JSONArray();
        items.forEach(entry -> stored.put(entry.getItem().toUnderstandable().serialize() + "," + entry.getAmount()));
        return new JSONObject()
                .put("tier", tier.name())
                .put("position", position.blockX() + "," + position.blockY() + "," + position.blockZ())
                .put("items", stored)
                .toString();
    }

    public static MinionStorageChest deserialize(String raw) {
        JSONObject json = new JSONObject(raw);
        String[] coordinates = json.getString("position").split(",");
        Vec position = new Vec(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
        List<ItemQuantifiable> items = new ArrayList<>();
        JSONArray stored = json.getJSONArray("items");
        for (int i = 0; i < stored.length(); i++) {
            String entry = stored.getString(i);
            int split = entry.lastIndexOf(',');
            items.add(new ItemQuantifiable(
                    new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(entry.substring(0, split))),
                    Integer.parseInt(entry.substring(split + 1))));
        }
        return new MinionStorageChest(MinionStorageTier.valueOf(json.getString("tier")), position, items);
    }
}
