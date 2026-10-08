package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.crimsonisle.faction.FactionVillagerNPC;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Suus and her crying son Turd (wiki: Suus, Turd): a Magma Cube Rider ate Turd's toy (10% to find
 * per kill); returning it gives +40 Barbarian Reputation, at most every 2 days.
 */
public final class NPCSuus {
    public static final int REPUTATION = 40;

    private NPCSuus() {
    }

    public static class Suus extends FactionVillagerNPC implements NPCAbiphoneTrait {
        public Suus() {
            super("<d>Suus", CrimsonSkins.SUUS, new Pos(-615.5, 115, -706.5), CrimsonFaction.BARBARIAN);
        }

        @Override
        public String getAbiphoneKey() {
            return "suus";
        }

        @Override
        protected void talk(SkyBlockPlayer player) {
            if (!isMember(player)) {
                sayRandom(player, "Get away from my child!", "Step away from my child, stranger!",
                        "Don't talk to strangers, little Turd.", "Stop harassing me and my child!");
                return;
            }
            if (CrimsonSideQuests.firstMeeting(player, "suus")) {
                say(player, "My kid has been crying for hours.", "We just came back from our walk and a monster ate his favorite toy.",
                        "One of the <c>Magma Cube Riders<f> out there did it!");
                return;
            }
            if (!CrimsonSideQuests.is(player, CrimsonSideQuests.SUUS_TOY)) {
                sayRandom(player, "I don't think he will ever calm down until he gets his toy back.", "This kid is a handful.",
                        "Maybe one of the <c>" + CrimsonSideQuests.suusMobName(player) + "s <f>out there will have his toy.");
                return;
            }
            boolean first = !CrimsonSideQuests.is(player, CrimsonSideQuests.SUUS_EVER);
            CrimsonSideQuests.set(player, CrimsonSideQuests.SUUS_TOY, 0);
            CrimsonSideQuests.set(player, CrimsonSideQuests.SUUS_MOB, 0);
            CrimsonSideQuests.flag(player, CrimsonSideQuests.SUUS_EVER);
            CrimsonSideQuests.stamp(player, CrimsonSideQuests.SUUS_LAST);
            CrimsonReputationService.add(player, CrimsonFaction.BARBARIAN, REPUTATION, "Suus");
            if (first) {
                say(player, "You found his toy!", "Thank you so much <player>!",
                        "My little Turd has such a lovely voice, I don't mind hearing him cry.",
                        "Such an expensive toy, I am glad you found it for him!");
            } else {
                say(player, "You found his toy!", "Thanks for finding his toy again, <player>!", "He keeps losing it...");
            }
        }
    }

    public static class Turd extends FactionVillagerNPC {
        public Turd() {
            super("<d>Turd", EntityType.PIGLIN, new Pos(-616.5, 115, -708.5), CrimsonFaction.BARBARIAN);
        }

        @Override
        protected void talk(SkyBlockPlayer player) {
            boolean calm = CrimsonSideQuests.is(player, CrimsonSideQuests.SUUS_EVER)
                    && !CrimsonSideQuests.isReady(player, CrimsonSideQuests.SUUS_LAST, CrimsonSideQuests.SUUS_COOLDOWN);
            if (calm) {
                sayRandom(player, "My toy. MINE!", "I don't want to play with you.", "Mom!!!! Tell the weird stranger that it's MY toy!");
                return;
            }
            sayRandom(player, "waaaaaaaaa", "WAAAAAAAHHH", "waaaaaaaaaaaaaa", "waaaaaa", "<c>WAAAAAAAAAAAAAH");
        }
    }
}
