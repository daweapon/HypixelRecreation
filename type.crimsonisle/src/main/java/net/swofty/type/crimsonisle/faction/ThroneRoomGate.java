package net.swofty.type.crimsonisle.faction;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.crimson.questline.UniversalStep;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

import static net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene.GRELIUS;
import static net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene.KREVIUS;

/**
 * The Throne Room is for Mage Faction members only. Krevius and Grelius also let a player through
 * who shows them the Dean's Letter of Recommendation while joining the Mages.
 */
public final class ThroneRoomGate {
    public static final Pos OUTSIDE = new Pos(-134.5, 101, -754.5, 90, 0);
    public static final Pos CHIEFS_HUT_OUTSIDE = new Pos(-597.5, 109, -668.5);
    private static final double CHIEFS_HUT_FLOOR_Y = 113;
    private static final String PASS = "throne_pass";
    private static final String[] RANDOM = {"DO YOU WANT TO BE PULVERIZED?", "WHAT DO YOU WANT?", "WE HAVE OUR EYES ON YOU."};

    private ThroneRoomGate() {
    }

    public static boolean mayEnter(SkyBlockPlayer player) {
        if (CrimsonReputationService.isMember(player, CrimsonFaction.MAGE)) return true;
        return CrimsonQuestlines.mage(player) == MageStep.DELIVER_LETTER && CrimsonQuestlines.counter(player, PASS) > 0;
    }

    public static void turnAway(SkyBlockPlayer player) {
        player.teleport(OUTSIDE);
        if (CrimsonQuestlines.universal(player) == UniversalStep.EXPLORE) {
            ElleQuest.explainFactions(player, false);
            return;
        }
        player.sendMessage("<c>You cannot enter the Throne Room unless you are a member of the <5>Mage Faction<c>!");
    }

    /** Before choosing a faction the Chief's Hut is off limits too; Elle stops the player there. */
    public static boolean mayEnterChiefsHut(SkyBlockPlayer player) {
        return CrimsonQuestlines.universal(player) == UniversalStep.COMPLETE;
    }

    /**
     * The Chief's Hut region also covers the square in front of it, where Elle waits and where a
     * turned-away player is put back, so only the hut's raised floor is closed off.
     */
    public static boolean isInsideChiefsHut(SkyBlockPlayer player) {
        return player.getPosition().y() >= CHIEFS_HUT_FLOOR_Y;
    }

    public static void turnAwayFromChiefsHut(SkyBlockPlayer player) {
        player.teleport(CHIEFS_HUT_OUTSIDE);
        ElleQuest.explainFactions(player, true);
    }

    public static void talkToGuards(SkyBlockPlayer player) {
        if (CrimsonScene.isPlaying(player)) return;
        boolean hasLetter = CrimsonQuestlines.mage(player) == MageStep.DELIVER_LETTER
                && player.getAmountInInventory(ItemType.DEAN_LETTER_OF_RECOMMENDATION) > 0
                && CrimsonQuestlines.counter(player, PASS) == 0;
        if (!hasLetter) {
            String line = RANDOM[ThreadLocalRandom.current().nextInt(RANDOM.length)];
            CrimsonScene.create().say(KREVIUS, line).say(GRELIUS, line).play(player);
            return;
        }
        CrimsonScene.create().say(KREVIUS, "WHY ARE YOU HERE?").say(GRELIUS, "WHY ARE YOU HERE?").play(player)
                .thenRun(() -> CrimsonChoices.pick(player, "throne_guards",
                        "I'm here to cause trouble, of course", NamedTextColor.RED,
                        () -> CrimsonScene.create().say(KREVIUS, "DO YOU WANT TO BE PULVERIZED?")
                                .say(GRELIUS, "DO YOU WANT TO BE PULVERIZED?").play(player),
                        "I have a letter of recommendation", NamedTextColor.GREEN,
                        () -> {
                            CrimsonQuestlines.setCounter(player, PASS, 1);
                            CrimsonScene.create().say(KREVIUS, "FINE, YOU MAY PASS!").say(GRELIUS, "FINE, YOU MAY PASS!").play(player);
                        }));
    }
}
