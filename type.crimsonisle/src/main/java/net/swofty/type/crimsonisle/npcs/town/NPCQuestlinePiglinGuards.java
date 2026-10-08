package net.swofty.type.crimsonisle.npcs.town;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.skyblockgeneric.crimson.questline.BarbarianStep;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumSet;
import java.util.Set;

/**
 * The Piglin Guards of the main questlines (wiki Piglin Guard): two flank Chief Scorn while he throws
 * people out of his hut, and two stand at the Forgotten Skull meeting at the end of either questline.
 * The two Bastion guards of the Mage questline are WS-Q's rescue-mission guards, shown for that step too.
 */
public final class NPCQuestlinePiglinGuards {
    private static final String NAME = "<c>Piglin Guard";
    private static final String RESTRICTED = "Hey, this is a restricted area. Leave now!";
    /** The Barbarian steps around Chief Scorn's three "escort them out" scenes. */
    private static final Set<BarbarianStep> CHIEF_SCENES = EnumSet.of(BarbarianStep.ESCORT_UGO, BarbarianStep.TALK_TO_UGO,
            BarbarianStep.PRESENT_RESEARCH, BarbarianStep.UGO_TOWN_BOARD, BarbarianStep.TALK_TO_CHIEF_FINAL);

    private NPCQuestlinePiglinGuards() {
    }

    private abstract static class Guard extends CrimsonNPC {
        Guard(Pos home) {
            super(NAME, EntityType.PIGLIN_BRUTE, home);
        }

        @Override
        protected String[] holograms(SkyBlockPlayer player) {
            return new String[]{NAME};
        }

        @Override
        protected void interact(SkyBlockPlayer player) {
            say(player, RESTRICTED);
        }
    }

    private abstract static class ChiefsHutGuard extends Guard {
        ChiefsHutGuard(Pos home) {
            super(home);
        }

        @Override
        protected boolean visible(SkyBlockPlayer player) {
            return CHIEF_SCENES.contains(CrimsonQuestlines.barbarian(player));
        }
    }

    private abstract static class WastelandGuard extends Guard {
        WastelandGuard(Pos home) {
            super(home);
        }

        @Override
        protected boolean visible(SkyBlockPlayer player) {
            return CrimsonQuestlines.isOnForgottenSkullMeeting(player);
        }
    }

    public static class ChiefsHutNorth extends ChiefsHutGuard {
        public ChiefsHutNorth() {
            super(new Pos(-586.08, 115.06, -688.5, -90, 0));
        }
    }

    public static class ChiefsHutSouth extends ChiefsHutGuard {
        public ChiefsHutSouth() {
            super(new Pos(-586.08, 115.06, -686.5, -90, 0));
        }
    }

    public static class WastelandNorth extends WastelandGuard {
        public WastelandNorth() {
            super(new Pos(-367.5, 117, -977.5));
        }
    }

    public static class WastelandSouth extends WastelandGuard {
        public WastelandSouth() {
            super(new Pos(-367.5, 117, -975.5));
        }
    }
}
