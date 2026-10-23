package dev.daweapon.type.thefarmingislands.events;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.farming.lab.LabQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.thefarmingislands.lab.LabFixtures;
import dev.daweapon.type.thefarmingislands.lab.LabLever;
import dev.daweapon.type.thefarmingislands.lab.LabScenes;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerMoveEvent;

// Starts the Underground Lab scenes when a player with the right quest progress walks up to Beth
public class ActionUndergroundLab implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        Pos position = event.getNewPosition();
        if (!LabScenes.insideLab(position)) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        LabQuest.Stage stage = LabQuest.stage(player);
        if (player.getInstance() != null) LabFixtures.ensure(player.getInstance());
        if (stage.compareTo(LabQuest.Stage.LAB2_PENDING) >= 0 && player.getInstance() != null) {
            LabLever.ensure(player.getInstance());
        }
        switch (stage) {
            case LAB1 -> {
                if (position.distance(LabScenes.FIRST_ROOM) <= LabScenes.TRIGGER_RANGE) LabScenes.firstVisit(player);
            }
            case LAB2_PENDING -> {
                if (position.distance(LabScenes.SECOND_VISIT_START) <= LabScenes.TRIGGER_RANGE) LabScenes.secondVisitStart(player);
            }
            case LAB2_LEVER -> {
                if (position.distance(LabScenes.HIDDEN_SECTION_BETH) <= LabScenes.TRIGGER_RANGE) LabScenes.revealCropAnalyzer(player);
            }
            default -> {
            }
        }
    }
}
