package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** Velyna reads the player's future in the flames (wiki: Velyna). */
public class VelynaNPC extends FactionVillagerNPC {
    private static final String READ = "velyna:read";

    public VelynaNPC() {
        super("<5>Velyna", CrimsonSkins.VELYNA, new Pos(-86.5, 104.5, -820.5), CrimsonFaction.MAGE);
    }

    @Override
    protected void talk(SkyBlockPlayer player) {
        if (CrimsonSideQuests.is(player, READ)) {
            if (KuudraProgress.hasDefeatedKuudra(player)) {
                say(player, "Disturbing... the visions must have been wrong.", "But the flames are never wrong...");
            } else {
                sayRandom(player, "I also saw some pretty embarrassing stuff...", "Not everyone wants to see their future, but they should.");
            }
            return;
        }
        String[] opening = CrimsonSideQuests.firstMeeting(player, "velyna")
                ? new String[]{"The more I look into the fire, the more I understand.",
                "I knew you would come, I saw it in the flames a few minutes ago.",
                "Do you know why we can see the future inside the flames?", "I know!",
                "It's because <e>fire<f> is eternal.",
                "I could extinguish it now and light it back up in a year. It would still be the same flames.",
                "If you want, I can read your future. Are you interested?"}
                : new String[]{"Well? Shall I read your future in the flames?"};
        say(player, opening).thenRun(() -> CrimsonChoices.yesNo(player, "velyna", () -> readFuture(player),
                () -> say(player, "I knew you would say that.", "You just wanted to see what I would say, and then ask again.",
                        "How predictable.", "In any case, I'll read your future.").thenRun(() -> readFuture(player))));
    }

    private void readFuture(SkyBlockPlayer player) {
        CrimsonSideQuests.flag(player, READ);
        if (KuudraProgress.hasDefeatedKuudra(player)) {
            say(player, "...", "The flames are dancing strangely...", "I see... I see you... Fighting... The vision is quite blurry...",
                    "You are holding a sword... But how...", "That sword, the emblems on it...", "This future... Impossible...");
            return;
        }
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction == null) say(player, "I see that one day you will join our faction... You shall will become a great asset...");
        else if (faction == CrimsonFaction.MAGE) say(player, "I see that you will betray us one day, and join the Barbarian faction...");
        else say(player, "Ohh... I see you... Fighting a monstrous creature... With giant tentacles...");
    }
}
