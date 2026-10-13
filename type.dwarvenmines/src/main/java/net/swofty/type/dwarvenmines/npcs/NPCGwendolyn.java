package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.dwarvenmines.gui.GUIGwendolyn;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrystalHollows;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCGwendolyn extends AbstractTalkingNPC {
    private static final String INTRO_FLAG = "met_gwendolyn";
    private static final String SPECIAL_FLAG = "gwendolyn_special_hint";

    public NPCGwendolyn() {
        super("Gwendolyn", "<d>",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY3NGMxMjRhMmQ1NDM1OWMyZmRkMDg2Mjk4ODcwNjYwODkzNWU2MGVmNzQ1ODg2N2E5MGJlMDEyMjM3ZjVkZjgifX19",
                new Pos(88.5, 198, -98.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        String missing = CrystalHollowsAccess.missingRequirement(player);
        if (missing != null) {
            setDialogue(player, "locked");
            return;
        }
        if (CrystalHollowsAccess.canUnlockPermanent(player)) {
            CrystalHollowsAccess.unlockPermanent(player);
            setDialogue(player, "permanent").thenRun(() -> player.openView(new GUIGwendolyn()));
            return;
        }
        DatapointCrystalHollows.CrystalHollowsData data = CrystalHollowsAccess.data(player);
        if (!data.hasFlag(INTRO_FLAG)) {
            setDialogue(player, "intro").thenRun(() -> {
                markFlag(player, INTRO_FLAG);
                player.openView(new GUIGwendolyn());
            });
            return;
        }
        if (needsDulinHint(player, data)) {
            setDialogue(player, "special").thenRun(() -> {
                markFlag(player, SPECIAL_FLAG);
                player.openView(new GUIGwendolyn());
            });
            return;
        }
        player.openView(new GUIGwendolyn());
    }

    private static boolean needsDulinHint(SkyBlockPlayer player, DatapointCrystalHollows.CrystalHollowsData data) {
        return HotmService.data(player).getTier() >= CrystalHollowsAccess.PERMANENT_HOTM_TIER
                && !data.hasFlag(CrystalHollowsAccess.HELPED_DULIN_FLAG) && !data.hasFlag(SPECIAL_FLAG);
    }

    private static void markFlag(SkyBlockPlayer player, String flag) {
        DatapointCrystalHollows.CrystalHollowsData data = CrystalHollowsAccess.data(player);
        data.getFlags().add(flag);
        CrystalHollowsAccess.save(player, data);
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        String missing = CrystalHollowsAccess.missingRequirement((SkyBlockPlayer) player);
        return new DialogueSet[]{
                DialogueSet.builder().key("locked").lines(missing == null ? "Welcome back!" : missing).build(),
                DialogueSet.builder().key("intro").lines(
                        "So you have reached Heart of the Mountain Tier " + CrystalHollowsAccess.REQUIRED_HOTM_TIER + "!",
                        "The Crystal Hollows are full of treasure and secrets, but the dwarven outposts there are harder than expected.",
                        "We now use them for expeditions and for mining Gemstones.",
                        "I'll grant you access to the Crystal Hollows for 6 hours, for only 10,000 coins!"
                ).build(),
                DialogueSet.builder().key("special").lines(
                        "Someone special has been asking to meet you.",
                        "Head to the Hanging Court in the Royal Palace. Better not keep him waiting!",
                        "Help him out, and I won't charge you for the Crystal Hollows ever again."
                ).build(),
                DialogueSet.builder().key("permanent").lines(
                        "I heard you helped Dulin. The Crystal Hollows are open to you for good, free of charge!"
                ).build()
        };
    }
}
