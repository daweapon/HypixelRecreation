package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.abiphone.ChatAnswers;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingNpcCatalogueView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class AbiphoneTony extends AbiphoneNPC {
    private static final String GIVEN = "abiphone.tony.given";

    public AbiphoneTony() {
        super("tony", "<9>Tony", "Lets you use <5>Tony's <7>shop from anywhere.");
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, GIVEN)) {
            super.onAdd(player, slot);
            return;
        }
        if (dialogue().isInDialogue(player)) return;
        dialogue().setDialogue(player, "abiphone").thenRun(() -> option(player, "tony_yes", "yes", "Yes", () -> {
            if (dialogue().isInDialogue(player)) return;
            dialogue().setDialogue(player, "game").thenRun(() -> option(player, "tony_game", "game", "What kind of game?", () -> {
                if (dialogue().isInDialogue(player)) return;
                dialogue().setDialogue(player, "guess");
                ChatAnswers.expect(player, answer -> answer.equalsIgnoreCase("horse"), () -> won(player, slot));
            }));
        }));
    }

    private void won(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, GIVEN)) return;
        ChapterTriggers.fire(player, GIVEN);
        dialogue().setDialogue(player, "won").thenRun(() -> super.onAdd(player, slot));
    }

    private void option(SkyBlockPlayer player, String id, String key, String label, Runnable action) {
        NPCOption.sendOption(player, id, true, List.of(new NPCOption.Option(key, NamedTextColor.GREEN, false, label, _ -> action.run())));
    }

    @Override
    public void onCall(HypixelPlayer player) {
        player.openView(new HuntingNpcCatalogueView("tony", null));
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head("65f31c1c215a57e937fd75ab35782f85ec242a1b1f950a26a42bb5e0aa5cbeda", Text.empty(), List.of());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("abiphone").lines(
                        "<f>Yo bro, how's it going?",
                        "<f>Such lovely weather in this little ol' cave today, isn't it?",
                        "<f>I guess you want my <9>Abiphone Contact<f>, huh?").build(),
                DialogueSet.builder().key("game").lines(
                        "<f>Well, I'll give you it if you play a game with me.",
                        "<f>Not much else to do around here, really.").build(),
                DialogueSet.builder().key("guess").lines(
                        "<f>How about you guess my favorite animal?",
                        "<f>I'll let you know if you get it right.").build(),
                DialogueSet.builder().key("won").lines(
                        "<f>Haha yeah, my favorite animal is a <a>horse<f>.",
                        "<f>Nice one bro.",
                        "<f>Well, I'm a man of my word. Here's my contact.").build()
        };
    }
}
