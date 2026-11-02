package dev.daweapon.type.skyblockgeneric.levels;

import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerDefinition;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRegistry;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerTier;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneFight;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class XPSubTaskProgress {
    public static final List<String> DRAGONS = List.of("SUPERIOR", "OLD", "YOUNG", "WISE", "PROTECTOR", "UNSTABLE", "STRONG");

    private XPSubTaskProgress() {
    }

    public static String dragonSlainKey(String variant) {
        return "the_end:dragon_slain:" + variant;
    }

    public static boolean completed(SkyBlockXPSource source, int index, SkyBlockPlayer player) {
        return switch (source) {
            case SLAY_DRAGONS -> DatapointCrimsonIsle.of(player).getNumber(dragonSlainKey(DRAGONS.get(index))) > 0;
            case DEFEAT_SLAYERS -> slayerCleared(player, SlayerType.values()[index]);
            case DEFEAT_KUUDRA -> KuudraProgress.hasCleared(player, KuudraTier.values()[index]);
            case DEFEAT_ARACHNE -> ArachneFight.hasDefeated(player, ArachneTier.values()[index]);
            default -> false;
        };
    }

    public static void slayAllDragons(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> DRAGONS.forEach(variant -> data.getNumbers().put(dragonSlainKey(variant), 1L)));
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.SLAY_DRAGONS, SkyBlockXPSource.SLAY_DRAGONS.getMaxXP());
    }

    private static boolean slayerCleared(SkyBlockPlayer player, SlayerType type) {
        SlayerDefinition definition = SlayerRegistry.get(type);
        if (definition == null || !definition.enabled() || definition.tiers().isEmpty()) return false;
        DatapointSlayer.SlayerProgress progress = player.getSlayerData().progress(type);
        for (SlayerTier tier : definition.tiers().keySet()) {
            if (progress.completions(tier) == 0) return false;
        }
        return true;
    }
}
