package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum HotfBeekeeper {
    HIVE_REFILL("<6>Honeyhives<7> refill <a>20%<7> faster"),
    FASTER_CRITTERS("<a>Trees<7> lathered with <6>Honeycomb<7> attract <a>Critters 20%<7> faster"),
    EXTRA_HONEYCOMB("<7>Gain <6>2 Honeycomb<7> from <6>Honeyhives<7> and lathering <a>Trees<7> with <6>Honeycomb"),
    SECOND_CRITTER("<a>Trees<7> lathered with <6>Honeycomb<7> have a <a>25%<7> chance to attract a second <a>Critter");

    public static final double FASTER_PERCENT = 20;
    public static final double SECOND_CRITTER_PERCENT = 25;
    public static final int EXTRA_HONEYCOMB_AMOUNT = 2;

    private final String description;

    HotfBeekeeper(String description) {
        this.description = description;
    }

    public static HotfBeekeeper current(SkyBlockPlayer player) {
        if (!HotfService.hasPerk(player, "beekeeper")) return null;
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        long today = HotfService.skyBlockDay();
        HotfBeekeeper stored = byId(data.getBeekeeperEffect());
        if (data.getBeekeeperDay() == today && stored != null) return stored;
        HotfBeekeeper rolled = LootRoll.pick(player.getUuid(), Arrays.asList(values()), effect -> null,
                effect -> 1D, ThreadLocalRandom.current());
        data.setBeekeeperDay(today);
        data.setBeekeeperEffect(rolled.name());
        HotfService.save(player, data);
        player.sendMessage("<2>New day! Your <a>Beekeeper<2> effect changed!");
        player.sendMessage("<2>New effect: " + rolled.getDescription() + "<7>.");
        return rolled;
    }

    public static boolean active(SkyBlockPlayer player, HotfBeekeeper effect) {
        return current(player) == effect;
    }

    public static double speedFactor(boolean boosted) {
        return boosted ? 1 - FASTER_PERCENT / 100 : 1;
    }

    private static HotfBeekeeper byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
