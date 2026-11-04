package dev.daweapon.type.skyblockgeneric.fishing.grubber;

import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;
import net.minestom.server.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureDrop.always;
import static dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureDrop.chance;
import static dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureDrop.oneIn;
import static dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureDrop.table;

/**
 * The four Mithril Grubbers fished up in the Abandoned Quarry once Guard Gornum has talked to you (wiki: Mithril
 * Grubber, Guard Gornum). Stats, weights, Fishing XP, drops and the Mithril Powder each one gives on death are the
 * wiki's; Powder bonuses (Powder Buff, Mining Fiesta) are not applied to it.
 */
@Getter
public enum MithrilGrubbers {
    MITHRIL_GRUBBER("", 29, 6_000, 75, 290, 34, 250, 8, 16, 2, 4, 1, 1),
    MEDIUM_MITHRIL_GRUBBER("Medium ", 33, 7_500, 100, 330, 39, 500, 16, 24, 4, 8, 2, 2),
    LARGE_MITHRIL_GRUBBER("Large ", 41, 10_000, 150, 410, 49, 1_000, 24, 48, 8, 12, 4, 4),
    BLOATED_MITHRIL_GRUBBER("Bloated ", 49, 15_000, 200, 490, 58, 5_000, 48, 96, 12, 24, 8, 12);

    public static final String MET_GORNUM = "npc.guard_gornum";

    private final long mithrilPowder;
    private final SeaCreatureProfile profile;

    MithrilGrubbers(String size, int level, double health, double damage, long fishingXp, int orbs, long mithrilPowder,
                    int mithrilMin, int mithrilMax, int codMin, int codMax, int enchantedMithril, int plasma) {
        this.mithrilPowder = mithrilPowder;
        this.profile = SeaCreatureProfile.builder(name())
                .displayName(size + "Mithril Grubber").level(level).entityType(EntityType.SILVERFISH)
                .health(health).damage(damage).speed(90)
                .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 16))
                .mobTypes(MobType.AQUATIC, MobType.ARTHROPOD)
                .fishingXpReward(fishingXp).xpOrbs(orbs)
                .catchMessage("A leech of the mines surfaces... you've caught a " + size + "Mithril Grubber.")
                .lootTable(table(always("MITHRIL", mithrilMin, mithrilMax), always("RAW_FISH", codMin, codMax),
                        chance("ENCHANTED_MITHRIL", enchantedMithril, 20), chance("PLASMA", plasma, 2.5),
                        oneIn("DYE_AQUAMARINE", 5_000_000)))
                .build();
    }

    public static final List<SeaCreatureProfile> PROFILES = Arrays.stream(values()).map(MithrilGrubbers::getProfile).toList();

    public static boolean metGornum(SkyBlockPlayer player) {
        return ChapterTriggers.done(player, MET_GORNUM);
    }

    public static void meetGornum(SkyBlockPlayer player) {
        ChapterTriggers.fire(player, MET_GORNUM);
    }

    public static boolean allows(SkyBlockPlayer player, String seaCreatureId) {
        return of(seaCreatureId).isEmpty() || metGornum(player);
    }

    public static Optional<MithrilGrubbers> of(String seaCreatureId) {
        for (MithrilGrubbers grubber : values()) {
            if (grubber.name().equals(seaCreatureId)) return Optional.of(grubber);
        }
        return Optional.empty();
    }

    public static void rewardKill(SkyBlockPlayer player, String seaCreatureId) {
        of(seaCreatureId).ifPresent(grubber -> {
            DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
            data.addPowder(HotmPowder.MITHRIL, grubber.mithrilPowder);
            HotmService.save(player, data);
            player.sendMessage("<2>+" + grubber.mithrilPowder + " Mithril Powder");
        });
    }
}
