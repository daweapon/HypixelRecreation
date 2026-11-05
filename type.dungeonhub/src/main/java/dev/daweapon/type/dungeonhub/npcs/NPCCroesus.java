package dev.daweapon.type.dungeonhub.npcs;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.GUICroesus;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestStorage;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

import java.util.concurrent.ThreadLocalRandom;

/** Croesus keeps unopened Catacombs and Kuudra reward chests for 72 hours (wiki Croesus, Dungeon Hub -27.5 119 35.5). */
public class NPCCroesus extends HypixelNPC {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FiOGU2M2FiYzBlYzJkMDI4OTAwMjg3YWM3ZTcxZGJmNDQ3ZWU3MDkxYzlmZjA3ODVmYTE5ZTg4MGIzMDgxNSJ9fX0=";
    private static final int RANDOM_LINES = 4;

    public NPCCroesus() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<e>Croesus", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<e>Croesus");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-27.5, 119, 35.5);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!DungeonChestStorage.hasMetCroesus(player)) {
            setDialogue(player, "first").thenRun(() -> {
                DungeonChestStorage.meetCroesus(player);
                player.openView(new GUICroesus());
            });
            return;
        }
        setDialogue(player, "random_" + ThreadLocalRandom.current().nextInt(RANDOM_LINES));
        player.openView(new GUICroesus());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("first").lines(
                        "After completed a dungeon run, you get a selection of <6>treasure chests<f>.",
                        "If you <c>don't open <f>any, I collect and hoard them for you!"
                ).build(),
                DialogueSet.builder().key("random_0").lines(
                        "Did you know, I used to live in the catacombs before they moved in all the zombies?").build(),
                DialogueSet.builder().key("random_1").lines(
                        "It's always best to keep a <o>handle <f>on things!").build(),
                DialogueSet.builder().key("random_2").lines(
                        "The Wither King now lives in the deepest floor of the Catacombs, but where is the Wither Prince?").build(),
                DialogueSet.builder().key("random_3").lines(
                        "I wonder if they'll ever open more doors in here.").build()
        };
    }
}
