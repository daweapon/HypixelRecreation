package dev.daweapon.type.skyblockgeneric.data.datapoints;

import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.museum.MuseumDisplays;
import dev.daweapon.type.skyblockgeneric.museum.MuseumEntry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class DatapointMuseum extends SkyBlockDatapoint<DatapointMuseum.MuseumData> {
    private static final Serializer<MuseumData> serializer = new Serializer<>() {
        @Override
        public String serialize(MuseumData value) {
            return value.serialize().toString();
        }

        @Override
        public MuseumData deserialize(String json) {
            return MuseumData.deserialize(new JSONObject(json));
        }

        @Override
        public MuseumData clone(MuseumData value) {
            return MuseumData.deserialize(new JSONObject(value.serialize().toString()));
        }
    };

    public DatapointMuseum(String key, MuseumData value) {
        super(key, value, serializer);
    }

    public DatapointMuseum(String key) {
        super(key, new MuseumData(), serializer);
    }

    // Records for better encapsulation
    public record ViewingInfo(UUID playerUuid, UUID profileUuid) {}
    public record DisplayPlacement(MuseumDisplays display, Integer slot) {}
    public record ItemMetadata(UUID itemUuid, Long insertionTime, Double calculatedPrice) {}

    @Getter
    @Setter
    public static class MuseumData {
        private ViewingInfo currentlyViewing = new ViewingInfo(UUID.randomUUID(), UUID.randomUUID());
        private boolean hasBoughtAppraisalService = false;
        private ArrayList<SkyBlockItem> currentlyInMuseum = new ArrayList<>();
        private ArrayList<SkyBlockItem> previouslyInMuseum = new ArrayList<>();
        private Map<UUID, DisplayPlacement> museumDisplay = new HashMap<>();
        private Map<UUID, Long> insertionTimes = new HashMap<>();
        private Map<UUID, Double> calculatedPrices = new HashMap<>();
        private Set<String> donatedEntries = new LinkedHashSet<>();
        private Map<UUID, String> entryOf = new HashMap<>();
        private Set<UUID> staffGranted = new LinkedHashSet<>();
        private int claimedMilestone = 0;

        @Getter
        private final DisplayHandler displayHandler = new DisplayHandler();

        public boolean isCredited(String entryId) {
            return donatedEntries.contains(entryId);
        }

        public void credit(String entryId) {
            donatedEntries.add(entryId);
        }

        public List<SkyBlockItem> itemsFor(MuseumEntry entry, boolean current) {
            return (current ? currentlyInMuseum : previouslyInMuseum).stream()
                    .filter(item -> item.getAttributeHandler().getPotentialType() != null)
                    .filter(item -> belongsTo(entry, item))
                    .collect(Collectors.toList());
        }

        public boolean belongsTo(MuseumEntry entry, SkyBlockItem item) {
            String owner = ownerId(item);
            if (owner != null) return owner.equals(entry.id());
            ItemType type = item.getAttributeHandler().getPotentialType();
            return type != null && entry.accepts(type);
        }

        public @Nullable String ownerId(SkyBlockItem item) {
            UUID id = trackedId(item);
            return id == null ? null : entryOf.get(id);
        }

        public void assignOwner(SkyBlockItem item, String entryId) {
            UUID id = trackedId(item);
            if (id != null) entryOf.putIfAbsent(id, entryId);
        }

        public boolean isStaffGranted(SkyBlockItem item) {
            UUID id = trackedId(item);
            return id != null && staffGranted.contains(id);
        }

        public void grant(MuseumEntry entry, SkyBlockItem item) {
            add(entry, item);
            staffGranted.add(trackedId(item));
        }

        private static @Nullable UUID trackedId(SkyBlockItem item) {
            String id = item.getAttributeHandler().getUniqueTrackedID();
            return id == null ? null : UUID.fromString(id);
        }

        public List<SkyBlockItem> getNotInDisplay() {
            return currentlyInMuseum.stream()
                    .filter(item -> !museumDisplay.containsKey(UUID.fromString(item.getAttributeHandler().getUniqueTrackedID())))
                    .collect(Collectors.toList());
        }

        // Item retrieval methods
        public @Nullable SkyBlockItem getItemInMuseum(ItemType type) {
            return currentlyInMuseum.stream()
                    .filter(item -> item.getAttributeHandler().getPotentialType().equals(type))
                    .findFirst().orElse(null);
        }

        public @Nullable SkyBlockItem getItemPreviouslyInMuseum(ItemType type) {
            return previouslyInMuseum.stream()
                    .filter(item -> item.getAttributeHandler().getPotentialType().equals(type))
                    .findFirst().orElse(null);
        }

        public @NonNull SkyBlockItem getFromUUID(UUID itemUuid) {
            return currentlyInMuseum.stream()
                    .filter(item -> item.getAttributeHandler().getUniqueTrackedID().equals(itemUuid.toString()))
                    .findFirst()
                    .orElseGet(() -> previouslyInMuseum.stream()
                            .filter(item -> item.getAttributeHandler().getUniqueTrackedID().equals(itemUuid.toString()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No item with UUID " + itemUuid + " found")));
        }

        public List<SkyBlockItem> getAllItems() {
            List<SkyBlockItem> items = new ArrayList<>(currentlyInMuseum);
            items.addAll(previouslyInMuseum);
            return items;
        }

        public void add(MuseumEntry entry, SkyBlockItem item) {
            ItemType itemType = item.getAttributeHandler().getPotentialType();
            UUID itemUuid = trackedId(item);
            List<SkyBlockItem> replaced = getAllItems().stream()
                    .filter(existing -> Objects.equals(existing.getAttributeHandler().getPotentialType(), itemType))
                    .filter(existing -> belongsTo(entry, existing))
                    .toList();
            for (SkyBlockItem existing : replaced) {
                currentlyInMuseum.remove(existing);
                previouslyInMuseum.remove(existing);
                UUID existingUuid = trackedId(existing);
                if (existingUuid == null || existingUuid.equals(itemUuid)) continue;
                entryOf.remove(existingUuid);
                staffGranted.remove(existingUuid);
                insertionTimes.remove(existingUuid);
            }

            currentlyInMuseum.add(item);
            entryOf.put(itemUuid, entry.id());
            insertionTimes.put(itemUuid, System.currentTimeMillis());
        }

        public void moveToRetrieved(SkyBlockItem item) {
            UUID itemUuid = UUID.fromString(item.getAttributeHandler().getUniqueTrackedID());

            if (currentlyInMuseum.remove(item)) {
                previouslyInMuseum.add(item);
                museumDisplay.remove(itemUuid);
            }
        }

        public ItemMetadata getItemMetadata(UUID itemUuid) {
            return new ItemMetadata(
                    itemUuid,
                    insertionTimes.get(itemUuid),
                    calculatedPrices.get(itemUuid)
            );
        }

        public class DisplayHandler {
            public List<SkyBlockItem> getAllItemsInDisplay(MuseumDisplays display) {
                return museumDisplay.entrySet().stream()
                        .filter(entry -> entry.getValue().display().equals(display))
                        .sorted(Map.Entry.comparingByValue(Comparator.comparing(DisplayPlacement::slot)))
                        .map(entry -> getFromUUID(entry.getKey()))
                        .collect(Collectors.toList());
            }

            public List<SkyBlockItem> getItemsAtSlot(MuseumDisplays display, Integer slot) {
                return museumDisplay.entrySet().stream()
                        .filter(entry -> entry.getValue().display().equals(display) && entry.getValue().slot().equals(slot))
                        .map(entry -> getFromUUID(entry.getKey()))
                        .collect(Collectors.toList());
            }

            public Map<Integer, List<SkyBlockItem>> getItemsBySlot(MuseumDisplays display) {
                return museumDisplay.entrySet().stream()
                        .filter(entry -> entry.getValue().display().equals(display))
                        .collect(Collectors.groupingBy(
                                entry -> entry.getValue().slot(),
                                Collectors.mapping(entry -> getFromUUID(entry.getKey()), Collectors.toList())
                        ));
            }

            public boolean isItemInDisplay(SkyBlockItem item) {
                UUID itemUuid = UUID.fromString(item.getAttributeHandler().getUniqueTrackedID());
                return museumDisplay.containsKey(itemUuid);
            }

            public @Nullable DisplayPlacement getItemDisplayPlacement(SkyBlockItem item) {
                UUID itemUuid = UUID.fromString(item.getAttributeHandler().getUniqueTrackedID());
                return museumDisplay.get(itemUuid);
            }

            public void addToDisplay(SkyBlockItem item, MuseumDisplays display, Integer slot) {
                UUID itemUuid = UUID.fromString(item.getAttributeHandler().getUniqueTrackedID());
                museumDisplay.put(itemUuid, new DisplayPlacement(display, slot));
            }

            public void addToDisplay(List<SkyBlockItem> items, MuseumDisplays display, Integer slot) {
                for (SkyBlockItem item : items) {
                    addToDisplay(item, display, slot);
                }
            }

            public void removeFromDisplay(SkyBlockItem item) {
                UUID itemUuid = UUID.fromString(item.getAttributeHandler().getUniqueTrackedID());
                museumDisplay.remove(itemUuid);
            }

            public void removeFromDisplay(List<SkyBlockItem> items) {
                for (SkyBlockItem item : items) {
                    removeFromDisplay(item);
                }
            }

            public void removeAllFromSlot(MuseumDisplays display, Integer slot) {
                museumDisplay.entrySet().removeIf(entry ->
                        entry.getValue().display().equals(display) &&
                                entry.getValue().slot().equals(slot)
                );
            }

            public void removeAllFromDisplay(MuseumDisplays display) {
                museumDisplay.entrySet().removeIf(entry ->
                        entry.getValue().display().equals(display)
                );
            }

            public boolean hasItemsAtSlot(MuseumDisplays display, Integer slot) {
                return museumDisplay.values().stream()
                        .anyMatch(placement -> placement.display().equals(display) && placement.slot().equals(slot));
            }

            public int getItemCountAtSlot(MuseumDisplays display, Integer slot) {
                return (int) museumDisplay.values().stream()
                        .filter(placement -> placement.display().equals(display) && placement.slot().equals(slot))
                        .count();
            }

            public List<Integer> getOccupiedSlots(MuseumDisplays display) {
                return museumDisplay.values().stream()
                        .filter(placement -> placement.display().equals(display))
                        .map(DisplayPlacement::slot)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
            }

            public List<Integer> getAvailableSlots(MuseumDisplays display) {
                Set<Integer> occupiedSlots = museumDisplay.values().stream()
                        .filter(placement -> placement.display().equals(display))
                        .map(DisplayPlacement::slot)
                        .collect(Collectors.toSet());

                List<Integer> availableSlots = new ArrayList<>();
                for (int i = 0; i < display.getPositions().size(); i++) {
                    if (!occupiedSlots.contains(i)) {
                        availableSlots.add(i);
                    }
                }
                return availableSlots;
            }

            public Map<MuseumDisplays, List<SkyBlockItem>> fetchAllDisplayedItems() {
                Map<MuseumDisplays, List<SkyBlockItem>> displayedItems = new HashMap<>();

                for (MuseumDisplays display : MuseumDisplays.values()) {
                    displayedItems.put(display, getAllItemsInDisplay(display));
                }

                return displayedItems;
            }

            public Map<MuseumDisplays, Map<Integer, List<SkyBlockItem>>> fetchAllDisplayedItemsBySlot() {
                Map<MuseumDisplays, Map<Integer, List<SkyBlockItem>>> displayedItems = new HashMap<>();

                for (MuseumDisplays display : MuseumDisplays.values()) {
                    displayedItems.put(display, getItemsBySlot(display));
                }

                return displayedItems;
            }

            public Map<MuseumDisplays, Integer> getDisplayItemCounts() {
                return Arrays.stream(MuseumDisplays.values())
                        .collect(Collectors.toMap(
                                display -> display,
                                display -> getAllItemsInDisplay(display).size()
                        ));
            }
        }

        public JSONObject serialize() {
            JSONObject json = new JSONObject();

            if (currentlyViewing != null) {
                json.put("currentlyViewing", currentlyViewing.playerUuid() + "=" + currentlyViewing.profileUuid());
            } else {
                json.put("currentlyViewing", JSONObject.NULL);
            }

            json.put("hasBoughtAppraisalService", hasBoughtAppraisalService);

            List<String> currentItems = currentlyInMuseum.stream()
                    .map(item -> item.toUnderstandable().serialize())
                    .toList();
            List<String> previousItems = previouslyInMuseum.stream()
                    .map(item -> item.toUnderstandable().serialize())
                    .toList();

            json.put("currentlyInMuseum", currentItems);
            json.put("previouslyInMuseum", previousItems);

            JSONObject displayJson = new JSONObject();
            for (Map.Entry<UUID, DisplayPlacement> entry : museumDisplay.entrySet()) {
                UUID itemUuid = entry.getKey();
                DisplayPlacement placement = entry.getValue();
                displayJson.put(itemUuid.toString(), placement.display().name() + "=" + placement.slot());
            }
            json.put("museumDisplay", displayJson.toString());

            json.put("insertionTimes", new JSONObject(insertionTimes).toString());
            json.put("calculatedPrices", new JSONObject(calculatedPrices).toString());
            json.put("donatedEntries", new ArrayList<>(donatedEntries));
            json.put("claimedMilestone", claimedMilestone);
            JSONObject owners = new JSONObject();
            entryOf.forEach((itemUuid, entryId) -> owners.put(itemUuid.toString(), entryId));
            json.put("entryOf", owners);
            json.put("staffGranted", staffGranted.stream().map(UUID::toString).toList());

            return json;
        }

        public static MuseumData deserialize(JSONObject json) {
            MuseumData data = new MuseumData();

            if (json.isNull("currentlyViewing")) {
                data.currentlyViewing = null;
            } else {
                String[] viewingParts = json.getString("currentlyViewing").split("=");
                data.currentlyViewing = new ViewingInfo(
                        UUID.fromString(viewingParts[0]),
                        UUID.fromString(viewingParts[1])
                );
            }

            data.hasBoughtAppraisalService = json.getBoolean("hasBoughtAppraisalService");

            data.previouslyInMuseum = json.getJSONArray("previouslyInMuseum").toList().stream()
                    .map(item -> new SkyBlockItem(UnderstandableSkyBlockItem.deserialize((String) item)))
                    .collect(Collectors.toCollection(ArrayList::new));

            data.currentlyInMuseum = json.getJSONArray("currentlyInMuseum").toList().stream()
                    .map(item -> new SkyBlockItem(UnderstandableSkyBlockItem.deserialize((String) item)))
                    .collect(Collectors.toCollection(ArrayList::new));

            JSONObject displayJson = new JSONObject(json.getString("museumDisplay"));
            data.museumDisplay = displayJson.toMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> UUID.fromString(entry.getKey()),
                            entry -> {
                                String[] values = ((String) entry.getValue()).split("=");
                                MuseumDisplays display = MuseumDisplays.valueOf(values[0]);
                                int slot = Integer.parseInt(values[1]);
                                return new DisplayPlacement(display, slot);
                            }
                    ));

            data.insertionTimes = new JSONObject(json.getString("insertionTimes")).toMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> UUID.fromString(entry.getKey()),
                            entry -> ((Number) entry.getValue()).longValue()
                    ));

            data.calculatedPrices = new JSONObject(json.getString("calculatedPrices")).toMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> UUID.fromString(entry.getKey()),
                            entry -> ((Number) entry.getValue()).doubleValue()
                    ));

            JSONArray donated = json.optJSONArray("donatedEntries");
            if (donated != null) {
                donated.toList().forEach(entryId -> data.donatedEntries.add(String.valueOf(entryId)));
            }
            data.claimedMilestone = json.optInt("claimedMilestone", 0);
            JSONObject owners = json.optJSONObject("entryOf");
            if (owners != null) {
                owners.keySet().forEach(itemUuid -> data.entryOf.put(UUID.fromString(itemUuid), owners.getString(itemUuid)));
            }
            JSONArray staff = json.optJSONArray("staffGranted");
            if (staff != null) {
                staff.toList().forEach(itemUuid -> data.staffGranted.add(UUID.fromString(String.valueOf(itemUuid))));
            }

            return data;
        }
    }
}