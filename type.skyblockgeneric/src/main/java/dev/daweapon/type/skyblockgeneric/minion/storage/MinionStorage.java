package dev.daweapon.type.skyblockgeneric.minion.storage;

import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.extension.MinionExtensionData;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public final class MinionStorage {
    private static final String ATTRIBUTE = "storage_chest";
    private static final Map<MinionExtensionData, MinionStorageChest> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private MinionStorage() {
    }

    public static @Nullable MinionStorageChest get(IslandMinionData.IslandMinion minion) {
        MinionExtensionData data = minion.getExtensionData();
        MinionStorageChest cached = CACHE.get(data);
        if (cached != null) return cached;
        String raw = data.getAttribute(ATTRIBUTE);
        if (raw == null) return null;
        MinionStorageChest parsed = MinionStorageChest.deserialize(raw);
        CACHE.put(data, parsed);
        return parsed;
    }

    public static void attach(IslandMinionData.IslandMinion minion, MinionStorageChest chest) {
        CACHE.put(minion.getExtensionData(), chest);
        persist(minion);
    }

    public static @Nullable MinionStorageChest detach(IslandMinionData.IslandMinion minion) {
        MinionStorageChest chest = get(minion);
        CACHE.remove(minion.getExtensionData());
        minion.getExtensionData().setAttribute(ATTRIBUTE, null);
        return chest;
    }

    public static void persist(IslandMinionData.IslandMinion minion) {
        MinionStorageChest chest = CACHE.get(minion.getExtensionData());
        minion.getExtensionData().setAttribute(ATTRIBUTE, chest == null ? null : chest.serialize());
    }

    public static boolean tryAdd(IslandMinionData.IslandMinion minion, SkyBlockItem item) {
        MinionStorageChest chest = get(minion);
        if (chest == null || !chest.add(item)) return false;
        minion.setGeneratedItems(minion.getGeneratedItems() + item.getAmount());
        persist(minion);
        return true;
    }

    public static int extraCapacity(IslandMinionData.IslandMinion minion) {
        MinionStorageChest chest = get(minion);
        return chest == null ? 0 : chest.getTier().capacityItems();
    }

    public static int extraStored(IslandMinionData.IslandMinion minion) {
        MinionStorageChest chest = get(minion);
        return chest == null ? 0 : chest.storedAmount();
    }

    public static boolean hasRoom(IslandMinionData.IslandMinion minion) {
        MinionStorageChest chest = get(minion);
        return chest != null && chest.freeAmount() > 0;
    }

    public static List<ItemQuantifiable> drain(IslandMinionData.IslandMinion minion) {
        MinionStorageChest chest = get(minion);
        if (chest == null || chest.getItems().isEmpty()) return List.of();
        List<ItemQuantifiable> drained = new ArrayList<>(chest.getItems());
        chest.getItems().clear();
        persist(minion);
        return drained;
    }

    public static void release(IslandMinionData.IslandMinion minion, Consumer<SkyBlockItem> receiver) {
        MinionStorageChest chest = detach(minion);
        if (chest == null) return;
        chest.getItems().forEach(entry -> receiver.accept(entry.toSkyBlockItem()));
        receiver.accept(new SkyBlockItem(chest.getTier().itemType()));
        if (minion.getMinionEntity() != null && minion.getMinionEntity().getInstance() != null) {
            minion.getMinionEntity().getInstance().setBlock(chest.getPosition(), Block.AIR);
        }
    }

    public static Optional<IslandMinionData.IslandMinion> findByChest(IslandMinionData data, Point chestPosition) {
        for (IslandMinionData.IslandMinion minion : data.getMinions()) {
            MinionStorageChest chest = get(minion);
            if (chest != null && chest.isAt(chestPosition.blockX(), chestPosition.blockY(), chestPosition.blockZ())) {
                return Optional.of(minion);
            }
        }
        return Optional.empty();
    }

    public static Optional<IslandMinionData.IslandMinion> findLinkable(IslandMinionData data, Point chestPosition) {
        return MinionStorageLinks.firstAvailable(chestPosition, candidate -> linkableAt(data, candidate))
                .flatMap(candidate -> minionAt(data, candidate));
    }

    public static boolean hasAdjacentMinion(IslandMinionData data, Point chestPosition) {
        return MinionStorageLinks.linkOrder(chestPosition).stream().anyMatch(candidate -> minionAt(data, candidate).isPresent());
    }

    private static boolean linkableAt(IslandMinionData data, Point candidate) {
        return minionAt(data, candidate).filter(minion -> get(minion) == null).isPresent();
    }

    private static Optional<IslandMinionData.IslandMinion> minionAt(IslandMinionData data, Point position) {
        return data.getMinions().stream()
                .filter(minion -> minion.getPosition().blockX() == position.blockX()
                        && minion.getPosition().blockY() == position.blockY()
                        && minion.getPosition().blockZ() == position.blockZ())
                .findFirst();
    }
}
