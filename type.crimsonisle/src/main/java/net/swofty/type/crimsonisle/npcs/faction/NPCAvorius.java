package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

// Avorius, definitely not a Vampire, takes a Cup of Blood every 3 days for +5 Mage Reputation (wiki: Avorius).
public class NPCAvorius extends CrimsonNPC {
    public static final int REPUTATION = 5;
    public static final Duration COOLDOWN = Duration.ofDays(3);

    public NPCAvorius() {
        super("<c>Avorius", CrimsonSkins.AVORIUS, new Pos(-152.5, 128, -809.5));
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (!CrimsonReputationService.isMember(player, CrimsonFaction.MAGE)) {
            say(player, "I can't be seen conversing with you right now!");
            return;
        }
        boolean ready = CrimsonSideQuests.isReady(player, CrimsonSideQuests.AVORIUS_LAST, COOLDOWN);
        if (ready && player.getAmountInInventory(ItemType.CUP_OF_BLOOD) > 0) {
            player.removeItemFromPlayer(ItemType.CUP_OF_BLOOD, 1);
            CrimsonSideQuests.flag(player, CrimsonSideQuests.AVORIUS_EVER);
            CrimsonSideQuests.stamp(player, CrimsonSideQuests.AVORIUS_LAST);
            CrimsonReputationService.add(player, CrimsonFaction.MAGE, REPUTATION, "Avorius");
            say(player, "A Cup of Blood? What a strange present, I'm sure a Vampire would like that.",
                    "Thanks for the gift. I really appreciate it. Of course I won't be drinking that.");
            return;
        }
        if (CrimsonSideQuests.firstMeeting(player, "avorius")) {
            say(player, "Have you noticed how everyone around here kind of resembles a Vampire?",
                    "It would be a pretty convenient place for a Vampire to hide.",
                    "Not that I'm a Vampire of course - I am definitely not.");
            return;
        }
        if (!ready) {
            say(player, "My thirst is quenched!", "Err, from water and other non-Vampire liquids that is.",
                    "Thank you for the gesture though!");
            return;
        }
        sayRandom(player, "I am quite thirsty all the time, it's a rare condition.",
                "Why are you looking at me that way? I am not a Vampire, you are!",
                "There is no sunlight either, it would be quite accommodating for a Vampire.");
    }
}
