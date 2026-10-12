package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.bazaar.BazaarHistory;
import net.swofty.type.skyblockgeneric.bazaar.BazaarProduct;
import net.swofty.type.skyblockgeneric.bazaar.BazaarProducts;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public class DatapointBazaarHistory extends SkyBlockDatapoint<List<BazaarHistory.Entry>> {
    public static final Serializer<List<BazaarHistory.Entry>> serializer = new Serializer<>() {
        @Override
        public String serialize(List<BazaarHistory.Entry> value) {
            JSONArray array = new JSONArray();
            for (BazaarHistory.Entry entry : value) {
                array.put(new JSONObject()
                        .put("at", entry.at())
                        .put("kind", entry.kind().name())
                        .put("item", entry.item().id())
                        .put("amount", entry.amount())
                        .put("coins", entry.coins())
                        .put("player", entry.player().serialize()));
            }
            return array.toString();
        }

        @Override
        public List<BazaarHistory.Entry> deserialize(String json) {
            List<BazaarHistory.Entry> entries = new ArrayList<>();
            if (json == null || json.isEmpty()) {
                return entries;
            }
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length() && entries.size() < BazaarHistory.CAPACITY; i++) {
                JSONObject stored = array.getJSONObject(i);
                try {
                    BazaarProduct product = BazaarProducts.byId(stored.getString("item"));
                    if (product == null) {
                        continue;
                    }
                    entries.add(new BazaarHistory.Entry(
                            stored.getLong("at"),
                            BazaarHistory.Kind.valueOf(stored.getString("kind")),
                            product,
                            stored.getInt("amount"),
                            stored.getDouble("coins"),
                            Text.read(stored.getString("player"))));
                } catch (RuntimeException e) {
                    Logger.warn(e, "Skipping unreadable Bazaar history entry");
                }
            }
            return entries;
        }

        @Override
        public List<BazaarHistory.Entry> clone(List<BazaarHistory.Entry> value) {
            return new ArrayList<>(value);
        }
    };

    public DatapointBazaarHistory(String key, List<BazaarHistory.Entry> value) {
        super(key, value, serializer);
    }

    public DatapointBazaarHistory(String key) {
        this(key, new ArrayList<>());
    }
}
