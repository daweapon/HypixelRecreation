package dev.daweapon.type.theend.events;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobSeer;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.theend.npcs.LoneAdventurerProgress;
import dev.daweapon.type.theend.npcs.LoneAdventurerStage;

/**
 * Seer page: "Can now drop Dragon Shortbow once, after progressing Lone Adventurer's questline." It only drops
 * while the quest asks for it and the player isn't already carrying one. The wiki has no chance: estimate.
 */
public class ActionSeerDragonShortbow implements HypixelEventClass {
    public static final double SHORTBOW_CHANCE = 5;

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        if (!(event.getKilledMob() instanceof MobSeer seer)) return;
        SkyBlockPlayer player = event.getPlayer();
        if (LoneAdventurerProgress.stage(player) != LoneAdventurerStage.SHORTBOW) return;
        if (player.countItem(ItemType.DRAGON_SHORTBOW) > 0) return;
        if (!LootRoll.roll(player.getUuid(), ItemType.DRAGON_SHORTBOW, SHORTBOW_CHANCE)) return;
        CrimsonMobLoot.deliver(player, seer, new SkyBlockItem(ItemType.DRAGON_SHORTBOW, 1), true);
    }
}
