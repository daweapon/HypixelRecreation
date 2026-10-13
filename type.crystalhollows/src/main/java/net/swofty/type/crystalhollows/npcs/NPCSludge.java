package net.swofty.type.crystalhollows.npcs;

import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

public class NPCSludge extends AbstractStructureNPC {
    public static final long GEMSTONE_POWDER_REWARD = 2_500;

    public NPCSludge() {
        super("Sludge", "<a>", DEFAULT_TEXTURE, "sludge_cage", "sludge");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        UUID id = player.getUuid();
        if (CrystalHollowsProgress.has(id, CrystalHollowsProgress.SLUDGE_FREED)) {
            sendNPCMessage(player, "<f>Thanks again! I'd been stuck there for weeks.");
            return;
        }
        int hits = CrystalHollowsProgress.hitSludge(id);
        if (hits < CrystalHollowsProgress.SLUDGE_HITS_TO_FREE) {
            sendNPCMessage(player, "<f>Help me, I'm stuck! Punch me out of here!");
            return;
        }
        CrystalHollowsProgress.mark(id, CrystalHollowsProgress.SLUDGE_FREED);
        long powder = HotmService.gainPowder(player, HotmPowder.GEMSTONE, GEMSTONE_POWDER_REWARD);
        sendNPCMessage(player, "<f>You freed me! I'd been stuck there for weeks. Here, take this for your trouble.");
        player.sendMessage("<d>You received <a>" + powder + " <d>Gemstone Powder!");
    }
}
