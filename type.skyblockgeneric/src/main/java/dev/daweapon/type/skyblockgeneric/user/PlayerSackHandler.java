package dev.daweapon.type.skyblockgeneric.user;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.SackComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PlayerSackHandler {
    private static final Pattern SACK_PATTERN = Pattern.compile("^(?:(SMALL|MEDIUM|LARGE|ENCHANTED)_)?(.+?)_SACK$");

    private final SkyBlockPlayer player;

    PlayerSackHandler(SkyBlockPlayer player) {
        this.player = player;
    }

    List<SkyBlockItem> getAllSacks() {
        List<SkyBlockItem> sacks = new ArrayList<>(player.getSackOfSacks().getAllSacks());
        for (SkyBlockItem item : player.getAllInventoryItems()) {
            if (item.hasComponent(SackComponent.class)) {
                sacks.add(item);
            }
        }
        return sacks;
    }

    int getMaxSackStorage(ItemType sack) {
        int maxStorage = 0;
        String sackCategory = "";

        Matcher matcher = SACK_PATTERN.matcher(sack.name());
        if (matcher.find()) {
            sackCategory = matcher.group(2);
        } else {
            org.tinylog.Logger.warn("Invalid sack name: {}", sack.name());
            return 0;
        }

        for (SkyBlockItem skyBlockItem : getAllSacks()) {
            matcher = SACK_PATTERN.matcher(skyBlockItem.getAttributeHandler().getTypeAsString());
            if (matcher.find()) {
                String otherCategory = matcher.group(2);
                if (sackCategory.equals(otherCategory) && skyBlockItem.hasComponent(SackComponent.class)) {
                    maxStorage += skyBlockItem.getComponent(SackComponent.class).capacityOf(skyBlockItem);
                }
            }
        }
        return maxStorage;
    }

    boolean canInsertItemIntoSacks(ItemType item) {
        for (SkyBlockItem sack : getAllSacks()) {
            if (sack.hasComponent(SackComponent.class)) {
                SackComponent sackInstance = sack.getComponent(SackComponent.class);
                for (ItemType linker : sackInstance.getValidItems()) {
                    if (linker == item) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean canInsertItemIntoSacks(ItemType item, Integer amount) {
        if (!canInsertItemIntoSacks(item)) return false;

        List<Map<SkyBlockItem, Integer>> maxStorages = new ArrayList<>();
        for (SkyBlockItem sack : getAllSacks()) {
            if (sack.hasComponent(SackComponent.class)) {
                SackComponent sackInstance = sack.getComponent(SackComponent.class);
                for (ItemType linker : sackInstance.getValidItems()) {
                    if (linker == item) {
                        if (!maxStorages.contains(sack)) {
                            maxStorages.add(Map.of(sack, getMaxSackStorage(sack.getAttributeHandler().getPotentialType())));
                        }
                    }
                }
            }
        }

        maxStorages.sort((map1, map2) -> {
            Integer value1 = map1.values().iterator().next();
            Integer value2 = map2.values().iterator().next();
            return value2.compareTo(value1);
        });

        return maxStorages.getFirst().values().iterator().next() >= player.getSackItems().getAmount(item) + amount;
    }
}
