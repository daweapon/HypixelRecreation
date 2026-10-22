package dev.daweapon.type.skyblockgeneric.farming.animal;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public final class FarmAnimalAxes {
    private static final int EXTRA = 2;
    private static final Map<FarmAnimal, String> AXES = Map.of(
            FarmAnimal.SHEEP, "SHEEP_AXE",
            FarmAnimal.RABBIT, "RABBIT_AXE",
            FarmAnimal.MOOSHROOM, "MUSHROOM_COW_AXE",
            FarmAnimal.CHICKEN, "CHICKEN_AXE",
            FarmAnimal.COW, "COW_AXE",
            FarmAnimal.PIG, "PIG_AXE");
    private static final Map<FarmAnimal, String[]> DROPS = Map.of(
            FarmAnimal.SHEEP, new String[]{"WHITE_WOOL", "MUTTON"},
            FarmAnimal.RABBIT, new String[]{"RAW_RABBIT", "RABBIT_HIDE", "RABBIT_FOOT"},
            FarmAnimal.MOOSHROOM, new String[]{"RED_MUSHROOM", "LEATHER"},
            FarmAnimal.CHICKEN, new String[]{"FEATHER", "RAW_CHICKEN"},
            FarmAnimal.COW, new String[]{"RAW_BEEF"},
            FarmAnimal.PIG, new String[]{"RAW_PORKCHOP"});

    private FarmAnimalAxes() {
    }

    public static void onKill(SkyBlockPlayer player, SkyBlockMob mob) {
        FarmAnimal animal = FarmAnimal.of(mob);
        if (animal == null || !AXES.containsKey(animal)) return;
        ItemType held = new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType();
        if (held == null || !held.name().equals(AXES.get(animal))) return;
        for (String drop : DROPS.get(animal)) {
            ItemType type = ItemType.get(drop);
            if (type == null) continue;
            if (player.canInsertItemIntoSacks(type, EXTRA)) {
                player.getSackItems().increase(type, EXTRA);
            } else {
                player.addAndUpdateItem(type, EXTRA);
            }
        }
    }
}
