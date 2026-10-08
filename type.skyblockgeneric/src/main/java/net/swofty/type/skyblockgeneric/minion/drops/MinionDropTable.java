package net.swofty.type.skyblockgeneric.minion.drops;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public record MinionDropTable(List<MinionDrop> drops) {
    public record Drop(ItemType item, int amount) {
    }

    public static MinionDropTable of(MinionDrop... drops) {
        return new MinionDropTable(List.of(drops));
    }

    public List<Drop> roll(RandomGenerator random) {
        List<Drop> rolled = new ArrayList<>();
        for (MinionDrop drop : drops) {
            if (!drop.guaranteed() && !LootRoll.roll(null, drop.item(), drop.chancePercent(), random)) continue;
            int amount = drop.minAmount() + (int) (random.nextDouble() * (drop.maxAmount() - drop.minAmount() + 1));
            rolled.add(new Drop(drop.item(), amount));
        }
        return rolled;
    }

    public List<SkyBlockItem> rollItems() {
        return roll(ThreadLocalRandom.current()).stream()
                .map(drop -> new SkyBlockItem(drop.item(), drop.amount()))
                .toList();
    }

    public Set<ItemType> producedItems() {
        Set<ItemType> items = new LinkedHashSet<>();
        drops.forEach(drop -> items.add(drop.item()));
        return items;
    }
}
