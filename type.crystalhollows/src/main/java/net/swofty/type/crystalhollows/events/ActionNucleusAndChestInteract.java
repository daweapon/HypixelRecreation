package net.swofty.type.crystalhollows.events;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.swofty.type.crystalhollows.chest.StructureChests;
import net.swofty.type.crystalhollows.temple.JungleTemple;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import net.swofty.type.skyblockgeneric.crystalhollows.DugTreasureChests;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusConfig;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionNucleusAndChestInteract implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (DugTreasureChests.isDugChest(event.getBlockPosition())) {
            event.setBlockingItemUse(true);
            DugTreasureChests.interact(player, event.getBlockPosition());
            return;
        }
        if (JungleTemple.isExitLever(event.getBlockPosition())) {
            event.setBlockingItemUse(true);
            JungleTemple.useExitLever(player);
            return;
        }
        StructureChests.ChestSpec chest = StructureChests.at(event.getBlockPosition());
        if (chest != null) {
            event.setBlockingItemUse(true);
            StructureChests.open(player, chest);
            return;
        }
        NucleusCrystal statue = NucleusConfig.get().statueAt(event.getBlockPosition().blockX(),
                event.getBlockPosition().blockY(), event.getBlockPosition().blockZ());
        if (statue != null) {
            event.setBlockingItemUse(true);
            CrystalNucleus.place(player, statue);
        }
    }
}
