package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerks;
import dev.daweapon.type.skyblockgeneric.essence.FossilEssencePerk;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.instance.Instance;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CutLoose {
    public static final int MAX_STACKS = 10;
    private static final Map<UUID, Stacks> STACKS = new ConcurrentHashMap<>();

    private record Stacks(Instance instance, int count) {
    }

    private CutLoose() {
    }

    public static void onGlacialKill(SkyBlockPlayer player) {
        if (ShaftCorpses.of(player.getInstance()).isEmpty() || EssencePerks.value(player, FossilEssencePerk.CUT_LOOSE) <= 0) return;
        Stacks current = STACKS.get(player.getUuid());
        int count = current != null && current.instance() == player.getInstance() ? current.count() : 0;
        STACKS.put(player.getUuid(), new Stacks(player.getInstance(), Math.min(MAX_STACKS, count + 1)));
    }

    public static int stacks(SkyBlockPlayer player) {
        Stacks current = STACKS.get(player.getUuid());
        return current != null && current.instance() == player.getInstance() ? current.count() : 0;
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        int stacks = stacks(player);
        if (stacks == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.COLD_RESISTANCE, stacks * EssencePerks.value(player, FossilEssencePerk.CUT_LOOSE)).build();
    }
}
