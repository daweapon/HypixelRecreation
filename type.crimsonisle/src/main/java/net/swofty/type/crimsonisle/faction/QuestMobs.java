package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestMobs;
import net.swofty.type.skyblockgeneric.entity.mob.MobRegistry;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;

import java.util.Optional;
import java.util.Set;

/**
 * Spawns quest mobs by id from whatever mob classes are registered, so quests don't depend on the
 * mob classes directly. Returns empty when no such mob exists yet.
 */
public final class QuestMobs {
    private QuestMobs() {
    }

    public static Optional<SkyBlockMob> spawn(Set<String> ids, Pos at) {
        for (MobRegistry registry : MobRegistry.REGISTERED_MOBS) {
            if (registry.getMobCache() == null || !CrimsonQuestMobs.matches(registry.getMobCache(), ids)) continue;
            SkyBlockMob mob = registry.asMob();
            if (mob == null) return Optional.empty();
            mob.setInstance(HypixelConst.getInstanceContainer(), at);
            return Optional.of(mob);
        }
        return Optional.empty();
    }
}
