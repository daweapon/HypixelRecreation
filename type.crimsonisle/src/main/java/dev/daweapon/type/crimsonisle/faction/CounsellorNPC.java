package dev.daweapon.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.MageStep;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** The three Counsellors at the Mage Council table, there only for the stolen-research meeting. */
public abstract class CounsellorNPC extends CrimsonNPC {
    private final String line;

    protected CounsellorNPC(Pos seat, String line) {
        super("<f>Counsellor", CrimsonSkins.COUNSELLOR, seat);
        this.line = line;
    }

    @Override
    protected boolean visible(SkyBlockPlayer player) {
        return CrimsonQuestlines.mage(player) == MageStep.MAGE_COUNCIL;
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        say(player, line);
    }

    @Override
    public String getName() {
        return "Counsellor " + home().blockX() + "," + home().blockZ();
    }
}
