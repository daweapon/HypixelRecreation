package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Igrupan's 5 runaway chickens (wiki: Igrupan's Chickens) at their wiki spots. Hitting one sends it
 * home to the coop (+50 Mage Reputation once all 5 are back).
 */
public final class IgrupanChickens {
    public static final List<String> NAMES = List.of("Henrietta", "Chikira", "Chickovsky", "Cluckles", "Hennifer");
    private static final String IGRUPAN = "<5>Igrupan";

    private IgrupanChickens() {
    }

    public static boolean isHome(SkyBlockPlayer player, String chicken) {
        return CrimsonSideQuests.is(player, CrimsonSideQuests.CHICKEN + chicken);
    }

    public static long returned(SkyBlockPlayer player) {
        return NAMES.stream().filter(name -> isHome(player, name)).count();
    }

    public static boolean isSearching(SkyBlockPlayer player) {
        return CrimsonQuestlines.mage(player) == MageStep.HELP_IGRUPAN && CrimsonQuestlines.counter(player, "igrupan_started") > 0;
    }

    public static void bringHome(SkyBlockPlayer player, String chicken) {
        if (!isSearching(player) || isHome(player, chicken)) return;
        CrimsonSideQuests.flag(player, CrimsonSideQuests.CHICKEN + chicken);
        player.sendMessage("<a>You sent <e>" + chicken + "<a> back to Igrupan's coop! <7>(" + returned(player) + "/5)");
        String[] lines = {
                "Oh " + chicken + " was missing too? Wondered why it was so peaceful.",
                chicken + " Jr., what time to do you call this?",
                "Oh " + chicken + ", I'm so glad to have you back!",
                "Finally! " + chicken + " has been returned to me!",
                "Oh there they are! Nice to see you again Regi... I mean " + chicken + "."};
        CrimsonScene scene = CrimsonScene.create().say(IGRUPAN, lines[ThreadLocalRandom.current().nextInt(lines.length)]);
        if (returned(player) >= NAMES.size()) {
            scene.say(IGRUPAN, "I don't know how to thank you!", "You just saved my business!",
                    "Come back in a few days and I'll reward you with my first chicken!");
            scene.play(player).thenRun(() -> CrimsonQuestlines.advance(player, MageStep.HELP_IGRUPAN, MageStep.HELP_ODEXAR, 50));
            return;
        }
        scene.play(player);
    }

    /** One runaway chicken. */
    public abstract static class Chicken extends CrimsonNPC {
        private final String chicken;

        protected Chicken(String chicken, Pos spot) {
            super("<f>" + chicken, EntityType.CHICKEN, spot);
            this.chicken = chicken;
        }

        @Override
        protected boolean visible(SkyBlockPlayer player) {
            return isSearching(player) && !isHome(player, chicken);
        }

        @Override
        protected String[] holograms(SkyBlockPlayer player) {
            return new String[]{"<f>" + chicken, "<e><l>HIT"};
        }

        @Override
        protected void interact(SkyBlockPlayer player) {
            bringHome(player, chicken);
        }
    }
}
