package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// The Dean's Apprentice, lying unconscious north of The Dukedom during the Mage questline.
public class NPCApprentice extends CrimsonNPC {
    public NPCApprentice() {
        super("<5>Apprentice", CrimsonSkins.APPRENTICE, new Pos(-521, 104.187, -917));
    }

    @Override
    protected boolean visible(SkyBlockPlayer player) {
        MageStep step = CrimsonQuestlines.mage(player);
        return step == MageStep.FIND_APPRENTICE || step == MageStep.CALL_AHONE;
    }

    @Override
    protected EntityPose pose(SkyBlockPlayer player) {
        return EntityPose.SLEEPING;
    }

    @Override
    protected String[] holograms(SkyBlockPlayer player) {
        return new String[]{"<5>Apprentice", "<7>Unconscious"};
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (CrimsonQuestlines.advance(player, MageStep.FIND_APPRENTICE, MageStep.CALL_AHONE, 0)) {
            player.sendMessage("<7>The Dean's Apprentice is badly hurt and unconscious. <e>Call Captain Ahone<7> with your Abiphone!");
        } else {
            player.sendMessage("<7>The Apprentice is unconscious.");
        }
    }
}
