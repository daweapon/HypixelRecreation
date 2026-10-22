package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryEconomyHooks;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.SharedInstance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.entity.MinionEntityImpl;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.MinionFuelComponent;
import dev.daweapon.type.skyblockgeneric.item.components.MinionUpgradeComponent;
import dev.daweapon.type.skyblockgeneric.minion.extension.MinionExtensionData;
import dev.daweapon.type.skyblockgeneric.minion.extension.extensions.MinionFuelExtension;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;
import org.tinylog.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Getter
// Tracks all minions placed on an island, handling their initialization, storage, and per-minion state.
public class IslandMinionData {
    private final SkyBlockIsland island;
    private static final int EVERBURNING_FLAME_COMBAT_BONUS = 5;
    private static final Set<MinionRegistry> EVERBURNING_FLAME_BONUS_MINIONS = EnumSet.of(
            MinionRegistry.BLAZE, MinionRegistry.CAVE_SPIDER, MinionRegistry.CREEPER, MinionRegistry.ENDERMAN,
            MinionRegistry.GHAST, MinionRegistry.MAGMA_CUBE, MinionRegistry.SKELETON, MinionRegistry.SLIME,
            MinionRegistry.ZOMBIE);

    private final List<IslandMinion> minions = new ArrayList<>();

    public IslandMinionData(SkyBlockIsland island) {
        this.island = island;
    }

    public IslandMinion initializeMinion(
            Pos position, MinionRegistry minion, ItemAttributeMinionData.MinionData previousData, boolean mithrilInfusion) {
        IslandMinion islandMinion = new IslandMinion(
                UUID.randomUUID(), position, minion,
                previousData.tier(), new ArrayList<>(),
                previousData.generatedResources(),
                System.currentTimeMillis(), null,
                new MinionHandler.InternalMinionTags(),
                new MinionExtensionData(),
                mithrilInfusion,
                0L);
        minions.add(islandMinion);
        return islandMinion;
    }

    public void spawn(IslandMinion minion) {
        minion.spawnMinion(getIsland().getSharedInstance().join());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class IslandMinion {
        private final UUID minionUUID;
        private final Pos position;
        private final MinionRegistry minion;
        private final int tier;
        private List<ItemQuantifiable> itemsInMinion;
        private int generatedItems;
        private long lastAction;
        private MinionEntityImpl minionEntity;
        private MinionHandler.InternalMinionTags internalMinionTags;
        private final MinionExtensionData extensionData;
        private boolean mithrilInfusion;
        private long capsaicinExpiry;
        private final AtomicLong offlineSince = new AtomicLong();

        public void spawnMinion(SharedInstance instance) {
            minionEntity = new MinionEntityImpl(this, minion.asSkyBlockMinion());
            minionEntity.setInstance(instance, position.add(0.5, 0, 0.5));
        }

        public void removeMinion() {
            internalMinionTags.onMinionDespawn(this);
            minionEntity.remove();
        }

        public boolean addItem(SkyBlockItem item) {
            int slots = minion.asSkyBlockMinion().getTiers().get(getTier() - 1).getSlots();
            // Calculate the total number of items in minion as if they were in stacks of 64.
            int totalItemsInStacks = itemsInMinion.stream().mapToInt(ItemQuantifiable::getAmount).sum();

            // Check if adding the new item would exceed the total slot capacity (slots * 64).
            if ((totalItemsInStacks + item.getAmount()) > (slots * 64)) {
                return dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorage.tryAdd(this, item);
            }

            // Increment the generatedItems counter by the amount of the new item.
            setGeneratedItems(getGeneratedItems() + item.getAmount());

            // Check if the item already exists in the inventory.
            Optional<ItemQuantifiable> existingItem = itemsInMinion.stream()
                    .filter(materialQuantifiable -> materialQuantifiable.matchesType(item))
                    .findFirst();

            if (existingItem.isPresent()) {
                // If the item exists, increase its amount.
                existingItem.get().setAmount(existingItem.get().getAmount() + item.getAmount());
            } else {
                // If the item does not exist, add it as a new entry.
                if (item.getAttributeHandler().getPotentialType() == null) {
                    throw new NullPointerException(
                            "Item type is null for " + item.getAttributeHandler().getTypeAsString());
                }
                itemsInMinion.add(new ItemQuantifiable(item.getAttributeHandler().getPotentialType(), item.getAmount()));
            }
            return true;
        }

        public SkyBlockItem asSkyBlockItem() {
            SkyBlockItem toReturn = new SkyBlockItem(getMinion().getItemType());
            toReturn.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(
                    getTier(),
                    getGeneratedItems()
            ));
            toReturn.getAttributeHandler().setMithrilInfused(mithrilInfusion);
            return toReturn;
        }

        public int getSpeedPercentage() {
            return getSpeedPercentage(minionEntity == null ? null : minionEntity.getInstance());
        }

        public int getSpeedPercentage(Instance instance) {//Handle percentage speed increase from both fuels and minion upgrades
            int percentageSpeedIncrease = 0;

            //Handle Mithril Infusion
            if (isMithrilInfusion())
                percentageSpeedIncrease += 10;

            //Handle Minion Fuel
            MinionFuelExtension fuelExtension = (MinionFuelExtension)
                    extensionData.getOfType(MinionFuelExtension.class);
            ItemType minionFuel = fuelExtension.getItemTypePassedIn();
            if (minionFuel != null && fuelExtension.refresh(this)
                    && (minionFuel != ItemType.SOLAR_PANEL || isDaytime(instance))) {
                percentageSpeedIncrease += (int) new SkyBlockItem(minionFuel).getComponent(MinionFuelComponent.class).getFuelPercentage();
                if (minionFuel == ItemType.EVERBURNING_FLAME && EVERBURNING_FLAME_BONUS_MINIONS.contains(minion)) {
                    percentageSpeedIncrease += EVERBURNING_FLAME_COMBAT_BONUS;
                }
            }

            //Handle speed increases from minion upgrades
            for (SkyBlockItem item : extensionData.getMinionUpgrades()) {
                if (item != null && item.hasComponent(MinionUpgradeComponent.class)) {
                    percentageSpeedIncrease += (int) item.getComponent(MinionUpgradeComponent.class).getSpeedIncrease();
                }
            }

            if (minion.isMining()) {
                percentageSpeedIncrease += (int) ElectionPerks.bonus(SkyBlockMayor.Perk.PROSPECTION);
            }

            percentageSpeedIncrease += minion.asSkyBlockMinion().getBonusSpeedPercentage(this);
            if (dev.daweapon.type.skyblockgeneric.minion.freewill.FreeWillService.isLoyal(this)) {
                percentageSpeedIncrease += (int) dev.daweapon.type.skyblockgeneric.minion.freewill.FreeWillOdds.LOYALTY_SPEED_PERCENT;
            }
            if (instance != null) {
                percentageSpeedIncrease += (int) PetAbilities.minionSpeedPercent(instance, minion.name());
                percentageSpeedIncrease += (int) AccessoryEconomyHooks.potatoMinionSpeedPercent(instance, minion.name());
                percentageSpeedIncrease += (int) dev.daweapon.type.skyblockgeneric.island.IslandUtilityBlocks.minionSpeedPercent(instance, minion.isMining());
                percentageSpeedIncrease += (int) dev.daweapon.type.skyblockgeneric.island.IslandUtilityBlocks.crystalSpeedPercent(instance, minion);
                percentageSpeedIncrease += (int) dev.daweapon.type.skyblockgeneric.beacon.BeaconService.minionSpeedPercent(instance);
            }

            return percentageSpeedIncrease;
        }

        public double getSpeedDivisor() {
            return (1 + getSpeedPercentage() / 100.0) * minion.asSkyBlockMinion().getSpeedMultiplier(this);
        }

        public double getSpeedDivisor(Instance instance) {
            return (1 + getSpeedPercentage(instance) / 100.0) * minion.asSkyBlockMinion().getSpeedMultiplier(this);
        }

        private boolean isDaytime(Instance instance) {
            if (instance == null) return false;
            long time = Math.floorMod(instance.getTime(), 24000);
            return time < 12000;
        }

        public int getBonusRange() {
            int range = 0;
            range += extensionData.getMinionUpgradeCount(ItemType.MINION_EXPANDER);
            return range;
        }

        public Map<String, Object> serialize() {
            List<String> itemsInMinionAsString = new ArrayList<>();
            itemsInMinion.forEach(item -> {
                itemsInMinionAsString.add(item.getItem().toUnderstandable().serialize() + "," + item.getAmount());
            });

            Map<String, Object> data = new HashMap<>();
            data.put("position", position.blockX() + "," + position.blockY() + "," + position.blockZ());
            data.put("itemsInMinion", itemsInMinionAsString);
            data.put("minion", minion.name());
            data.put("lastAction", lastAction);
            data.put("savedAt", System.currentTimeMillis());
            data.put("tier", tier);
            data.put("generatedItems", generatedItems);
            data.put("minionUUID", minionUUID.toString());
            data.put("extensionData", extensionData.toString());
            data.put("mithrilInfusion", mithrilInfusion);
            data.put("capsaicinExpiry", capsaicinExpiry);
            return data;
        }

        public static IslandMinion deserialize(Map<String, Object> data) {
            List<ItemQuantifiable> itemsInMinion = new ArrayList<>();
            ((List<String>) data.get("itemsInMinion")).forEach(item -> {
                int splitIndex = item.lastIndexOf(",");
                if (splitIndex != -1) {
                    String itemJson = item.substring(0, splitIndex);
                    int amount;
                    try {
                        amount = Integer.parseInt(item.substring(splitIndex + 1));
                    } catch (NumberFormatException e) {
                        Logger.error("Failed to parse item amount: {}", item);
                        throw new RuntimeException("Failed to parse item amount: " + item, e);
                    }
                    itemsInMinion.add(new ItemQuantifiable(
                            new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(itemJson)),
                            amount
                    ));
                } else {
                    Logger.error("Invalid item format: {}", item);
                    throw new RuntimeException("Invalid item format: " + item);
                }
            });

            MinionExtensionData extensionData;
            if (data.containsKey("extensionData"))
                extensionData = MinionExtensionData.fromString(data.get("extensionData").toString());
            else
                extensionData = new MinionExtensionData();

            long storedLastAction = data.containsKey("lastAction")
                    ? ((Number) data.get("lastAction")).longValue() : System.currentTimeMillis();
            long savedAt = data.containsKey("savedAt") ? ((Number) data.get("savedAt")).longValue() : storedLastAction;

            IslandMinion restored = new IslandMinion(
                    UUID.fromString(data.get("minionUUID").toString()),
                    new Pos(
                            Integer.parseInt(data.get("position").toString().split(",")[0]),
                            Integer.parseInt(data.get("position").toString().split(",")[1]),
                            Integer.parseInt(data.get("position").toString().split(",")[2])
                    ),
                    MinionRegistry.valueOf(data.get("minion").toString()),
                    (int) data.get("tier"),
                    itemsInMinion,
                    (int) data.get("generatedItems"),
                    storedLastAction,
                    null,
                    new MinionHandler.InternalMinionTags(),
                    extensionData,
                    data.containsKey("mithrilInfusion") && (boolean) data.get("mithrilInfusion"),
                    data.containsKey("capsaicinExpiry") ? ((Number) data.get("capsaicinExpiry")).longValue() : 0L
            );
            restored.offlineSince.set(savedAt);
            return restored;
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();

        minions.forEach(minion -> {
            data.put(minion.minionUUID.toString(), minion.serialize());
        });

        return data;
    }

    public static IslandMinionData deserialize(Map<String, Object> data, SkyBlockIsland island) {
        IslandMinionData islandMinionData = new IslandMinionData(island);
        data.forEach((uuid, minionData) -> {
            islandMinionData.minions.add(IslandMinion.deserialize((Map<String, Object>) minionData));
        });
        return islandMinionData;
    }
}
