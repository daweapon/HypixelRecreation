package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One pet at most per kill: a single weighted pick over the rarities, so the stated per-rarity chances hold and a
 * staff-forced pet is claimed by the first listed rarity.
 */
public final class PetDropTable {
    private record Entry(Rarity rarity, double chancePercent) {
    }

    private final ItemType pet;
    private final List<Entry> entries = new ArrayList<>();

    private PetDropTable(String itemId) {
        this.pet = ItemType.get(itemId);
    }

    public static PetDropTable of(String itemId) {
        return new PetDropTable(itemId);
    }

    public PetDropTable rarity(Rarity rarity, double chancePercent) {
        entries.add(new Entry(rarity, chancePercent));
        return this;
    }

    public void roll(SkyBlockPlayer player, SkyBlockMob mob) {
        if (pet == null) return;
        double total = entries.stream().mapToDouble(Entry::chancePercent).sum();
        List<Entry> table = new ArrayList<>(entries);
        table.add(new Entry(null, Math.max(0, 100 - total)));
        Entry won = LootRoll.pick(player.getUuid(), table, entry -> entry.rarity() == null ? null : pet,
                Entry::chancePercent, ThreadLocalRandom.current());
        if (won.rarity() == null) return;
        SkyBlockItem item = new SkyBlockItem(pet, 1);
        item.getAttributeHandler().setRarity(won.rarity());
        CrimsonMobLoot.deliver(player, mob, item, true);
    }
}
