package net.swofty.type.skyblockgeneric.crystalhollows;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.random.RandomGenerator;

public final class TreasureLoot {
    public static final int MIN_ROLLS = 3;
    public static final int MAX_ROLLS = 6;
    private static final AttributeId BIGGER_BOX = AttributeId.parse("U7");

    private TreasureLoot() {
    }

    public static void open(SkyBlockPlayer player, TreasurePool pool, RandomGenerator lootRandom, RandomGenerator amountRandom) {
        int rolls = MIN_ROLLS + lootRandom.nextInt(MAX_ROLLS - MIN_ROLLS + 1);
        double box = AttributeEffectService.value(player.getHuntingData(), BIGGER_BOX);
        if (box > 0 && LootRoll.roll(player.getUuid(), null, box)) rolls++;

        Map<String, Long> summary = new LinkedHashMap<>();
        long powder = 0;
        for (int i = 0; i < rolls; i++) {
            TreasurePool.Entry entry = LootRoll.pick(player.getUuid(), pool.entries(), TreasurePool.Entry::item,
                    TreasurePool.Entry::weight, lootRandom);
            RandomGenerator source = entry.powder() ? amountRandom : lootRandom;
            int amount = entry.min() + source.nextInt(entry.max() - entry.min() + 1);
            if (entry.powder()) {
                powder += HotmService.gainPowder(player, HotmPowder.GEMSTONE, amount);
                continue;
            }
            SkyBlockItem reward = new SkyBlockItem(entry.item(), amount);
            player.addAndUpdateItem(reward);
            summary.merge(reward.getDisplayName(), (long) amount, Long::sum);
        }
        player.sendMessage(Text.of("<6><l>TREASURE CHEST <r><7>You looted the chest:"));
        if (powder > 0) player.sendMessage(Text.of("  <d>+{} Gemstone Powder", String.format("%,d", powder)));
        summary.forEach((name, amount) -> player.sendMessage(Text.of("  <8>{}x <f>{}", amount, name)));
    }
}
