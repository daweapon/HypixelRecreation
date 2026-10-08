package net.swofty.type.skyblockgeneric.data;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.LinkedField;
import net.swofty.PlayerField;
import net.swofty.codec.Codecs;
import net.swofty.commons.data.NameIndex;
import net.swofty.commons.data.SwoftyData;
import net.swofty.commons.skyblock.CoopLinks;
import net.swofty.commons.skyblock.PlayerShopData;
import net.swofty.commons.skyblock.SkyBlockPlayerProfiles;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.BackedField;
import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.data.Datapoint;
import net.swofty.type.generic.data.datapoints.*;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.generic.data.mongodb.UserDatabase;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.datapoints.*;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.potion.CookieBuffService;
import net.swofty.type.skyblockgeneric.gifts.BottleOfJyrre;
import net.swofty.type.skyblockgeneric.potion.GodPotionService;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.island.IslandPrivacy;
import net.swofty.type.skyblockgeneric.user.SkyBlockInventory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.trade.TradeOffer;
import net.swofty.type.skyblockgeneric.trade.TradeManager;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import tools.jackson.core.JacksonException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

// Per-profile SkyBlock data handler: loads/saves a player's profile document to/from its datapoints
// (coins, inventory, skills, minions, etc.) and defines every persisted SkyBlock data field.
public class SkyBlockDataHandler extends DataHandler {

    @Getter
    private UUID currentProfileId;

    protected SkyBlockDataHandler() {
        super();
    }

    protected SkyBlockDataHandler(UUID uuid, UUID profileId) {
        super(uuid);
        this.currentProfileId = profileId;
    }

    public static SkyBlockDataHandler getUser(UUID uuid) {
        return PlayerDataService.get(SkyBlockDomain.KEY, uuid);
    }

    public static @Nullable SkyBlockDataHandler getUser(HypixelPlayer player) {
        return PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
    }

    public static SkyBlockDataHandler createFromProfileOnly(Document profileDoc) {
        if (profileDoc == null) {
            throw new IllegalArgumentException("Cannot create SkyBlock data from a missing profile document");
        }
        UUID owner = UUID.fromString(profileDoc.getString("_owner"));
        UUID profileId = UUID.fromString(profileDoc.getString("_id"));
        SkyBlockDataHandler sb = new SkyBlockDataHandler(owner, profileId);
        sb.loadSkyBlock(profileDoc);
        return sb;
    }

    public static SkyBlockDataHandler createFromProfile(UUID playerUuid, UUID profileId, Document profileDoc) {
        SkyBlockDataHandler sb = new SkyBlockDataHandler(playerUuid, profileId);
        sb.loadSkyBlock(profileDoc);
        return sb;
    }

    private void loadSkyBlock(Document document) {
        if (document == null) {
            initSkyBlockDefaults();
            return;
        }
        this.uuid = UUID.fromString(document.getString("_owner"));
        this.currentProfileId = UUID.fromString(document.getString("_id"));

        for (Data data : Data.values()) {
            String key = data.getKey();
            if (!document.containsKey(key)) {
                datapoints.put(key, data.getDefaultDatapoint().deepClone().setUser(this).setData(data));
                continue;
            }
            String jsonValue = document.getString(key);
            try {
                Datapoint<?> dp = data.getDefaultDatapoint().getClass()
                        .getDeclaredConstructor(String.class).newInstance(key);
                dp.deserializeValue(jsonValue);
                datapoints.put(key, dp.setUser(this).setData(data));
            } catch (Exception e) {
                datapoints.put(key, data.getDefaultDatapoint().deepClone().setUser(this).setData(data));
                Logger.error(e, "Issue with SkyBlock datapoint {} for user {} - defaulting to default value", key, this.uuid);
            }
        }
    }

    private void initSkyBlockDefaults() {
        for (Data data : Data.values()) {
            try {
                datapoints.put(
                        data.getKey(),
                        data.getDefaultDatapoint().deepClone().setUser(this).setData(data)
                );
            } catch (Exception e) {
                Logger.error(e, "Issue with SkyBlock datapoint {} for user {} - requires fixing", data.getKey(), uuid);
            }
        }
    }

    public Document toProfileDocument() {
        Document document = new Document();
        document.put("_owner", this.uuid.toString());
        document.put("_id", currentProfileId.toString());

        for (Data data : Data.values()) {
            try {
                Datapoint<?> datapoint = getDatapoint(data.getKey());
                if (datapoint == null) {
                    datapoint = data.getDefaultDatapoint().deepClone().setUser(this).setData(data);
                    datapoints.put(data.getKey(), datapoint);
                }
                document.put(data.getKey(), datapoint.getSerializedValue());
            } catch (Exception e) {
                Logger.error(e, "Failed to serialize SkyBlock datapoint {} for user {}", data.getKey(), this.uuid);
            }
        }
        return document;
    }

    @Override
    public SkyBlockDataHandler fromDocument(Document document) {
        throw new UnsupportedOperationException("Use SkyBlockDataHandler.createFromProfile(...) instead");
    }

    @Override
    public Document toDocument() {
        return toProfileDocument();
    }

    public SkyBlockDatapoint<?> getSkyBlockDatapoint(String key) {
        return (SkyBlockDatapoint<?>) getDatapoint(key);
    }

    public static SkyBlockDataHandler getProfileOfOfflinePlayer(UUID uuid, UUID profileUUID) throws RuntimeException {
        SkyBlockDataHandler cached = PlayerDataService.find(SkyBlockDomain.KEY, uuid).orElse(null);
        if (cached != null) return cached;

        if (profileUUID == null)
            throw new RuntimeException("No profile selected for user " + uuid.toString());

        Document profile;
        try {
            profile = ProfilesDatabase.fetchDocument(profileUUID);
        } finally {
            releaseOfflineProfile(uuid, profileUUID);
        }
        return createFromProfileOnly(profile);
    }

    private static void releaseOfflineProfile(UUID uuid, UUID profileUUID) {
        if (PlayerDataService.isLoaded(SkyBlockDomain.KEY, uuid)) return;
        try {
            SwoftyData.profile().unload(profileUUID);
        } catch (Exception e) {
            Logger.error(e, "Failed to release offline profile container {} for user {}", profileUUID, uuid);
        }
    }

    /**
     * SB datapoint by enum (no generic param).
     */
    public Datapoint<?> get(Data datapoint) {
        Datapoint<?> dp = datapoints.get(datapoint.key);
        return dp != null ? dp : datapoint.defaultDatapoint;
    }

    /**
     * Optionally typed getter (casts to the class you pass).
     */
    public <R extends Datapoint<?>> R get(Data datapoint, Class<R> type) {
        Datapoint<?> dp = datapoints.get(datapoint.key);
        return (R) (dp != null ? type.cast(dp) : type.cast(datapoint.defaultDatapoint));
    }

    @Override
    public void runOnLoad(HypixelPlayer player) {
        for (Data data : Data.values()) {
            if (data.onLoad != null) data.onLoad.accept((SkyBlockPlayer) player, get(data));
        }
    }

    @SneakyThrows
    @Override
    public void runOnSave(HypixelPlayer player) {
        if (player instanceof SkyBlockPlayer sbp) {
            for (Data data : Data.values()) {
                if (data.onQuit != null) {
                    Datapoint<?> target = get(data);
                    Datapoint<?> produced = data.onQuit.apply(sbp);
                    target.setFrom(produced); // no onChange during save
                }
            }
        }
    }

    public static SkyBlockDataHandler initUserWithDefaultData(UUID uuid, UUID profileId) {
        SkyBlockDataHandler h = new SkyBlockDataHandler(uuid, profileId);
        h.initSkyBlockDefaults();
        return h;
    }

    public void loadFromApi() {
        if (!SwoftyData.profile().isLoaded(currentProfileId)) SwoftyData.profile().load(currentProfileId);
        loadBackedData();
    }

    public void loadFromTransferDocument(Document document) {
        loadSkyBlock(document);
        if (!SwoftyData.profile().isLoaded(currentProfileId)) SwoftyData.profile().load(currentProfileId);
        readCoopBackedData();
        persistProfileDocument(SwoftyData.profile(), currentProfileId, toProfileDocument());
    }

    private void readCoopBackedData() {
        for (Datapoint<?> datapoint : datapoints.values()) {
            if (!isCoopBacked(datapoint)) continue;
            try {
                String stored = ((Data) datapoint.getData()).readData(this);
                if (stored != null) datapoint.deserializeValue(stored);
            } catch (Exception e) {
                Logger.error(e, "Failed to read coop datapoint {} for user {}", datapoint.getKey(), this.uuid);
            }
        }
    }

    private static int freeInventorySlot(SkyBlockInventory inventory) {
        for (int slot = 0; slot < 36; slot++) {
            if (slot != 8 && !inventory.getItems().containsKey(slot)) return slot;
        }
        return -1;
    }

    private static boolean isCoopBacked(Datapoint<?> datapoint) {
        return datapoint.getData() instanceof Data data && data.coopField() != null;
    }

    public void saveToApi() {
        saveBackedData(SkyBlockDataHandler::isCoopBacked);
        Document document = toProfileDocument();
        persistProfileDocument(SwoftyData.profile(), currentProfileId, document);
    }

    public boolean commitAuctionClaim(UUID receipt, SkyBlockPlayer player, Runnable reward) {
        saveBackedData(SkyBlockDataHandler::isCoopBacked);
        return SwoftyData.profile().transaction(currentProfileId, transaction -> {
            String existing = transaction.get(Data.AUCTION_DELIVERED_CLAIMS.profileField());
            if (existing != null && Arrays.asList(existing.split(",")).contains(receipt.toString())) return false;
            reward.run();
            runOnSave(player);
            writeProfileDocument(transaction, toProfileDocument());
            return true;
        });
    }

    public void commitAuctionOperation(DatapointAuctionOperations.State expected, SkyBlockPlayer player, Runnable change) {
        saveBackedData(SkyBlockDataHandler::isCoopBacked);
        SwoftyData.profile().transaction(currentProfileId, transaction -> {
            var saved = DatapointAuctionOperations.read(transaction.get(Data.AUCTION_OPERATIONS.profileField()));
            if (!saved.equals(expected)) throw new IllegalStateException("Auction operation state changed; reload the profile");
            change.run();
            runOnSave(player);
            writeProfileDocument(transaction, toProfileDocument());
        });
    }

    static void persistProfileDocument(net.swofty.DataAPI api, UUID profile, Document document) {
        validateProfileDocument(document);
        api.transaction(profile, transaction -> { writeProfileDocument(transaction, document); });
    }

    private static void validateProfileDocument(Document document) {
        for (Data field : new Data[]{Data.COINS, Data.INVENTORY, Data.AUCTION_DELIVERED_CLAIMS, Data.AUCTION_OPERATIONS,
                Data.AUCTION_ACTIVE_OWNED, Data.AUCTION_INACTIVE_OWNED, Data.AUCTION_ACTIVE_BIDS, Data.AUCTION_INACTIVE_BIDS}) {
            if (!document.containsKey(field.getKey())) throw new IllegalStateException("Incomplete profile save: " + field);
        }
    }

    private static void writeProfileDocument(net.swofty.transaction.Transaction transaction, Document document) {
        validateProfileDocument(document);
        String savedClaims = transaction.get(Data.AUCTION_DELIVERED_CLAIMS.profileField());
        if (savedClaims != null && !savedClaims.isEmpty()) {
            var currentClaims = Arrays.asList(document.getString(Data.AUCTION_DELIVERED_CLAIMS.getKey()).split(","));
            if (!currentClaims.containsAll(Arrays.asList(savedClaims.split(",")))) {
                throw new IllegalStateException("Refusing to overwrite newer auction claim receipts");
            }
        }
        var savedOperations = DatapointAuctionOperations.read(transaction.get(Data.AUCTION_OPERATIONS.profileField()));
        var operations = DatapointAuctionOperations.read(document.getString(Data.AUCTION_OPERATIONS.getKey()));
        if (operations.revision() < savedOperations.revision()
                || operations.revision() == savedOperations.revision() && !operations.equals(savedOperations)) {
            throw new IllegalStateException("Refusing to overwrite newer auction operations");
        }
        for (Data field : Data.values()) {
            if (field.coopField() == null && document.containsKey(field.getKey())) {
                transaction.set(field.profileField(), document.getString(field.getKey()));
            }
        }
        transaction.set(ProfilesDatabase.DOCUMENT, document.toJson());
    }

    public Map<String, Object> getCoopValues() {
        Map<String, Object> values = new HashMap<>();
        Arrays.stream(Data.values()).forEach(data -> {
            if (data.isCoopPersistent) {
                values.put(data.getKey(), getDatapoint(data.getKey()).getValue());
            }
        });
        return values;
    }

    public static @Nullable UUID getPotentialUUIDFromName(String name) throws RuntimeException {
        return NameIndex.lookup(name);
    }

    public enum Data implements BackedField {
        EXPERIENCED_STATISTICS("experienced_statistics", false, false, false,
                DatapointStringList.class, new DatapointStringList("experienced_statistics")),
        PROFILE_NAME("profile_name", false, true, false,
                DatapointString.class, new DatapointString("profile_name", "null"),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    if (Objects.equals(datapoint.getValue(), "null")) {
                        ((DatapointString) datapoint).setValue(SkyBlockPlayerProfiles.getRandomName());
                    }
                }),

        PROFILE_MODE("profile_mode", false, true, false,
                DatapointString.class, new DatapointString("profile_mode", "CLASSIC")),

        COINS("coins", false, false, false,
                DatapointDouble.class, new DatapointDouble("coins", 0.0)),

        INVENTORY("inventory", false, false, false,
                DatapointInventory.class, new DatapointInventory("inventory", new SkyBlockInventory()),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    SkyBlockInventory inv = (SkyBlockInventory) datapoint.getValue();

                    player.setHelmet(new SkyBlockItem(inv.getHelmet()).getItemStack());
                    player.setChestplate(new SkyBlockItem(inv.getChestplate()).getItemStack());
                    player.setLeggings(new SkyBlockItem(inv.getLeggings()).getItemStack());
                    player.setBoots(new SkyBlockItem(inv.getBoots()).getItemStack());

                    List<SkyBlockItem> displaced = new ArrayList<>();
                    inv.getItems().forEach((slot, item) -> {
                        SkyBlockItem loaded = new SkyBlockItem(item);
                        if (loaded.getItemType() == ItemType.SKYBLOCK_MENU) return;
                        if (slot == 8) {
                            displaced.add(loaded);
                            return;
                        }
                        PlayerItemOrigin origin = PlayerItemOrigin.INVENTORY_SLOT;
                        origin.setData(slot);

                        ItemStack updated = PlayerItemUpdater.playerUpdate(
                                player,
                                loaded.getItemStack(),
                                true
                        ).build();

                        origin.setStack(player, updated);
                    });

                    player.getInventory().setItemStack(8,
                            new NonPlayerItemUpdater(new SkyBlockItem(ItemType.SKYBLOCK_MENU).getItemStack())
                                    .getUpdatedItem().build());
                    displaced.forEach(player::addAndUpdateItem);
                },
                (player) -> {
                    SkyBlockInventory inv = new SkyBlockInventory();
                    ItemStack helmet = player.getHelmet();
                    if (SkyBlockItem.isSkyBlockItem(helmet)) inv.setHelmet(new SkyBlockItem(helmet).toUnderstandable());
                    ItemStack chestplate = player.getChestplate();
                    if (SkyBlockItem.isSkyBlockItem(chestplate))
                        inv.setChestplate(new SkyBlockItem(chestplate).toUnderstandable());
                    ItemStack leggings = player.getLeggings();
                    if (SkyBlockItem.isSkyBlockItem(leggings))
                        inv.setLeggings(new SkyBlockItem(leggings).toUnderstandable());
                    ItemStack boots = player.getBoots();
                    if (SkyBlockItem.isSkyBlockItem(boots)) inv.setBoots(new SkyBlockItem(boots).toUnderstandable());

                    for (int i = 0; i < 36; i++) {
                        ItemStack stack = player.getInventory().getItemStack(i);
                        if (SkyBlockItem.isSkyBlockItem(stack)) {
                            inv.getItems().put(i, new SkyBlockItem(stack).toUnderstandable());
                        }
                    }
                    for (TradeOffer.HeldItem held : TradeManager.heldItems(player)) {
                        int slot = inv.getItems().containsKey(held.homeSlot()) ? freeInventorySlot(inv) : held.homeSlot();
                        if (slot >= 0) inv.getItems().put(slot, new SkyBlockItem(held.stack()).toUnderstandable());
                    }
                    return new DatapointInventory("inventory", inv);
                }),

        SKILLS("skills", false, false, false,
                DatapointSkills.class, new DatapointSkills("skills")),

        EXPERIENCE("experience", false, false, false,
                DatapointLong.class, new DatapointLong("experience", 0L),
                (player, datapoint) -> {
                },
                (player, datapoint) -> player.setExperience((Long) datapoint.getValue()),
                (player) -> new DatapointLong("experience", player.getExperience())),

        COMPLETED_BAZAAR_TRANSACTIONS("completed_bazaar_transactions", false, false, false,
                DatapointCompletedBazaarTransactions.class, new DatapointCompletedBazaarTransactions("completed_bazaar_transactions")),

        MISSION_DATA("mission_data", false, false, false,
                DatapointMissionData.class, new DatapointMissionData("mission_data", new MissionData()),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    MissionData data = (MissionData) datapoint.getValue();
                    data.setSkyBlockPlayer(player);
                    ((DatapointMissionData) datapoint).setValue(data);
                }),

        SHOPPING_DATA("shopping_data", false, false, true,
                DatapointShopData.class, new DatapointShopData("shopping_data", new PlayerShopData()),
                (player, datapoint) -> {
                }),

        FAIRY_SOULS("player_fairy_souls", false, false, false,
                DatapointFairySouls.class, new DatapointFairySouls("player_fairy_souls")),

        CREATED("created", false, true, false,
                DatapointLong.class, new DatapointLong("created", 0L),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    if (Objects.equals(datapoint.getValue(), 0L)) {
                        ((DatapointLong) datapoint).setValue(System.currentTimeMillis());
                    }
                }),

        LAST_EDITED_SKILL("last_edited_skill", false, false, false,
                DatapointSkillCategory.class, new DatapointSkillCategory("last_edited_skill", SkillCategories.FORAGING)),

        ISLAND_UUID("island_uuid", false, true, false,
                DatapointUUID.class, new DatapointUUID("island_uuid", null),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    if (player.getSkyBlockIsland() != null) {
                        ((DatapointUUID) datapoint).setValue(player.getSkyBlockIsland().getIslandID());
                    }
                }),

        IS_COOP("is_coop", false, true, false,
                DatapointBoolean.class, new DatapointBoolean("is_coop", false)),

        // Island-wide visitation privacy tier (PUBLIC/FRIENDS_ONLY/PRIVATE) - coop-shared like
        // ISLAND_UUID/IS_COOP since it describes the island itself, not any one member.
        ISLAND_PRIVACY("island_privacy", false, true, false,
                DatapointString.class, new DatapointString("island_privacy", IslandPrivacy.PUBLIC.name())),

        // Players (by UUID string) blocked from /visit-ing this island regardless of privacy tier -
        // island-wide like ISLAND_PRIVACY, set via /ignore.
        IGNORED_VISITORS("ignored_visitors", false, true, false,
                DatapointStringList.class, new DatapointStringList("ignored_visitors")),

        COLLECTION("collection", false, true, false,
                DatapointCollection.class, new DatapointCollection("collection")),

        HUNTING("hunting", false, false, false,
                DatapointHunting.class, new DatapointHunting("hunting")),

        FOREST_ESSENCE("forest_essence", false, false, false,
                DatapointLong.class, new DatapointLong("forest_essence", 0L)),

        FOREST_WHISPERS("forest_whispers", false, false, false,
                DatapointLong.class, new DatapointLong("forest_whispers", 0L)),

        HOTF_EXPERIENCE("hotf_experience", false, false, false,
                DatapointLong.class, new DatapointLong("hotf_experience", 0L)),

        MINION_DATA("minions", false, true, false,
                DatapointMinionData.class, new DatapointMinionData("minions")),

        STORAGE("storage", false, false, false,
                DatapointStorage.class, new DatapointStorage("storage")),

        WARDROBE("wardrobe", false, false, false,
                DatapointWardrobe.class, new DatapointWardrobe("wardrobe")),

        LOADOUTS("loadouts", false, false, false,
                DatapointLoadouts.class, new DatapointLoadouts("loadouts")),

        BACKPACKS("backpacks", false, false, false,
                DatapointBackpacks.class, new DatapointBackpacks("backpacks")),

        VISITED_REGIONS("visited_regions", false, false, false,
                DatapointStringList.class, new DatapointStringList("visited_regions")),

        AUCTION_STATISTICS("auction_statistics", false, false, false,
                DatapointAuctionStatistics.class, new DatapointAuctionStatistics("auction_statistics")),

        AUCTION_ACTIVE_BIDS("auction_bids", false, false, false,
                DatapointUUIDList.class, new DatapointUUIDList("auction_active_bids")),

        AUCTION_INACTIVE_BIDS("auction_inactive_bids", false, false, false,
                DatapointUUIDList.class, new DatapointUUIDList("auction_inactive_bids")),

        AUCTION_ACTIVE_OWNED("auction_owned", false, false, false,
                DatapointUUIDList.class, new DatapointUUIDList("auction_active_owned")),

        AUCTION_INACTIVE_OWNED("auction_inactive_owned", false, false, false,
                DatapointUUIDList.class, new DatapointUUIDList("auction_inactive_owned")),

        AUCTION_DELIVERED_CLAIMS("auction_delivered_claims", false, false, false,
                DatapointUUIDList.class, new DatapointUUIDList("auction_delivered_claims")),

        AUCTION_OPERATIONS("auction_operations", false, false, false,
                DatapointAuctionOperations.class, new DatapointAuctionOperations("auction_operations")),

        AUCTION_ESCROW("auction_escrow", false, false, false,
                DatapointAuctionEscrow.class, new DatapointAuctionEscrow("auction_escrow")),

        BANK_DATA("bank_data", false, true, false,
                DatapointBankData.class, new DatapointBankData("bank_data"),
                (player, datapoint) -> {
                },
                (player, datapoint) -> Thread.startVirtualThread(() ->
                        ((DatapointBankData) datapoint).setValue(((DatapointBankData) datapoint).getValue())
                )),

        PET_DATA("pet_data", false, false, false,
                DatapointPetData.class, new DatapointPetData("pet_data")),

        QUIVER("quiver", false, false, false,
                DatapointQuiver.class, new DatapointQuiver("quiver")),

        SHIP_STATE("ship_state", false, false, false,
                DatapointShipState.class, new DatapointShipState("ship_state")),

        TROPHY_FISH("trophy_fish", false, false, false,
                DatapointTrophyFish.class, new DatapointTrophyFish("trophy_fish")),

        SLAYER("slayer", false, false, false,
                DatapointSlayer.class, new DatapointSlayer("slayer")),

        CITY_PROJECT_CONTRIBUTIONS("city_project_contributions", false, false, false, DatapointMapStringLong.class,
                new DatapointMapStringLong("city_project_contributions")),

        RACE_BEST_TIME("race_best_time", false, false, false, DatapointMapStringLong.class,
                new DatapointMapStringLong("race_best_time")),

        ACCESSORY_BAG("accessory_bag", false, false, false,
                DatapointAccessoryBag.class, new DatapointAccessoryBag("accessory_bag")),

        SACK_OF_SACKS("sack_of_sacks", false, false, false,
                DatapointSackOfSacks.class, new DatapointSackOfSacks("sack_of_sacks")),

        ITEMS_IN_SACKS("items_in_sacks", false, false, false,
                DatapointItemsInSacks.class, new DatapointItemsInSacks("items_in_sacks")),

        BESTIARY("bestiary", false, false, false,
                DatapointBestiary.class, new DatapointBestiary("bestiary")),

        DEATHS("deaths", false, false, false,
                DatapointDeaths.class, new DatapointDeaths("deaths")),

        ARCHERY_PRACTICE("archery_practice", false, false, false,
                DatapointArcheryPractice.class, new DatapointArcheryPractice("archery_practice")),

        SKYBLOCK_EXPERIENCE("skyblock_experience", false, false, false,
                DatapointSkyBlockExperience.class, new DatapointSkyBlockExperience("skyblock_experience")),

        BITS("bits", false, false, false,
                DatapointInteger.class, new DatapointInteger("bits", 0)),

        BITS_AVAILABLE("bits_available", false, false, false,
                DatapointInteger.class, new DatapointInteger("bits_available", 0)),

        BITS_POOL_SIZE("bits_pool_size", false, false, false,
                DatapointInteger.class, new DatapointInteger("bits_pool_size", 0)),

        BITS_LAST_FARMED("bits_last_farmed", false, false, false,
                DatapointLong.class, new DatapointLong("bits_last_farmed", 0L)),

        STAT_OVERRIDES("stat_overrides", false, false, false,
                DatapointString.class, new DatapointString("stat_overrides", "")),

        GEMS("gems", false, false, false,
                DatapointInteger.class, new DatapointInteger("gems", 0)),

        FAME("fame", false, false, false,
                DatapointInteger.class, new DatapointInteger("fame", 0)),

        BRAIN_FOOD_CONSUMED("brain_food_consumed", false, false, false,
                DatapointInteger.class, new DatapointInteger("brain_food_consumed", 0)),

        FORCED_DROPS("forced_drops", false, false, false,
                DatapointStringList.class, new DatapointStringList("forced_drops")),

        MUSEUM_DATA("museum_data", false, false, false,
                DatapointMuseum.class, new DatapointMuseum("museum_data"),
                (player, datapoint) -> {
                },
                (player, datapoint) -> {
                    DatapointMuseum.MuseumData data = (DatapointMuseum.MuseumData) datapoint.getValue();
                    data.setCurrentlyViewing(new DatapointMuseum.ViewingInfo(
                            player.getUuid(),
                            player.getProfiles().getCurrentlySelected()
                    ));
                }),

        BUILD_MODE("build_mode", true, false, false, DatapointBoolean.class,
                new DatapointBoolean("build_mode", false),
                (player, datapoint) -> ((SkyBlockPlayer) player).setBypassBuild((Boolean) datapoint.getValue()),
                (player, datapoint) -> player.setBypassBuild((Boolean) datapoint.getValue()),
                (player) -> new DatapointBoolean("build_mode", player.isBypassBuild())),

        SKIN_SIGNATURE("skin_signature", false, false, false,
                DatapointString.class, new DatapointString("skin_signature", "null"),
                (player, datapoint) -> {
                },
                (player, datapoint) -> ((DatapointString) datapoint).setValue(player.getSkin().signature())),

        SKIN_TEXTURE("skin_texture", false, false, false,
                DatapointString.class, new DatapointString("skin_texture", "null"),
                (player, datapoint) -> {
                },
                (player, datapoint) -> ((DatapointString) datapoint).setValue(player.getSkin().textures())),

        VISITED_ISLANDS("visited_islands", false, false, false,
                DatapointStringList.class, new DatapointStringList("visited_islands")),

        USED_SCROLLS("used_scrolls", false, false, false,
                DatapointStringList.class, new DatapointStringList("used_scrolls")),

        BOOSTER_COOKIE_EXPIRATION_DATE("booster_cookie_expiration_date", false, false, false,
                DatapointLong.class, new DatapointLong("booster_cookie_expiration_date", 1L),
                (player, datapoint) -> CookieBuffService.restore((SkyBlockPlayer) player),
                (player, datapoint) -> CookieBuffService.restore(player)),

        GOD_POTION_REMAINING_MS("god_potion_remaining_ms", false, false, false,
                DatapointLong.class, new DatapointLong("god_potion_remaining_ms", 0L),
                null,
                (player, datapoint) -> GodPotionService.restore(player)),

        GOD_POTION_LAST_UPDATED("god_potion_last_updated", false, false, false,
                DatapointLong.class, new DatapointLong("god_potion_last_updated", 0L)),

        TRIAL_OF_FIRE_LEVEL("trial_of_fire_level", false, false, false,
                DatapointInteger.class, new DatapointInteger("trial_of_fire_level", 0)),

        DARK_AUCTION_PURCHASES("dark_auction_purchases", false, false, false,
                DatapointInteger.class, new DatapointInteger("dark_auction_purchases", 0)),

        LUCIUS_TIERS_PURCHASED("lucius_tiers_purchased", false, false, false,
                DatapointInteger.class, new DatapointInteger("lucius_tiers_purchased", 0)),

        PET_TRAINING("pet_training", false, false, false,
                DatapointPetTraining.class, new DatapointPetTraining("pet_training")),

        RIFT_PRISM_IMBUED("rift_prism_imbued", false, false, false,
                DatapointBoolean.class, new DatapointBoolean("rift_prism_imbued", false)),

        LATEST_NEW_YEAR_CAKE_YEAR("latest_new_year_cake_year", false, false, false,
                DatapointInteger.class, new DatapointInteger("latest_new_year_cake_year", 0)),

        SPOOKY_FESTIVAL_CLAIMED_TIER("spooky_festival_claimed_tier", false, false, false,
                DatapointInteger.class, new DatapointInteger("spooky_festival_claimed_tier", 0)),

        SPOOKY_FESTIVAL_CLAIMED_YEAR("spooky_festival_claimed_year", false, false, false,
                DatapointInteger.class, new DatapointInteger("spooky_festival_claimed_year", 0)),

        SPOOKY_FESTIVAL_CANDY_YEAR("spooky_festival_candy_year", false, false, false,
                DatapointInteger.class, new DatapointInteger("spooky_festival_candy_year", 0)),

        SPOOKY_FESTIVAL_GREEN_CANDY("spooky_festival_green_candy", false, false, false,
                DatapointInteger.class, new DatapointInteger("spooky_festival_green_candy", 0)),

        SPOOKY_FESTIVAL_PURPLE_CANDY("spooky_festival_purple_candy", false, false, false,
                DatapointInteger.class, new DatapointInteger("spooky_festival_purple_candy", 0)),

        NORTH_STARS("north_stars", false, false, false,
                DatapointInteger.class, new DatapointInteger("north_stars", 0)),

        ESSENCE("essence", false, false, false, DatapointMapStringLong.class,
                new DatapointMapStringLong("essence")),

        ESSENCE_PERKS("essence_perks", false, false, false, DatapointMapStringLong.class,
                new DatapointMapStringLong("essence_perks")),

        CHICKEN_RACE_CLUCKPOINT("chicken_race_cluckpoint", false, false, false,
                DatapointInteger.class, new DatapointInteger("chicken_race_cluckpoint", 0)),

        GIFTS_GIVEN("gifts_given", false, false, false,
                DatapointInteger.class, new DatapointInteger("gifts_given", 0)),

        JYRRE_BUFF_EXPIRATION("jyrre_buff_expiration", false, false, false,
                DatapointLong.class, new DatapointLong("jyrre_buff_expiration", 0L),
                null,
                (player, datapoint) -> BottleOfJyrre.restore(player)),

        JYRRE_BUFF_INTELLIGENCE("jyrre_buff_intelligence", false, false, false,
                DatapointInteger.class, new DatapointInteger("jyrre_buff_intelligence", 0)),

        REFINED_JYRRE_CONSUMED("refined_jyrre_consumed", false, false, false,
                DatapointInteger.class, new DatapointInteger("refined_jyrre_consumed", 0)),

        ST_JERRY_GREEN_GIFT_YEAR("st_jerry_green_gift_year", false, false, false,
                DatapointInteger.class, new DatapointInteger("st_jerry_green_gift_year", 0)),

        LATEST_YEAR_PRESENT_PICKUP("latest_year_pickup_present", false, false, false,
                DatapointPresentYear.class, new DatapointPresentYear("latest_year_pickup_present")),

        CHOCOLATE_FACTORY("chocolate_factory", false, false, false,
                DatapointChocolateFactory.class, new DatapointChocolateFactory("chocolate_factory")),

        SOULFLOW("soulflow", false, false, false,
                DatapointInteger.class, new DatapointInteger("soulflow", 0)),

        COMMISSIONS_COMPLETED("commissions_completed", false, false, false,
                DatapointInteger.class, new DatapointInteger("commissions_completed", 0)),

        COMMISSIONS("commissions", false, false, false,
                DatapointCommissions.class, new DatapointCommissions("commissions")),

        HOTM("hotm", false, false, false,
                DatapointHOTM.class, new DatapointHOTM("hotm")),

        KAT("kat", false, false, false,
                DatapointKat.class, new DatapointKat("kat")),

        STASH("stash", false, false, false,
                DatapointStash.class, new DatapointStash("stash")),

        COLLECTED_MOB_TYPE_REWARDS("collected_mob_type_rewards", false, false, false,
                DatapointCollectedMobTypeRewards.class, new DatapointCollectedMobTypeRewards("collected_mob_type_rewards")),

        FARMING_CONTESTS("farming_contests", false, false, false,
                DatapointFarmingContests.class, new DatapointFarmingContests("farming_contests")),

        CARNIVAL("carnival", false, false, false,
                DatapointCarnival.class, new DatapointCarnival("carnival")),

        JACOBUS_UPGRADES("jacobus_upgrades", false, false, false,
                DatapointInteger.class, new DatapointInteger("jacobus_upgrades", 0)),

        TRADE_COINS_SENT("trade_coins_sent", false, false, false,
                DatapointString.class, new DatapointString("trade_coins_sent", "")),

        STONKS_AUCTIONS("stonks_auctions", false, false, false,
                DatapointStonksAuctions.class, new DatapointStonksAuctions("stonks_auctions")),

        SHEN_AUCTIONS("shen_auctions", false, false, false,
                DatapointShenAuctions.class, new DatapointShenAuctions("shen_auctions")),

        CRIMSON_ISLE("crimson_isle", false, false, false,
                DatapointCrimsonIsle.class, new DatapointCrimsonIsle("crimson_isle")),

        ACCESSORY_POWERS("accessory_powers", false, false, false,
                DatapointAccessoryPowers.class, new DatapointAccessoryPowers("accessory_powers")),
        ;

        @Getter
        private final String key;
        @Getter
        private final Boolean isProfilePersistent;
        @Getter
        private final Boolean isCoopPersistent;
        @Getter
        private final Boolean repeatSetValue;
        @Getter
        private final Class<? extends Datapoint<?>> type;
        @Getter
        private final Datapoint<?> defaultDatapoint;
        private final PlayerField<String> profileField;
        private final LinkedField<UUID, String> coopField;
        public final BiConsumer<Player, Datapoint<?>> onChange;
        public final BiConsumer<SkyBlockPlayer, Datapoint<?>> onLoad;
        public final Function<SkyBlockPlayer, Datapoint<?>> onQuit;

        Data(String key, Boolean isProfilePersistent, Boolean isCoopPersistent, Boolean repeatSetValue,
             Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint,
             BiConsumer<Player, Datapoint<?>> onChange, BiConsumer<SkyBlockPlayer, Datapoint<?>> onLoad, Function<SkyBlockPlayer, Datapoint<?>> onQuit) {
            this.key = key;
            this.isProfilePersistent = isProfilePersistent;
            this.isCoopPersistent = isCoopPersistent;
            this.repeatSetValue = repeatSetValue;
            this.type = type;
            this.defaultDatapoint = defaultDatapoint;
            this.onChange = onChange;
            this.onLoad = onLoad;
            this.onQuit = onQuit;
            if (Boolean.TRUE.equals(isCoopPersistent)) {
                this.coopField = LinkedField.create("coop", key, Codecs.STRING, null, CoopLinks.COOP);
                this.profileField = null;
            } else {
                this.profileField = PlayerField.create("skyblock", key, Codecs.STRING, null);
                this.coopField = null;
            }
        }

        @Override
        public String readData(DataHandler handler) {
            UUID profileId = ((SkyBlockDataHandler) handler).getCurrentProfileId();
            return coopField != null
                    ? SwoftyData.profile().get(profileId, coopField)
                    : SwoftyData.profile().get(profileId, profileField);
        }

        @Override
        public void writeData(DataHandler handler, String serialized) {
            UUID profileId = ((SkyBlockDataHandler) handler).getCurrentProfileId();
            if (coopField != null) {
                SwoftyData.profile().set(profileId, coopField, serialized);
            } else {
                SwoftyData.profile().set(profileId, profileField, serialized);
            }
        }

        public LinkedField<UUID, String> coopField() {
            return coopField;
        }

        public PlayerField<String> profileField() {
            return profileField;
        }

        Data(String key, Boolean isProfilePersistent, Boolean isCoopPersistent, Boolean repeatSetValue,
             Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint,
             BiConsumer<Player, Datapoint<?>> onChange, BiConsumer<SkyBlockPlayer, Datapoint<?>> onLoad) {
            this(key, isProfilePersistent, isCoopPersistent, repeatSetValue, type, defaultDatapoint, onChange, onLoad, null);
        }

        Data(String key, Boolean isProfilePersistent, Boolean isCoopPersistent, Boolean repeatSetValue,
             Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint, BiConsumer<Player, Datapoint<?>> onChange) {
            this(key, isProfilePersistent, isCoopPersistent, repeatSetValue, type, defaultDatapoint, onChange, null, null);
        }

        Data(String key, Boolean isProfilePersistent, Boolean isCoopPersistent, Boolean repeatSetValue,
             Class<? extends Datapoint<?>> type, Datapoint<?> defaultDatapoint) {
            this(key, isProfilePersistent, isCoopPersistent, repeatSetValue, type, defaultDatapoint, null, null, null);
        }

        public static Data fromKey(String key) {
            for (Data data : values()) if (data.getKey().equals(key)) return data;
            return null;
        }
    }

    public static void startRepeatSetValueLoop() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                SkyBlockDataHandler h = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
                if (h == null) return;
                for (Data data : Data.values()) {
                    if (!data.repeatSetValue) continue;
                    try {
                        Datapoint<?> dp = h.get(data);
                        @SuppressWarnings("unchecked")
                        Datapoint<Object> any = (Datapoint<Object>) dp;
                        any.setValue(any.getValue());
                    } catch (Exception e) {
                        Logger.error(e, "Failed to repeat set SkyBlock datapoint {} for user {}",
                                data.getKey(), player.getUuid());
                    }
                }
            });
            return TaskSchedule.nextTick();
        });
    }
}
