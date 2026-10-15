package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.dwarvenmines.gui.GUIGwendolyn;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrystalHollows;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

public class NPCGwendolyn extends AbstractTalkingNPC {
    private static final String INTRO_FLAG = "met_gwendolyn";
    private static final String SPECIAL_FLAG = "gwendolyn_special_hint";
    private static final String[] WAITING_PROMPTS = {
            "Many treasures await you blah, blah, blah... Get in the Minecart!",
            "What are you waiting for? Christmas? Get in!",
            "Just get in the heckin Minecart already.",
            "Go on, in you get.",
            "The Minecart will leave without you...",
            "That Minecart won't wait forever you know."
    };

    public NPCGwendolyn() {
        super("Gwendolyn", "<5>",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzVjMzQzNTE2MDZhOTlmNjJjYWI5ZTdjNTEyODM4NjQ3NTNkMmU2Nzk0OGIxZjEyNmJhZjcwYzEyOTE3MjMzZjAifX19",
                new Pos(88.5, 198, -98.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        String missing = CrystalHollowsAccess.missingRequirement(player);
        if (missing != null) {
            setDialogue(player, HotmService.data(player).getTier() < CrystalHollowsAccess.REQUIRED_HOTM_TIER ? "locked" : "mining");
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
        if (CrystalHollowsAccess.hasValidPass(player)) {
            sendNPCMessage(player, WAITING_PROMPTS[ThreadLocalRandom.current().nextInt(WAITING_PROMPTS.length)]);
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
                DialogueSet.builder().key("locked").lines(
                        "Come back to me when you reach <d>Heart of the Mountain<f> Tier " + CrystalHollowsAccess.REQUIRED_HOTM_TIER + "."
                ).build(),
                DialogueSet.builder().key("mining").lines(missing == null ? "Welcome back!" : missing).build(),
                DialogueSet.builder().key("intro").lines(
                        "Hello! Behind me lies the entrance to the <5>Crystal Hollows<f>.",
                        "This place is full of treasures and secrets!",
                        "We dwarves have tried to setup outposts there but it is harder than it looks.",
                        "Nowadays this place is only used for expeditions and adventures!",
                        "A fantastic place to explore and mine <d>Gemstones<f>.",
                        "I'll grant you access to the <5>Crystal Hollows<f> for 6 hours, for only <6>10,000 coins<f>!",
                        "Once access is granted, you can always come here by using the <b>Fast Travel <f>menu in your <a>SkyBlock Menu<f>!",
                        "You should also talk to <6>Emissary Braum<f>, they have more information about <e>commissions<f>."
                ).build(),
                DialogueSet.builder().key("special").lines(
                        "Hey! Someone <d>special <f>has been asking to meet you!",
                        "I'm not quite sure what he's doing back in the <2>Dwarven Mines<f>- it's been <o>years <r><f>since I last saw him!",
                        "Anyways, he's expecting you over in the <b>Hanging Court <f>by the Royal Palace. Better not keep him waiting!"
                ).build(),
                DialogueSet.builder().key("permanent").lines(
                        "I heard you helped Dulin. The Crystal Hollows are open to you for good, free of charge!"
                ).build()
        };
    }
}
