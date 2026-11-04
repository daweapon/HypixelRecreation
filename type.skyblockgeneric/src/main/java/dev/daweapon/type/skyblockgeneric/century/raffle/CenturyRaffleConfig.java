package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class CenturyRaffleConfig {
    public static final String RANDOM_CENTURY_CAKE = "RANDOM_CENTURY_CAKE";
    public static final String RANDOM_CAKE_SOUL = "RANDOM_CAKE_SOUL";
    private static final File FILE = new File("./configuration/skyblock/century_raffle.yml");
    private static volatile CenturyRaffleConfig instance;

    private final int onlineTicketMinutes;
    private final int joinTickets;
    private final int fedSlicesPerPeriod;
    private final Map<RaffleType, Long> referenceParticipants;
    private final Map<RaffleType, Map<PrizeTier, Long>> prizeCounts;
    private final Map<PrizeTier, List<String>> pools;
    private final List<FeedingReward> feeding;

    public enum FeedingKind { CENTURY_CAKE, CAKE_SOUL, TICKETS, SKILL_XP, COINS, ITEM }

    public record FeedingReward(FeedingKind kind, int amount, @Nullable SkillCategories skill, @Nullable String item, double weight) {
        public @Nullable ItemType itemType() {
            return item == null ? null : ItemType.get(item);
        }
    }

    private CenturyRaffleConfig(int onlineTicketMinutes, int joinTickets, int fedSlicesPerPeriod,
                                Map<RaffleType, Long> referenceParticipants, Map<RaffleType, Map<PrizeTier, Long>> prizeCounts,
                                Map<PrizeTier, List<String>> pools, List<FeedingReward> feeding) {
        this.onlineTicketMinutes = onlineTicketMinutes;
        this.joinTickets = joinTickets;
        this.fedSlicesPerPeriod = fedSlicesPerPeriod;
        this.referenceParticipants = referenceParticipants;
        this.prizeCounts = prizeCounts;
        this.pools = pools;
        this.feeding = feeding;
    }

    public static CenturyRaffleConfig get() {
        CenturyRaffleConfig current = instance;
        if (current == null) {
            synchronized (CenturyRaffleConfig.class) {
                if (instance == null) instance = load(FILE);
                current = instance;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    public static CenturyRaffleConfig load(File file) {
        Map<String, Object> root;
        try {
            root = YamlFileUtils.loadYaml(file);
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load century_raffle.yml");
            root = Map.of();
        }
        Map<RaffleType, Long> reference = new EnumMap<>(RaffleType.class);
        Map<RaffleType, Map<PrizeTier, Long>> counts = new EnumMap<>(RaffleType.class);
        Map<String, Object> raffles = root.get("raffles") instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        for (RaffleType type : RaffleType.values()) {
            Map<String, Object> raw = raffles.get(type.name()) instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
            reference.put(type, number(raw.get("reference_participants"), 100_000));
            Map<String, Object> prizes = raw.get("prizes") instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
            Map<PrizeTier, Long> tiers = new EnumMap<>(PrizeTier.class);
            for (PrizeTier tier : PrizeTier.values()) tiers.put(tier, number(prizes.get(tier.name()), 0));
            counts.put(type, tiers);
        }
        Map<PrizeTier, List<String>> pools = new EnumMap<>(PrizeTier.class);
        Map<String, Object> rawPools = root.get("prizes") instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        for (PrizeTier tier : PrizeTier.values()) {
            List<String> ids = new ArrayList<>();
            if (rawPools.get(tier.name()) instanceof List<?> list) {
                for (Object id : list) {
                    String value = String.valueOf(id);
                    if (isSpecial(value) || ItemType.get(value) != null) ids.add(value);
                    else Logger.warn("century_raffle.yml prize {} is not an item", value);
                }
            }
            pools.put(tier, List.copyOf(ids));
        }
        List<FeedingReward> feeding = new ArrayList<>();
        if (root.get("feeding") instanceof List<?> list) {
            for (Object entry : list) {
                if (!(entry instanceof Map<?, ?> raw)) continue;
                FeedingKind kind = FeedingKind.valueOf(String.valueOf(raw.get("reward")));
                SkillCategories skill = raw.get("skill") == null ? null : SkillCategories.valueOf(String.valueOf(raw.get("skill")));
                String item = raw.get("item") == null ? null : String.valueOf(raw.get("item"));
                feeding.add(new FeedingReward(kind, (int) number(raw.get("amount"), 1), skill, item, number(raw.get("weight"), 0)));
            }
        }
        return new CenturyRaffleConfig(
                (int) number(root.get("online_ticket_minutes"), 15),
                (int) number(root.get("join_tickets"), 3),
                (int) number(root.get("fed_slices_per_period"), 25),
                reference, counts, pools, List.copyOf(feeding));
    }

    private static long number(@Nullable Object raw, long fallback) {
        return raw instanceof Number number ? number.longValue() : fallback;
    }

    public static boolean isSpecial(String prize) {
        return RANDOM_CENTURY_CAKE.equals(prize) || RANDOM_CAKE_SOUL.equals(prize);
    }

    public int onlineTicketMinutes() {
        return onlineTicketMinutes;
    }

    public int joinTickets() {
        return joinTickets;
    }

    public int fedSlicesPerPeriod() {
        return fedSlicesPerPeriod;
    }

    public long referenceParticipants(RaffleType type) {
        return referenceParticipants.getOrDefault(type, 100_000L);
    }

    public long prizeCount(RaffleType type, PrizeTier tier) {
        return prizeCounts.getOrDefault(type, Map.of()).getOrDefault(tier, 0L);
    }

    public List<String> pool(PrizeTier tier) {
        return pools.getOrDefault(tier, List.of());
    }

    public List<FeedingReward> feeding() {
        return feeding;
    }
}
