package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureCatalog;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class HuntingTutorialProgress {
    private HuntingTutorialProgress() {
    }

    public static String huntTrigger(String creatureId) {
        return "hunt." + creatureId.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    public static List<String> huntTriggers(List<CreatureSpec> creatures, CreatureSpec.Capture method) {
        return creatures.stream().filter(spec -> spec.captures(method)).map(spec -> huntTrigger(spec.id())).toList();
    }

    public static boolean huntedWith(SkyBlockPlayer player, CreatureSpec.Capture method) {
        return huntTriggers(CreatureCatalog.all(), method).stream().anyMatch(trigger -> ChapterTriggers.done(player, trigger));
    }

    public static boolean hunted(SkyBlockPlayer player, String creatureId) {
        return ChapterTriggers.done(player, huntTrigger(creatureId));
    }
}
