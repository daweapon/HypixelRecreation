package dev.daweapon.type.skyblockgeneric.user;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryPower;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.UpdateHealthPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.tag.Tag;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.PlayerShopData;
import dev.daweapon.commons.skyblock.SkyBlockPlayerProfiles;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarConnector;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.*;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.MaxHealthValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.set.ArmorSetRegistry;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.levels.CustomLevelAward;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockEmblems;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.noteblock.SkyBlockSongsHandler;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.skill.skills.RunecraftingSkill;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import dev.daweapon.type.skyblockgeneric.user.statistics.PlayerStatistics;
import dev.daweapon.type.skyblockgeneric.utility.DeathMessageCreator;
import dev.daweapon.type.generic.entity.npc.NPCConversations;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.daweapon.type.skyblockgeneric.region.rules.ZoneRuleService;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Getter
public class SkyBlockPlayer extends HypixelPlayer {
    private final PlayerAbilityHandler abilityHandler = new PlayerAbilityHandler();
    @Getter
    private final PlayerStatistics statistics = new PlayerStatistics(this);
    public float health = 100;
    private float absorption;
    @Setter
    public boolean isBankDelayed = false;
    @Setter
    private float mana = 100;
    @Setter
    private boolean inLaunchpad = false;
    @Setter
    public boolean bypassBuild = false;
    @Setter
    public boolean speedManaged = false;
    private final Map<String, HealingModifier> healingModifiers = new java.util.concurrent.ConcurrentHashMap<>();
    private long lastHealingBlockedMessage;
    @Setter
    private SkyBlockIsland skyBlockIsland;
    private static final Tag<Integer> fallHeight = Tag.Integer("fallHeight");
    private static final long HEALING_BLOCKED_MESSAGE_COOLDOWN_MS = 10_000;

    @Getter(AccessLevel.NONE)
    private final PlayerInventoryHandler inventoryHandler = new PlayerInventoryHandler(this);
    @Getter(AccessLevel.NONE)
    private final PlayerSackHandler sackHandler = new PlayerSackHandler(this);
    @Getter(AccessLevel.NONE)
    private final PlayerMiningHandler miningHandler = new PlayerMiningHandler(this);
    @Getter(AccessLevel.NONE)
    private final PlayerExperienceHandler experienceHandler = new PlayerExperienceHandler(this);
    @Getter(AccessLevel.NONE)
    private final PlayerDisplayNames displayNames = new PlayerDisplayNames(this);
    @Getter(AccessLevel.NONE)
    private final PlayerDataAccessors dataAccessors = new PlayerDataAccessors(this);

    public SkyBlockPlayer(@NotNull GameProfile gameProfile, @NotNull PlayerConnection playerConnection) {
        super(playerConnection, gameProfile);
    }

	public SkyBlockDataHandler getSkyblockDataHandler() {
        return SkyBlockDataHandler.getUser(this.getUuid());
    }

    public DatapointMuseum.MuseumData getMuseumData() { return dataAccessors.getMuseumData(); }

    public SkyBlockSongsHandler getSongHandler() {
        return new SkyBlockSongsHandler(this);
    }

    public FairySoulHandler getFairySoulHandler() {
        return new FairySoulHandler(this);
    }

    public @Nullable SkyBlockPlayerProfiles getProfiles() {
        return SkyBlockPlayerProfiles.get(this.getUuid());
    }

    public DatapointPetData.UserPetData getPetData() { return dataAccessors.getPetData(); }

    public MissionData getMissionData() { return dataAccessors.getMissionData(); }

    @Override
    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, @NotNull Vec velocity, long @Nullable [] chunks,
                                                                              @MagicConstant(flagsFromClass = RelativeFlags.class) int flags,
                                                                              boolean shouldConfirm) {
        setFallHeight(position.blockY());
        return super.teleport(position, velocity, chunks, flags, shouldConfirm);
    }

    public void setFallHeight(int fallHeight) {
        setTag(SkyBlockPlayer.fallHeight, fallHeight);
    }

    public Integer getFallHeight() {
        return getTag(SkyBlockPlayer.fallHeight);
    }

    @Override
    public Text getFullDisplayName() { return displayNames.getFullDisplayName(); }

    public Text getFullDisplayName(String levelColor) { return displayNames.getFullDisplayName(levelColor); }

    public Text getFullDisplayName(SkyBlockEmblems.SkyBlockEmblem displayEmblem) { return displayNames.getFullDisplayName(displayEmblem); }

    public DatapointArcheryPractice.ArcheryPracticeData getArcheryPracticeData() { return dataAccessors.getArcheryPracticeData(); }

    public Text getFullDisplayName(SkyBlockEmblems.SkyBlockEmblem displayEmblem, String levelColor) {
        return displayNames.getFullDisplayName(displayEmblem, levelColor);
    }

    public PlayerShopData getShoppingData() { return dataAccessors.getShoppingData(); }

    public DatapointCollection.PlayerCollection getCollection() { return dataAccessors.getCollection(); }

    public DatapointHunting.HuntingData getHuntingData() { return dataAccessors.getHuntingData(); }

    public DatapointSkills.PlayerSkills getSkills() { return dataAccessors.getSkills(); }

    public boolean isOnIsland() {
        return HypixelConst.isIslandServer()
                && getInstance() != null
                && getInstance() != HypixelConst.getInstanceContainer()
                && getInstance() != HypixelConst.getEmptyInstance();
    }

    public @Nullable SkyBlockItem getArrow() { return inventoryHandler.getArrow(); }

    public int getAmountOfEmptySlots() { return inventoryHandler.getAmountOfEmptySlots(); }

    public boolean hasEmptySlots(int amount) { return inventoryHandler.hasEmptySlots(amount); }

    public @Nullable SkyBlockItem peekArrow() { return inventoryHandler.peekArrow(); }

    public @Nullable SkyBlockItem getAndConsumeArrow() { return inventoryHandler.getAndConsumeArrow(); }

    public boolean hasTalisman(ItemType talisman) {
        return AccessoryOwnership.owns(this, talisman);
    }

    public List<ItemType> getTalismans() {
        return AccessoryOwnership.ownedTypes(this);
    }

    public int getAccessoryPower() {
        return AccessoryPower.of(this);
    }

    public void setMuseumData(DatapointMuseum.MuseumData data) { dataAccessors.setMuseumData(data); }

    public SkyBlockItem[] getAllInventoryItems() { return inventoryHandler.getAllInventoryItems(); }

    public Map<Integer, SkyBlockItem> getAllOfComponentInInventory(Class<? extends SkyBlockItemComponent> component) {
        return inventoryHandler.getAllOfComponentInInventory(component);
    }

    public Map<Integer, Integer> getAllOfTypeInInventory(ItemType type) { return inventoryHandler.getAllOfTypeInInventory(type); }

    public void setItemInHand(@Nullable SkyBlockItem item) { inventoryHandler.setItemInHand(item); }

    public int getAmountInInventory(ItemType type) { return inventoryHandler.getAmountInInventory(type); }

    public int getAmountAnywhere(ItemType type) { return inventoryHandler.getAmountAnywhere(type); }

    public boolean removeItemFromPlayer(ItemType type, int amount) { return inventoryHandler.removeItemFromPlayer(type, amount); }

    public boolean removeItemsFromPlayer(Map<ItemType, Integer> items) { return inventoryHandler.removeItemsFromPlayer(items); }

    public boolean isCoop() { return dataAccessors.isCoop(); }

    public ProfileMode getProfileMode() { return dataAccessors.getProfileMode(); }

    public boolean isIronman() { return dataAccessors.isIronman(); }

    public CoopDatabase.Coop getCoop() { return dataAccessors.getCoop(); }

    public @Nullable ArmorSetRegistry getArmorSet() { return inventoryHandler.getArmorSet(); }

    public int getRuneLevel() {
        return RunecraftingSkill.getUnlockedRune(this);
    }

    public boolean isWearingItem(SkyBlockItem item) { return inventoryHandler.isWearingItem(item); }

    public SkyBlockItem[] getArmor() { return inventoryHandler.getArmor(); }

    public SkyBlockItem updateItem(PlayerItemOrigin origin, Consumer<SkyBlockItem> update) { return inventoryHandler.updateItem(origin, update); }

    public SkyBlockItem updateItemInSlot(int slot, Consumer<SkyBlockItem> update) { return inventoryHandler.updateItemInSlot(slot, update); }

    public @Nullable SkyBlockRegion getRegion() {
        if (isOnIsland()) return SkyBlockRegion.getIslandRegion();
        return SkyBlockRegion.getRegionOfPosition(this.getPosition());
    }

    public void addAndUpdateItem(@Nullable List<SkyBlockItem> items) { inventoryHandler.addAndUpdateItem(items); }

    public void addAndUpdateItem(@Nullable SkyBlockItem item) { inventoryHandler.addAndUpdateItem(item); }

    public void addAndUpdateItem(UnderstandableSkyBlockItem item) { inventoryHandler.addAndUpdateItem(item); }

    public void addAndUpdateItem(ItemType item) { inventoryHandler.addAndUpdateItem(item); }

    public void addAndUpdateItem(ItemType item, int amount) { inventoryHandler.addAndUpdateItem(item, amount); }

    public void addAndUpdateItem(ItemStack item) { inventoryHandler.addAndUpdateItem(item); }

    public int countItem(ItemType item) { return inventoryHandler.countItem(item); }

    public boolean takeItem(SkyBlockItem itemToTake) { return inventoryHandler.takeItem(itemToTake); }

    public @Nullable List<SkyBlockItem> takeItem(ItemType type, int amount) { return inventoryHandler.takeItem(type, amount); }

    public DatapointQuiver.PlayerQuiver getQuiver() { return dataAccessors.getQuiver(); }

    public DatapointShipState.ShipState getShipState() { return dataAccessors.getShipState(); }

    public DatapointTrophyFish.TrophyFishData getTrophyFishData() { return dataAccessors.getTrophyFishData(); }

    public DatapointSlayer.SlayerData getSlayerData() { return dataAccessors.getSlayerData(); }

    public DatapointAccessoryBag.PlayerAccessoryBag getAccessoryBag() { return dataAccessors.getAccessoryBag(); }

    public DatapointSackOfSacks.PlayerSackOfSacks getSackOfSacks() { return dataAccessors.getSackOfSacks(); }

    public DatapointItemsInSacks.PlayerItemsInSacks getSackItems() { return dataAccessors.getSackItems(); }

    public DatapointStash.PlayerStash getStash() { return dataAccessors.getStash(); }

    public boolean canFitItem(ItemStack item) { return inventoryHandler.canFitItem(item); }

    public void addToStash(SkyBlockItem item) { inventoryHandler.addToStash(item); }

    public BazaarConnector getBazaarConnector() {
        return new BazaarConnector(this);
    }

    public List<SkyBlockItem> getAllSacks() { return sackHandler.getAllSacks(); }

    public int getMaxSackStorage(ItemType sack) { return sackHandler.getMaxSackStorage(sack); }

    public boolean canInsertItemIntoSacks(ItemType item) { return sackHandler.canInsertItemIntoSacks(item); }

    public boolean canInsertItemIntoSacks(ItemType item, Integer amount) { return sackHandler.canInsertItemIntoSacks(item, amount); }

    public String getShortenedDisplayName() {
        return getUsername();
    }

    public float getMaxMana() {
        return (float) (100 + getStatistics().allStatistics().getOverall(ItemStatistic.INTELLIGENCE));
    }

    public boolean hasCustomCollectionAward(CustomCollectionAward award) {
        if (dev.daweapon.type.skyblockgeneric.starlyn.StarlynShop.ownsAward(this, award)) return true;
        Map.Entry<ItemType, Integer> entry = CustomCollectionAward.AWARD_CACHE.get(award);
        if (entry == null) return false;

        return getCollection().get(entry.getKey()) >= entry.getValue();
    }

    public boolean hasCustomLevelAward(CustomLevelAward award) {
        return getSkyBlockExperience().getLevel().asInt() >= CustomLevelAward.getLevel(award);
    }

    public DatapointFairySouls.PlayerFairySouls getFairySouls() { return dataAccessors.getFairySouls(); }

    public DatapointSkyBlockExperience.PlayerSkyBlockExperience getSkyBlockExperience() { return dataAccessors.getSkyBlockExperience(); }

    public boolean hasUnlockedXPCause(SkyBlockLevelCauseAbstr cause) {
        return getSkyBlockExperience().hasExperienceFor(cause);
    }

    public PlayerEnchantmentHandler getEnchantmentHandler() {
        return new PlayerEnchantmentHandler(this);
    }

    public Double getMiningSpeed() {
        return this.getStatistics().allStatistics().getOverall(ItemStatistic.MINING_SPEED);
    }

    public double getTimeToMine(SkyBlockItem item, Block b) { return miningHandler.getTimeToMine(item, b); }

    public @NonNull Double getDefense() {
        return this.getStatistics().allStatistics().getOverall(ItemStatistic.DEFENSE);
    }

    public void playSuccessSound() {
        playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1.0f, 2.0f));
    }

    public double getCoins() { return dataAccessors.getCoins(); }

    public void setCoins(double amount) { dataAccessors.setCoins(amount); }

    public void addCoins(double amount) { dataAccessors.addCoins(amount); }

    public void removeCoins(double amount) { dataAccessors.removeCoins(amount); }

    public Integer getBits() { return dataAccessors.getBits(); }

    public void setBits(int amount) { dataAccessors.setBits(amount); }

    public void addBits(int amount) { dataAccessors.addBits(amount); }

    public void removeBits(int amount) { dataAccessors.removeBits(amount); }

    public Integer getGems() { return dataAccessors.getGems(); }

    public void setGems(int amount) { dataAccessors.setGems(amount); }

    public void addGems(int amount) { dataAccessors.addGems(amount); }

    public void removeGems(int amount) { dataAccessors.removeGems(amount); }

    public Integer getFame() { return dataAccessors.getFame(); }

    public void setFame(int amount) { dataAccessors.setFame(amount); }

    public void addFame(int amount) { dataAccessors.addFame(amount); }

    public int maxItemFit(ItemType targetType) { return inventoryHandler.maxItemFit(targetType); }

    public Long getBoosterCookieExpirationDate() { return dataAccessors.getBoosterCookieExpirationDate(); }

    public void setBoosterCookieExpirationDate(long timestamp) { dataAccessors.setBoosterCookieExpirationDate(timestamp); }

    public boolean isBoosterCookieActive() { return dataAccessors.isBoosterCookieActive(); }

    public DatapointKat.PlayerKat getKatData() { return dataAccessors.getKatData(); }

    public DatapointBestiary.PlayerBestiary getBestiaryData() { return dataAccessors.getBestiaryData(); }

    public Long getExperience() { return experienceHandler.getExperience(); }

    public void setExperience(long value) { experienceHandler.setExperience(value); }

    static int experienceLevel(long total) { return PlayerExperienceHandler.experienceLevel(total); }

    static long experienceAtLevel(int level) { return PlayerExperienceHandler.experienceAtLevel(level); }

    public void addExperience(long value) { experienceHandler.addExperience(value); }

    public void removeExperience(long value) { experienceHandler.removeExperience(value); }

    public DatapointDeaths.PlayerDeaths getDeathData() { return dataAccessors.getDeathData(); }

    public DatapointCollectedMobTypeRewards.PlayerCollectedMobTypeRewards getCollectedMobTypesData() {
        return dataAccessors.getCollectedMobTypesData();
    }

    @Override
    public void sendTo(ServerType type, boolean force) {
        if (!force && type != HypixelConst.getTypeLoader().getType() && type.name().startsWith("SKYBLOCK_")
                && !dev.daweapon.type.skyblockgeneric.elections.perks.TaxPerks.chargeIslandToll(this)) return;
        super.sendTo(type, force);
    }

    @Override
    public void kill() {
        if (PlayerDeathOverrides.handle(this)) return;
        NPCConversations.server().release(getUuid());
        healingModifiers.clear();
        setHealth(getMaxHealth());
        sendTo(HypixelConst.getTypeLoader().getType());

        DeathMessageCreator creator = new DeathMessageCreator(this.lastDamage);
        sendMessage("<c>☠ <7>You {}", creator.createPersonal());

        getDeathData().increase(this.lastDamage, 1);

        playSound(Sound.sound(Key.key("block.anvil.fall"), Sound.Source.PLAYER, 1.0f, 2.0f));

        if (HypixelConst.isPrivateWorldServer()) return;

        if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_KUUDRA
                && !isBoosterCookieActive()
                && !dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks.keepsCoinsOnDeath(this)) {
            double loss = getCoins() / 2;
            double saved = dev.daweapon.type.skyblockgeneric.accessory.PiggyBank.save(this, loss);
            if (saved > 0) sendMessage("<a>Your Piggy Bank saved {:,} coins!", saved);
            if (loss - saved > 0) sendMessage("<c>You died and lost {:,} coins!", loss - saved);
            setCoins(getCoins() - (loss - saved));
        }

        if (!HypixelConst.getTypeLoader().getLoaderValues().announceDeathMessages()) return;

        SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
            if (player.getUuid().equals(getUuid())) return;
            if (player.getInstance() != getInstance()) return;

            player.sendMessage("<c>☠ <7>{} <7>{}", getFullDisplayName(), creator.createOther());
        });
    }

    public record HealingModifier(double multiplier, @Nullable String blockedMessage) {
    }

    public void setHealingModifier(String source, HealingModifier modifier) {
        healingModifiers.put(source, modifier);
    }

    public void removeHealingModifier(String source) {
        healingModifiers.remove(source);
    }

    private double healingMultiplier() {
        double multiplier = 1;
        for (HealingModifier modifier : healingModifiers.values()) {
            multiplier *= modifier.multiplier();
            if (modifier.multiplier() <= 0 && modifier.blockedMessage() != null
                    && System.currentTimeMillis() - lastHealingBlockedMessage > HEALING_BLOCKED_MESSAGE_COOLDOWN_MS) {
                lastHealingBlockedMessage = System.currentTimeMillis();
                sendMessage(modifier.blockedMessage());
            }
        }
        return Math.max(0, multiplier);
    }

    public float getMaxHealth() {
        PlayerStatistics statistics = this.getStatistics();
        float maxHealth = statistics.allStatistics().getOverall(ItemStatistic.HEALTH).floatValue();
        MaxHealthValueUpdateEvent event = new MaxHealthValueUpdateEvent(this, maxHealth);
        SkyBlockValueEvent.callValueUpdateEvent(event);
        return (float) ZoneRuleService.capMaxHealth(this, ((Number) event.getValue()).doubleValue());
    }

    @Override
    public float getAdditionalHearts() {
        return absorption;
    }

    @Override
    public void setAdditionalHearts(float additionalHearts) {
        this.absorption = Math.max(0, additionalHearts);
    }

    @Override
    public float getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(float health) {
        if ((System.currentTimeMillis() - joined) < 3000) return;
        if (health < 0) {
            kill();
            return;
        }
        if (health > this.health && !healingModifiers.isEmpty()) {
            health = (float) (this.health + (health - this.health) * healingMultiplier());
        }
        this.health = health;
        this.sendPacket(new UpdateHealthPacket((health / getMaxHealth()) * 20, 20, 20));
    }

    @Override
    public void closeInventory() {
        Inventory tempInv = (Inventory) this.getOpenInventory();
        super.closeInventory();
        if (HypixelInventoryGUI.GUI_MAP.containsKey(this.getUuid())) {
            HypixelInventoryGUI gui = dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI.GUI_MAP.get(this.getUuid());

            if (gui == null) return;
            if (tempInv == null) return;
            gui.onClose(new InventoryCloseEvent(tempInv, this, true), dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI.CloseReason.SERVER_EXITED);
            dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI.GUI_MAP.remove(this.getUuid());
        }
    }

    public static Text getDisplayName(UUID uuid) { return PlayerDisplayNames.getDisplayName(uuid); }

    public static String getRawName(UUID uuid) { return PlayerDisplayNames.getRawName(uuid); }
}
