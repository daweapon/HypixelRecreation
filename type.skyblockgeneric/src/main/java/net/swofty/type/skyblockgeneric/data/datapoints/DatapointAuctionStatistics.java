package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// Persists a player's lifetime auction house statistics (items sold/bought by rarity, coins
// earned/spent, bid counts, etc.) as a simple counter map.
public class DatapointAuctionStatistics extends SkyBlockDatapoint<DatapointAuctionStatistics.AuctionStatistics> {

    public DatapointAuctionStatistics(String key, DatapointAuctionStatistics.AuctionStatistics value) {
        super(key, value, new Serializer<>() {
            @Override
            public String serialize(DatapointAuctionStatistics.AuctionStatistics value) {
                StringBuilder sb = new StringBuilder();

                if (value.getStatistics().isEmpty())
                    return "{}";

                sb.append("{");
                for (Map.Entry<AuctionStatistics.AuctionStat, Long> entry : value.getStatistics().entrySet()) {
                    sb.append("\"").append(entry.getKey().name()).append("\":").append(entry.getValue()).append(",");
                }

                sb.deleteCharAt(sb.length() - 1);
                sb.append("}");

                return sb.toString();
            }

            @Override
            public DatapointAuctionStatistics.AuctionStatistics deserialize(String json) {
                JSONObject obj = new JSONObject(json);
                Map<AuctionStatistics.AuctionStat, Long> map = new HashMap<>();

                if (obj.keySet().isEmpty())
                    return new AuctionStatistics(map);

                for (String key : obj.keySet()) {
                    map.put(AuctionStatistics.AuctionStat.valueOf(key), obj.getLong(key));
                }

                return new AuctionStatistics(map);
            }

            @Override
            public DatapointAuctionStatistics.AuctionStatistics clone(DatapointAuctionStatistics.AuctionStatistics value) {
                return new AuctionStatistics(new HashMap<>(value.getStatistics()));
            }
        });
    }

    public DatapointAuctionStatistics(String key) {
        this(key, new AuctionStatistics(new HashMap<>()));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AuctionStatistics {
        private Map<AuctionStat, Long> statistics;

        public Long get(AuctionStat stat) {
            if (!statistics.containsKey(stat))
                return 0L;
            return statistics.get(stat);
        }

        public void increment(AuctionStat stat) {
            add(stat, 1);
        }

        public void add(AuctionStat stat, long value) {
            statistics.put(stat, Math.addExact(get(stat), value));
        }

        public void maximum(AuctionStat stat, long value) {
            statistics.put(stat, Math.max(get(stat), value));
        }

        public void set(AuctionStat stat, long value) {
            statistics.put(stat, value);
        }

        public void sold(Rarity rarity) {
            AuctionStat stat = rarityStat(rarity, true);
            if (stat != null) increment(stat);
        }

        public void bought(Rarity rarity) {
            AuctionStat stat = rarityStat(rarity, false);
            if (stat != null) increment(stat);
        }

        private AuctionStat rarityStat(Rarity rarity, boolean sold) {
            if (rarity == null) return null;
            String tier = switch (rarity) {
                case VERY_SPECIAL, ADMIN -> "SPECIAL";
                case DIVINE -> "ULTIMATE";
                default -> rarity.name();
            };
            return AuctionStat.valueOf(tier + (sold ? "_SOLD" : "_BOUGHT"));
        }

        public enum AuctionStat {
            // Seller Stats
            AUCTIONS_CREATED,
            AUCTIONS_COMPLETED_WITH_BIDS,
            AUCTIONS_COMPLETED_WITHOUT_BIDS,
            HIGHEST_AUCTION_HELD,
            TOTAL_COINS_EARNED,
            COINS_SPENT_ON_FEES,
            COMMON_SOLD,
            UNCOMMON_SOLD,
            RARE_SOLD,
            EPIC_SOLD,
            LEGENDARY_SOLD,
            MYTHIC_SOLD,
            SPECIAL_SOLD,
            ULTIMATE_SOLD,

            // Buyer Stats
            AUCTIONS_WON,
            TOTAL_BIDS,
            HIGHEST_BID_MADE,
            TOTAL_COINS_SPENT,
            COMMON_BOUGHT,
            UNCOMMON_BOUGHT,
            RARE_BOUGHT,
            EPIC_BOUGHT,
            LEGENDARY_BOUGHT,
            MYTHIC_BOUGHT,
            SPECIAL_BOUGHT,
            ULTIMATE_BOUGHT,
        }
    }
}
