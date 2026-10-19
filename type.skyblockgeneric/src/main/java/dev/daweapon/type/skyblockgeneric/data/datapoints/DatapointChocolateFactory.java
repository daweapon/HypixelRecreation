package dev.daweapon.type.skyblockgeneric.data.datapoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateShopStock;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.rabbits.HoppityEggType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Persists a player's Chocolate Factory/Hoppity's Hunt progress: chocolate totals, upgrades,
// employees, time tower state, and found rabbits/eggs, plus the chocolate production math.
public class DatapointChocolateFactory extends SkyBlockDatapoint<DatapointChocolateFactory.ChocolateFactoryData> {
    private static final Serializer<ChocolateFactoryData> serializer = new Serializer<>() {
        @Override
        public String serialize(ChocolateFactoryData value) {
            JSONObject json = new JSONObject();
            json.put("chocolate", value.getChocolate());
            json.put("chocolateAllTime", value.getChocolateAllTime());
            json.put("lastUpdated", value.getLastUpdated());
            json.put("partialChocolate", value.getPartialChocolate());
            json.put("timeTowerLevel", value.getTimeTowerLevel());
            json.put("timeTowerCharges", value.getTimeTowerCharges());
            json.put("timeTowerLastUsed", value.getTimeTowerLastUsed());
            json.put("timeTowerActiveUntil", value.getTimeTowerActiveUntil());
            json.put("rabbitBarnLevel", value.getRabbitBarnLevel());
            json.put("handBakedChocolateLevel", value.getHandBakedChocolateLevel());
            json.put("rabbitShrineLevel", value.getRabbitShrineLevel());
            json.put("coachJackrabbitLevel", value.getCoachJackrabbitLevel());
            json.put("employees", serializeEmployees(value.getEmployees()));
            json.put("totalClicks", value.getTotalClicks());
            json.put("totalTimeTowerUsages", value.getTotalTimeTowerUsages());
            json.put("foundRabbits", serializeEnumSet(value.getFoundRabbits()));
            json.put("totalChocolateSpent", value.getTotalChocolateSpent());
            json.put("claimedEggs", serializeEnumSet(value.getClaimedEggs()));
            json.put("totalEggsFound", value.getTotalEggsFound());
            json.put("prestigeLevel", value.getPrestigeLevel());
            json.put("chocolateThisPrestige", value.getChocolateThisPrestige());
            json.put("rabbitDuplicates", serializeCounts(value.getRabbitDuplicates()));
            json.put("claimedSpawns", new JSONArray(value.getClaimedSpawns()));
            json.put("uniqueEggLocations", new JSONArray(value.getUniqueEggLocations()));
            json.put("mythicPity", value.getMythicPity());
            json.put("divinePity", value.getDivinePity());
            json.put("timeTowerChargeClock", value.getTimeTowerChargeClock());
            json.put("hitmanSlots", value.getHitmanSlots());
            json.put("hitmanEggs", new JSONArray(value.getHitmanEggs()));
            json.put("hitmanQueueEnd", value.getHitmanQueueEnd());
            json.put("hitmanLastCheckedSpawn", value.getHitmanLastCheckedSpawn());
            json.put("hitmanLastEggFinder", value.getHitmanLastEggFinder());
            json.put("goldenClickBonuses", value.getGoldenClickBonuses());
            json.put("goldenClickYear", value.getGoldenClickYear());
            json.put("elDoradoCatches", value.getElDoradoCatches());
            json.put("strayDueAt", value.getStrayDueAt());
            json.put("hoppityShopYear", value.getHoppityShopYear());
            json.put("hoppityStock", new JSONArray(value.getHoppityStock()));
            json.put("hoppityPurchases", new JSONArray(value.getHoppityPurchases()));
            json.put("shopStockYear", value.getShopStockYear());
            json.put("shopPurchases", serializeShopPurchases(value.getShopPurchases()));
            return json.toString();
        }

        @Override
        public ChocolateFactoryData deserialize(String json) {
            if (json == null || json.isEmpty()) {
                return new ChocolateFactoryData();
            }

            JSONObject jsonObject = new JSONObject(json);
            ChocolateFactoryData data = new ChocolateFactoryData();
            data.setChocolate(jsonObject.optLong("chocolate", 0L));
            data.setChocolateAllTime(jsonObject.optLong("chocolateAllTime", 0L));
            data.setLastUpdated(jsonObject.optLong("lastUpdated", System.currentTimeMillis()));
            data.setPartialChocolate(jsonObject.optDouble("partialChocolate", 0.0));
            data.setTimeTowerLevel(jsonObject.optInt("timeTowerLevel", 0));
            data.setTimeTowerCharges(jsonObject.optInt("timeTowerCharges", 0));
            data.setTimeTowerLastUsed(jsonObject.optLong("timeTowerLastUsed", 0L));
            data.setTimeTowerActiveUntil(jsonObject.optLong("timeTowerActiveUntil", 0L));
            data.setRabbitBarnLevel(jsonObject.optInt("rabbitBarnLevel", 0));
            data.setHandBakedChocolateLevel(jsonObject.optInt("handBakedChocolateLevel", 0));
            data.setRabbitShrineLevel(jsonObject.optInt("rabbitShrineLevel", 0));
            data.setCoachJackrabbitLevel(jsonObject.optInt("coachJackrabbitLevel", 0));
            data.setEmployees(deserializeEmployees(jsonObject.optJSONObject("employees")));
            data.setTotalClicks(jsonObject.optLong("totalClicks", 0L));
            data.setTotalTimeTowerUsages(jsonObject.optInt("totalTimeTowerUsages", 0));
            data.setFoundRabbits(deserializeEnumSet(jsonObject.optJSONArray("foundRabbits"), ChocolateRabbit.class));
            data.setTotalChocolateSpent(jsonObject.optLong("totalChocolateSpent", 0L));
            data.setClaimedEggs(deserializeEnumSet(jsonObject.optJSONArray("claimedEggs"), HoppityEggType.class));
            data.setTotalEggsFound(jsonObject.optInt("totalEggsFound", 0));
            if (jsonObject.has("prestigeLevel")) {
                data.setPrestigeLevel(jsonObject.getInt("prestigeLevel"));
                data.setChocolateThisPrestige(jsonObject.optLong("chocolateThisPrestige", 0L));
            } else {
                data.setPrestigeLevel(legacyPrestigeLevel(data.getChocolateAllTime()));
                data.setChocolateThisPrestige(data.getChocolateAllTime());
            }
            data.setRabbitDuplicates(deserializeCounts(jsonObject.optJSONObject("rabbitDuplicates")));
            data.setClaimedSpawns(deserializeStrings(jsonObject.optJSONArray("claimedSpawns"), HashSet::new));
            data.setUniqueEggLocations(deserializeStrings(jsonObject.optJSONArray("uniqueEggLocations"), HashSet::new));
            data.setMythicPity(jsonObject.optInt("mythicPity", 0));
            data.setDivinePity(jsonObject.optInt("divinePity", 0));
            data.setTimeTowerChargeClock(jsonObject.optLong("timeTowerChargeClock", 0L));
            data.setHitmanSlots(jsonObject.optInt("hitmanSlots", 0));
            data.setHitmanEggs(deserializeStrings(jsonObject.optJSONArray("hitmanEggs"), ArrayList::new));
            data.setHitmanQueueEnd(jsonObject.optLong("hitmanQueueEnd", 0L));
            data.setHitmanLastCheckedSpawn(jsonObject.optString("hitmanLastCheckedSpawn", ""));
            data.setHitmanLastEggFinder(jsonObject.optLong("hitmanLastEggFinder", 0L));
            data.setGoldenClickBonuses(jsonObject.optInt("goldenClickBonuses", 0));
            data.setGoldenClickYear(jsonObject.optInt("goldenClickYear", 0));
            data.setElDoradoCatches(jsonObject.optInt("elDoradoCatches", 0));
            data.setStrayDueAt(jsonObject.optLong("strayDueAt", 0L));
            data.setHoppityShopYear(jsonObject.optInt("hoppityShopYear", 0));
            data.setHoppityStock(deserializeStrings(jsonObject.optJSONArray("hoppityStock"), ArrayList::new));
            data.setHoppityPurchases(deserializeStrings(jsonObject.optJSONArray("hoppityPurchases"), HashSet::new));
            data.setShopStockYear(jsonObject.optInt("shopStockYear", 0));
            data.setShopPurchases(deserializeShopPurchases(jsonObject.optJSONObject("shopPurchases")));
            return data;
        }

        @Override
        public ChocolateFactoryData clone(ChocolateFactoryData value) {
            return deserialize(serialize(value));
        }

        private int legacyPrestigeLevel(long allTime) {
            if (allTime >= 1_000_000_000_000L) return 6;
            if (allTime >= 10_000_000_000L) return 5;
            if (allTime >= 1_000_000_000L) return 4;
            if (allTime >= 100_000_000L) return 3;
            if (allTime >= 10_000_000L) return 2;
            if (allTime >= 1_000_000L) return 1;
            return 0;
        }

        private JSONObject serializeShopPurchases(Map<String, Integer> purchases) {
            JSONObject json = new JSONObject();
            purchases.forEach(json::put);
            return json;
        }

        private Map<String, Integer> deserializeShopPurchases(JSONObject json) {
            Map<String, Integer> purchases = new HashMap<>();
            if (json == null) return purchases;
            for (String key : json.keySet()) purchases.put(key, json.optInt(key, 0));
            return purchases;
        }

        private JSONObject serializeCounts(Map<ChocolateRabbit, Integer> counts) {
            JSONObject counted = new JSONObject();
            for (Map.Entry<ChocolateRabbit, Integer> entry : counts.entrySet()) {
                counted.put(entry.getKey().name(), entry.getValue());
            }
            return counted;
        }

        private Map<ChocolateRabbit, Integer> deserializeCounts(JSONObject counted) {
            Map<ChocolateRabbit, Integer> counts = new HashMap<>();
            if (counted == null) return counts;
            for (String key : counted.keySet()) {
                try {
                    counts.put(ChocolateRabbit.valueOf(key), counted.getInt(key));
                } catch (IllegalArgumentException ignored) {
                    // Rabbit removed or renamed in a later version.
                }
            }
            return counts;
        }

        private <C extends java.util.Collection<String>> C deserializeStrings(JSONArray array, java.util.function.Supplier<C> factory) {
            C values = factory.get();
            if (array == null) return values;
            for (int i = 0; i < array.length(); i++) values.add(array.getString(i));
            return values;
        }

        private JSONObject serializeEmployees(Map<EmployeeType, EmployeeData> employees) {
            JSONObject employeesJson = new JSONObject();
            for (Map.Entry<EmployeeType, EmployeeData> entry : employees.entrySet()) {
                JSONObject employeeJson = new JSONObject();
                EmployeeData employee = entry.getValue();
                employeeJson.put("level", employee.getLevel());
                employeeJson.put("baseProduction", employee.getBaseProduction());
                employeesJson.put(entry.getKey().name(), employeeJson);
            }
            return employeesJson;
        }

        private Map<EmployeeType, EmployeeData> deserializeEmployees(JSONObject employeesJson) {
            Map<EmployeeType, EmployeeData> employees = new HashMap<>();
            if (employeesJson == null) {
                return employees;
            }

            for (String key : employeesJson.keySet()) {
                JSONObject employeeJson = employeesJson.getJSONObject(key);
                EmployeeType.fromName(key).ifPresent(type -> employees.put(type, new EmployeeData(
                        employeeJson.optInt("level", 0),
                        employeeJson.optDouble("baseProduction", type.getBaseProductionPerLevel())
                )));
            }
            return employees;
        }

        private JSONArray serializeEnumSet(Set<? extends Enum<?>> values) {
            JSONArray array = new JSONArray();
            for (Enum<?> value : values) {
                array.put(value.name());
            }
            return array;
        }

        private <E extends Enum<E>> Set<E> deserializeEnumSet(JSONArray array, Class<E> type) {
            Set<E> values = new HashSet<>();
            if (array == null) {
                return values;
            }

            for (int i = 0; i < array.length(); i++) {
                try {
                    values.add(Enum.valueOf(type, array.getString(i)));
                } catch (IllegalArgumentException ignored) {
                    // Ignore values removed or renamed in a later version.
                }
            }
            return values;
        }
    };

    public DatapointChocolateFactory(String key, ChocolateFactoryData value, Serializer<ChocolateFactoryData> serializer) {
        super(key, value, serializer);
    }

    public DatapointChocolateFactory(String key, ChocolateFactoryData value) {
        super(key, value, serializer);
    }

    public DatapointChocolateFactory(String key) {
        super(key, new ChocolateFactoryData(), serializer);
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class ChocolateFactoryData {
        private static final int BASE_CLICK_POWER = 1;
        private static final int BASE_BARN_CAPACITY = 20;
        private static final int BARN_CAPACITY_PER_LEVEL = 2;
        private static final double BASE_MULTIPLIER = 1.0;
        private static final double TIME_TOWER_MULTIPLIER_PER_LEVEL = 0.1;
        private static final double MU_TIME_TOWER_BONUS = 0.7;
        private static final double COACH_MULTIPLIER_PER_LEVEL = 0.01;
        private static final double GOLDEN_CLICK_CPS = 5.0;
        public static final int MAX_PRESTIGE = 5;
        public static final int TIME_TOWER_MAX_CHARGES = 3;
        public static final long TIME_TOWER_CHARGE_MS = 8L * 3_600_000L;
        public static final long TIME_TOWER_CHARGE_EINSTEIN_MS = 7L * 3_600_000L;
        private static final long[] PRESTIGE_THRESHOLDS = {150_000_000L, 1_000_000_000L, 4_000_000_000L, 10_000_000_000L, 30_000_000_000L};
        private static final double[] PRESTIGE_MULTIPLIERS = {1.0, 1.1, 1.25, 1.5, 2.0, 2.5};
        private static final long[] MAX_CHOCOLATE = {500_000_000L, 1_200_000_000L, 5_000_000_000L, 25_000_000_000L, 50_000_000_000L, 100_000_000_000L};
        private static final int BASE_EMPLOYEE_CAP = 120;
        private static final int EMPLOYEE_CAP_PER_PRESTIGE = 20;
        private static final long TIME_TOWER_DURATION_MS = 60L * 60L * 1000L;
        private static final double MILLIS_PER_SECOND = 1000.0;
        private final java.util.concurrent.atomic.AtomicReference<Double> petMultiplier = new java.util.concurrent.atomic.AtomicReference<>(0D);
        private int rabbitYouthLevels;
        private double shardFlatProduction;
        private double shardMultiplier;
        private double cookieMultiplier;
        private long chocolate;
        private long chocolateAllTime;
        private long lastUpdated = System.currentTimeMillis();
        private double partialChocolate; // accumulates fractional chocolate production
        private int timeTowerLevel;
        private int timeTowerCharges;
        private long timeTowerLastUsed;
        private long timeTowerActiveUntil;
        private long timeTowerChargeClock;
        private int rabbitBarnLevel;
        private int handBakedChocolateLevel;
        private int rabbitShrineLevel;
        private int coachJackrabbitLevel;
        private Map<EmployeeType, EmployeeData> employees = new HashMap<>();
        private long totalClicks;
        private int totalTimeTowerUsages;
        private Set<ChocolateRabbit> foundRabbits = new HashSet<>();
        private long totalChocolateSpent;
        private Set<HoppityEggType> claimedEggs = new HashSet<>();
        private int totalEggsFound;
        private int prestigeLevel;
        private long chocolateThisPrestige;
        private Map<ChocolateRabbit, Integer> rabbitDuplicates = new HashMap<>();
        private Set<String> claimedSpawns = new HashSet<>();
        private Set<String> uniqueEggLocations = new HashSet<>();
        private int mythicPity;
        private int divinePity;
        private int hitmanSlots;
        private List<String> hitmanEggs = new ArrayList<>();
        private long hitmanQueueEnd;
        private String hitmanLastCheckedSpawn = "";
        private long hitmanLastEggFinder;
        private int goldenClickBonuses;
        private int goldenClickYear;
        private int elDoradoCatches;
        private long strayDueAt;
        private int hoppityShopYear;
        private List<String> hoppityStock = new ArrayList<>();
        private Set<String> hoppityPurchases = new HashSet<>();
        private int shopStockYear;
        private Map<String, Integer> shopPurchases = new HashMap<>();

        public void addChocolate(long amount) {
            if (amount <= 0) {
                this.chocolate += amount;
                return;
            }
            long room = Math.max(0, getMaxChocolate() - this.chocolate);
            long added = Math.min(amount, room);
            this.chocolate += added;
            this.chocolateAllTime += added;
            this.chocolateThisPrestige += added;
        }

        public boolean removeChocolate(long amount) {
            if (this.chocolate >= amount) {
                this.chocolate -= amount;
                return true;
            }
            return false;
        }

        public long getMaxChocolate() {
            return MAX_CHOCOLATE[Math.min(prestigeLevel, MAX_CHOCOLATE.length - 1)];
        }

        public int getClickPower() {
            return BASE_CLICK_POWER + handBakedChocolateLevel;
        }

        public int getBarnCapacity() {
            return BASE_BARN_CAPACITY + BARN_CAPACITY_PER_LEVEL * rabbitBarnLevel;
        }

        public boolean isBarnFull() {
            return foundRabbits.size() >= getBarnCapacity();
        }

        public int getEmployeeCap() {
            return BASE_EMPLOYEE_CAP + EMPLOYEE_CAP_PER_PRESTIGE * Math.min(prestigeLevel, MAX_PRESTIGE)
                    + countFound(ChocolateRabbit.RabbitRarity.DIVINE);
        }

        public int getEmployeeCap(EmployeeType type) {
            return getEmployeeCap() + (type.getIndex() <= 3 ? rabbitYouthLevels : 0);
        }

        public int countFound(ChocolateRabbit.RabbitRarity rarity) {
            int count = 0;
            for (ChocolateRabbit rabbit : foundRabbits) {
                if (rabbit.getRarity() == rarity) count++;
            }
            return count;
        }

        public double getPrestigeMultiplier() {
            return PRESTIGE_MULTIPLIERS[Math.min(prestigeLevel, PRESTIGE_MULTIPLIERS.length - 1)];
        }

        public long getNextPrestigeThreshold() {
            return prestigeLevel >= MAX_PRESTIGE ? 0L : PRESTIGE_THRESHOLDS[prestigeLevel];
        }

        public boolean canPrestige() {
            return prestigeLevel < MAX_PRESTIGE && chocolateThisPrestige >= PRESTIGE_THRESHOLDS[prestigeLevel];
        }

        public boolean prestige() {
            if (!canPrestige()) return false;
            prestigeLevel++;
            chocolateThisPrestige = 0;
            chocolate = 0;
            partialChocolate = 0;
            employees.clear();
            handBakedChocolateLevel = 0;
            timeTowerLevel = 0;
            timeTowerCharges = 0;
            timeTowerActiveUntil = 0;
            timeTowerChargeClock = 0;
            rabbitShrineLevel = 0;
            coachJackrabbitLevel = 0;
            return true;
        }

        public int getFactoryLevel() {
            return prestigeLevel + 1;
        }

        public double getTimeTowerBonus() {
            if (System.currentTimeMillis() >= timeTowerActiveUntil) return 0;
            double bonus = timeTowerLevel * TIME_TOWER_MULTIPLIER_PER_LEVEL;
            return foundRabbits.contains(ChocolateRabbit.MU) ? bonus + MU_TIME_TOWER_BONUS : bonus;
        }

        public boolean isTimeTowerActive() {
            return System.currentTimeMillis() < timeTowerActiveUntil;
        }

        public long getTimeTowerChargeMillis() {
            return foundRabbits.contains(ChocolateRabbit.EINSTEIN) ? TIME_TOWER_CHARGE_EINSTEIN_MS : TIME_TOWER_CHARGE_MS;
        }

        public void accrueTimeTowerCharges(long now) {
            if (prestigeLevel < 1) return;
            if (timeTowerCharges >= TIME_TOWER_MAX_CHARGES || timeTowerChargeClock <= 0) {
                if (timeTowerCharges >= TIME_TOWER_MAX_CHARGES || timeTowerChargeClock <= 0) timeTowerChargeClock = now;
                return;
            }
            long period = getTimeTowerChargeMillis();
            long gained = Math.max(0, now - timeTowerChargeClock) / period;
            if (gained <= 0) return;
            int added = (int) Math.min(gained, TIME_TOWER_MAX_CHARGES - timeTowerCharges);
            timeTowerCharges += added;
            timeTowerChargeClock = timeTowerCharges >= TIME_TOWER_MAX_CHARGES ? now : timeTowerChargeClock + gained * period;
        }

        public long getTimeTowerNextChargeMillis(long now) {
            if (timeTowerCharges >= TIME_TOWER_MAX_CHARGES || timeTowerChargeClock <= 0) return 0;
            return Math.max(0, timeTowerChargeClock + getTimeTowerChargeMillis() - now);
        }

        public boolean activateTimeTower() {
            if (timeTowerCharges > 0 && !isTimeTowerActive()) {
                long now = System.currentTimeMillis();
                if (timeTowerCharges >= TIME_TOWER_MAX_CHARGES) timeTowerChargeClock = now;
                timeTowerCharges--;
                timeTowerLastUsed = now;
                timeTowerActiveUntil = now + TIME_TOWER_DURATION_MS;
                totalTimeTowerUsages++;
                return true;
            }
            return false;
        }

        public double getCoachBonus() {
            return coachJackrabbitLevel * COACH_MULTIPLIER_PER_LEVEL;
        }

        public double getCollectionFlatProduction() {
            double flat = shardFlatProduction;
            for (ChocolateRabbit rabbit : foundRabbits) flat += rabbit.getChocolateBonus();
            if (foundRabbits.contains(ChocolateRabbit.DANTE)) flat += 50;
            if (foundRabbits.contains(ChocolateRabbit.ZEST_ZEPHYR)) flat += 30;
            if (foundRabbits.contains(ChocolateRabbit.SIGMA)) flat += 5 * countFound(ChocolateRabbit.RabbitRarity.MYTHIC);
            if (goldenClickYear == SkyBlockCalendar.getYear()) flat += GOLDEN_CLICK_CPS * goldenClickBonuses;
            return flat;
        }

        public double getCollectionMultiplierBonus() {
            double bonus = shardMultiplier;
            for (ChocolateRabbit rabbit : foundRabbits) bonus += rabbit.getMultiplierBonus();
            if (foundRabbits.contains(ChocolateRabbit.GALAXY)) bonus += 0.05;
            if (foundRabbits.contains(ChocolateRabbit.ZEST_ZEPHYR)) bonus += 0.03;
            return bonus;
        }

        public double getProductionMultiplier() {
            return getPrestigeMultiplier() + getCoachBonus() + getTimeTowerBonus() + getCollectionMultiplierBonus() + cookieMultiplier;
        }

        public double getEmployeeCostFactor() {
            return foundRabbits.contains(ChocolateRabbit.ZETA) ? 0.99 : 1.0;
        }

        public void setPetMultiplier(double petMultiplier) {
            this.petMultiplier.set(petMultiplier);
        }

        public double getChocolatePerSecond() {
            double baseProduction = getCollectionFlatProduction();
            for (EmployeeData employee : employees.values()) {
                baseProduction += employee.getProductionPerSecond();
            }
            return baseProduction * PetPerkMath.chocolateMultiplier(getProductionMultiplier(), petMultiplier.get());
        }

        public void updateChocolateFromProduction() {
            long now = System.currentTimeMillis();
            double secondsPassed = (now - lastUpdated) / MILLIS_PER_SECOND;

            if (secondsPassed > 0) {
                accrueTimeTowerCharges(now);
                partialChocolate += getChocolatePerSecond() * secondsPassed;

                long wholeChocolate = (long) partialChocolate;
                if (wholeChocolate > 0) {
                    addChocolate(wholeChocolate);
                    partialChocolate -= wholeChocolate;
                }
                if (chocolate >= getMaxChocolate()) partialChocolate = 0;

                lastUpdated = now;
            }
        }

        public void click() {
            addChocolate(getClickPower());
            totalClicks++;
        }

        public void setEmployee(EmployeeType type, int level) {
            employees.put(type, new EmployeeData(level, type.getBaseProductionPerLevel()));
        }

        public int getEmployeeCount() {
            return employees.size();
        }

        public void addFoundRabbit(ChocolateRabbit rabbit) {
            foundRabbits.add(rabbit);
        }

        public boolean hasFoundRabbit(ChocolateRabbit rabbit) {
            return foundRabbits.contains(rabbit);
        }

        public int getFoundRabbitCount() {
            return foundRabbits.size();
        }

        public int getDuplicates(ChocolateRabbit rabbit) {
            return rabbitDuplicates.getOrDefault(rabbit, 0);
        }

        public int getTotalDuplicates() {
            return rabbitDuplicates.values().stream().mapToInt(Integer::intValue).sum();
        }

        public void addDuplicate(ChocolateRabbit rabbit) {
            rabbitDuplicates.merge(rabbit, 1, Integer::sum);
        }

        public void resetShopStockIfNewYear(int year) {
            if (shopStockYear == year) return;
            shopStockYear = year;
            shopPurchases.clear();
        }

        public int shopPurchasedThisYear(String key, int year) {
            return shopStockYear == year ? shopPurchases.getOrDefault(key, 0) : 0;
        }

        public int shopRemaining(String key, int year) {
            return ChocolateShopStock.remaining(key, shopPurchasedThisYear(key, year));
        }

        public void recordShopPurchase(String key, int year) {
            resetShopStockIfNewYear(year);
            shopPurchases.merge(key, 1, Integer::sum);
        }

        public void addChocolateSpent(long amount) {
            this.totalChocolateSpent += amount;
        }

        public void claimEgg(HoppityEggType egg) {
            claimedEggs.add(egg);
            totalEggsFound++;
        }

        public boolean hasClaimedEgg(HoppityEggType egg) {
            return claimedEggs.contains(egg);
        }

        public void resetClaimedEggs() {
            claimedEggs.clear();
        }

        public boolean hasClaimedSpawn(String spawnId) {
            return claimedSpawns.contains(spawnId);
        }

        public int uniqueLocationsOn(String islandName) {
            String prefix = islandName + ":";
            return (int) uniqueEggLocations.stream().filter(key -> key.startsWith(prefix)).count();
        }
    }

    @Getter
    @AllArgsConstructor
    public enum EmployeeType {
        RABBIT_BRO(1, "Rabbit Bro", 1.0),
        RABBIT_COUSIN(2, "Rabbit Cousin", 2.0),
        RABBIT_SIS(3, "Rabbit Sis", 3.0),
        RABBIT_DADDY(4, "Rabbit Daddy", 4.0),
        RABBIT_GRANNY(5, "Rabbit Granny", 5.0),
        RABBIT_UNCLE(6, "Rabbit Uncle", 6.0),
        RABBIT_DOG(7, "Rabbit Dog", 7.0);

        private final int index;
        private final String name;
        private final double baseProductionPerLevel;

        public static java.util.Optional<EmployeeType> fromName(String name) {
            return java.util.Arrays.stream(values())
                    .filter(type -> type.name().equals(name) || type.name.equals(name))
                    .findFirst();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    // TODO: record
    public static class EmployeeData {
        private int level;
        private double baseProduction;

        public double getProductionPerSecond() {
            return baseProduction * level;
        }
    }
}
