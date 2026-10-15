package dev.daweapon.type.skyblockgeneric.garden.visitor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public record VisitorOffer(List<Required> required, long farmingXp, int gardenXp, int copper, int bits) {
    public static final class Required {
        private final String item;
        private final String name;
        private final int amount;
        private int remaining;

        public Required(String item, String name, int amount, int remaining) {
            this.item = item;
            this.name = name;
            this.amount = amount;
            this.remaining = remaining;
        }

        public String item() {
            return item;
        }

        public String name() {
            return name;
        }

        public int amount() {
            return amount;
        }

        public int remaining() {
            return remaining;
        }

        public void give(int count) {
            remaining = Math.max(0, remaining - count);
        }
    }

    public boolean complete() {
        return required.stream().allMatch(requirement -> requirement.remaining() == 0);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONArray items = new JSONArray();
        for (Required requirement : required) {
            items.put(new JSONObject()
                    .put("item", requirement.item())
                    .put("name", requirement.name())
                    .put("amount", requirement.amount())
                    .put("remaining", requirement.remaining()));
        }
        return json.put("required", items).put("farmingXp", farmingXp).put("gardenXp", gardenXp)
                .put("copper", copper).put("bits", bits);
    }

    public static VisitorOffer fromJson(JSONObject json) {
        List<Required> required = new ArrayList<>();
        for (Object entry : json.getJSONArray("required")) {
            JSONObject item = (JSONObject) entry;
            required.add(new Required(item.getString("item"), item.getString("name"), item.getInt("amount"), item.getInt("remaining")));
        }
        return new VisitorOffer(required, json.getLong("farmingXp"), json.getInt("gardenXp"), json.getInt("copper"), json.getInt("bits"));
    }
}
