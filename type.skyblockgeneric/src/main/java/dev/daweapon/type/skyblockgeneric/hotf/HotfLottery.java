package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum HotfLottery {
    FIG_FORTUNE("<6>+50 Fig Fortune"),
    MANGROVE_FORTUNE("<6>+50 Mangrove Fortune"),
    SWEEP("<a>+5% Sweep");

    private final String description;

    HotfLottery(String description) {
        this.description = description;
    }

    public static HotfLottery current(SkyBlockPlayer player) {
        if (!HotfService.hasPerk(player, "lottery")) return null;
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        long today = HotfService.skyBlockDay();
        HotfLottery stored = byId(data.getLotteryEffect());
        if (data.getLotteryDay() == today && stored != null) return stored;
        HotfLottery rolled = LootRoll.pick(player.getUuid(), Arrays.asList(values()), effect -> null,
                effect -> 1D, ThreadLocalRandom.current());
        data.setLotteryDay(today);
        data.setLotteryEffect(rolled.name());
        HotfService.save(player, data);
        player.sendMessage("<2>New day! Your <a>Lottery<2> effect changed!");
        player.sendMessage("<2>New effect: <7>Gain " + rolled.getDescription() + "<7>.");
        return rolled;
    }

    private static HotfLottery byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
