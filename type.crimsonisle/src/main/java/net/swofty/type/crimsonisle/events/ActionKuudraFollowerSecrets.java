package net.swofty.type.crimsonisle.events;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crimsonisle.npcs.kuudra.NPCKuudraLoremaster;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.FollowerStep;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import net.swofty.type.skyblockgeneric.gui.inventories.kuudra.GUIKuudraClaimReward;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// The Kuudra Follower line's world secrets: the Cathedral bookshelf, the Relic dig site and the Loremaster's chat lines.
public class ActionKuudraFollowerSecrets implements HypixelEventClass {
    private static final BlockVec CATHEDRAL_BOOKSHELF = new BlockVec(10, 127, -938);
    private static final BlockVec RELIC_DIG_SITE = new BlockVec(-373, 82, -880);

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onInteract(PlayerBlockInteractEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player) || !KuudraFollowerRules.npcsVisible(player)) return;
        BlockVec clicked = new BlockVec(event.getBlockPosition().blockX(), event.getBlockPosition().blockY(),
                event.getBlockPosition().blockZ());
        if (clicked.equals(CATHEDRAL_BOOKSHELF)) {
            readBookshelf(player);
        } else if (clicked.equals(RELIC_DIG_SITE)) {
            digRelic(player);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onChat(PlayerChatEvent event) {
        if (event.getPlayer() instanceof SkyBlockPlayer player) NPCKuudraLoremaster.hear(player, event.getRawMessage());
    }

    private static void readBookshelf(SkyBlockPlayer player) {
        if (KuudraFollowerRules.intelligence(player) < KuudraFollowerRules.RELIC_INTELLIGENCE) return;
        player.sendMessage("<a>You use your mana to pull a book out. Most of it is unreadable but you do notice a 2 and a 22...");
        KuudraFollowerProgress.complete(player, FollowerStep.BOOKSHELF_READ);
    }

    private static void digRelic(SkyBlockPlayer player) {
        if (!KuudraFollowerRules.holdingDrill(player) || !KuudraFollowerRules.isCataclysmicEruption()) return;
        if (player.countItem(ItemType.KUUDRA_RELIC) > 0) return;
        player.openView(new GUIKuudraClaimReward(ItemType.KUUDRA_RELIC, claimer -> {
            if (claimer.countItem(ItemType.KUUDRA_RELIC) > 0) return;
            claimer.addAndUpdateItem(ItemType.KUUDRA_RELIC);
            KuudraFollowerProgress.complete(claimer, FollowerStep.RELIC_FOUND);
        }));
    }
}
