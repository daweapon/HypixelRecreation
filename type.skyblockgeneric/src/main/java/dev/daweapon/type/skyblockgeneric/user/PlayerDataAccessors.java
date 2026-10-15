package dev.daweapon.type.skyblockgeneric.user;

import dev.daweapon.commons.skyblock.PlayerShopData;
import dev.daweapon.type.generic.data.datapoints.DatapointBoolean;
import dev.daweapon.type.generic.data.datapoints.DatapointDouble;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.*;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;

class PlayerDataAccessors {
    private final SkyBlockPlayer player;

    PlayerDataAccessors(SkyBlockPlayer player) {
        this.player = player;
    }

    DatapointMuseum.MuseumData getMuseumData() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MUSEUM_DATA, DatapointMuseum.class).getValue();
    }

    void setMuseumData(DatapointMuseum.MuseumData data) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MUSEUM_DATA, DatapointMuseum.class).setValue(data);
    }

    DatapointPetData.UserPetData getPetData() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.PET_DATA, DatapointPetData.class).getValue();
    }

    MissionData getMissionData() {
        MissionData data = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MISSION_DATA, DatapointMissionData.class).getValue();
        data.setSkyBlockPlayer(player);
        return data;
    }

    DatapointArcheryPractice.ArcheryPracticeData getArcheryPracticeData() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ARCHERY_PRACTICE, DatapointArcheryPractice.class).getValue();
    }

    PlayerShopData getShoppingData() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SHOPPING_DATA, DatapointShopData.class).getValue();
    }

    DatapointCollection.PlayerCollection getCollection() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COLLECTION, DatapointCollection.class).getValue();
    }

    DatapointHunting.HuntingData getHuntingData() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HUNTING, DatapointHunting.class).getValue();
    }

    DatapointSkills.PlayerSkills getSkills() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SKILLS, DatapointSkills.class).getValue().attach(player);
    }

    boolean isCoop() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.IS_COOP, DatapointBoolean.class).getValue();
    }

    ProfileMode getProfileMode() {
        return ProfileMode.fromStored(player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.PROFILE_MODE, dev.daweapon.type.generic.data.datapoints.DatapointString.class)
                .getValue());
    }

    boolean isIronman() {
        return getProfileMode() == ProfileMode.IRONMAN;
    }

    CoopDatabase.Coop getCoop() {
        return CoopDatabase.getFromMember(player.getUuid());
    }

    DatapointQuiver.PlayerQuiver getQuiver() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.QUIVER, DatapointQuiver.class).getValue();
    }

    DatapointShipState.ShipState getShipState() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SHIP_STATE, DatapointShipState.class).getValue();
    }

    DatapointTrophyFish.TrophyFishData getTrophyFishData() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.TROPHY_FISH, DatapointTrophyFish.class).getValue();
    }

    DatapointSlayer.SlayerData getSlayerData() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SLAYER, DatapointSlayer.class).getValue();
    }

    DatapointAccessoryBag.PlayerAccessoryBag getAccessoryBag() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ACCESSORY_BAG, DatapointAccessoryBag.class).getValue();
    }

    DatapointSackOfSacks.PlayerSackOfSacks getSackOfSacks() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SACK_OF_SACKS, DatapointSackOfSacks.class).getValue();
    }

    DatapointItemsInSacks.PlayerItemsInSacks getSackItems() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ITEMS_IN_SACKS, DatapointItemsInSacks.class).getValue();
    }

    DatapointStash.PlayerStash getStash() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.STASH, DatapointStash.class).getValue();
    }

    DatapointFairySouls.PlayerFairySouls getFairySouls() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FAIRY_SOULS, DatapointFairySouls.class).getValue();
    }

    DatapointSkyBlockExperience.PlayerSkyBlockExperience getSkyBlockExperience() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SKYBLOCK_EXPERIENCE,
                DatapointSkyBlockExperience.class
        ).getValue();
        experience.setAttachedPlayer(player);
        return experience;
    }

    DatapointKat.PlayerKat getKatData() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.KAT, DatapointKat.class).getValue();
    }

    DatapointBestiary.PlayerBestiary getBestiaryData() {
        DatapointBestiary.PlayerBestiary bestiary = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class).getValue();
        bestiary.setAttachedPlayer(player);
        return bestiary;
    }

    DatapointDeaths.PlayerDeaths getDeathData() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.DEATHS, DatapointDeaths.class).getValue();
    }

    DatapointCollectedMobTypeRewards.PlayerCollectedMobTypeRewards getCollectedMobTypesData() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COLLECTED_MOB_TYPE_REWARDS, DatapointCollectedMobTypeRewards.class).getValue();
    }

    double getCoins() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).getValue();
    }

    void setCoins(double amount) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).setValue(amount);
    }

    void addCoins(double amount) {
        setCoins(getCoins() + amount);
    }

    void removeCoins(double amount) {
        setCoins(getCoins() - amount);
    }

    Integer getBits() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BITS, DatapointInteger.class).getValue();
    }

    void setBits(int amount) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BITS, DatapointInteger.class).setValue(amount);
    }

    void addBits(int amount) {
        setBits(getBits() + amount);
    }

    void removeBits(int amount) {
        setBits(getBits() - amount);
        addFame(amount);
    }

    Integer getGems() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GEMS, DatapointInteger.class).getValue();
    }

    void setGems(int amount) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GEMS, DatapointInteger.class).setValue(amount);
    }

    void addGems(int amount) {
        setGems(getGems() + amount);
    }

    // Gems are only spent in the Community Shop, where each one earns 200 Fame.
    void removeGems(int amount) {
        setGems(getGems() - amount);
        addFame(amount * 200);
    }

    Integer getFame() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FAME, DatapointInteger.class).getValue();
    }

    void setFame(int amount) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FAME, DatapointInteger.class).setValue(Math.max(0, amount));
    }

    void addFame(int amount) {
        setFame(getFame() + amount);
    }

    Long getBoosterCookieExpirationDate() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BOOSTER_COOKIE_EXPIRATION_DATE, DatapointLong.class).getValue();
    }

    void setBoosterCookieExpirationDate(long timestamp) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BOOSTER_COOKIE_EXPIRATION_DATE, DatapointLong.class).setValue(timestamp);
    }

    boolean isBoosterCookieActive() {
        return getBoosterCookieExpirationDate() >= System.currentTimeMillis();
    }
}
