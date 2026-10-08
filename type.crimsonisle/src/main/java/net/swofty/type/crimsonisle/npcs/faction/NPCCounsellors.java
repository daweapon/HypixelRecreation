package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crimsonisle.faction.CounsellorNPC;

// The three Mage Council Counsellors (wiki: Counsellor).
public final class NPCCounsellors {
    private NPCCounsellors() {
    }

    public static class First extends CounsellorNPC {
        public First() {
            super(new Pos(-125.5, 82, -756.5), "I'm not that smart, they just needed a third person.");
        }
    }

    public static class Second extends CounsellorNPC {
        public Second() {
            super(new Pos(-126.5, 82, -754.5), "I like voicing my opinion.");
        }
    }

    public static class Third extends CounsellorNPC {
        public Third() {
            super(new Pos(-125.5, 82, -752.5), "This side of the table isn't as valuable as the other side.");
        }
    }
}
