package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingNpcCatalogueView;
import dev.daweapon.type.skyblockgeneric.trapper.Pelts;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class AbiphoneTalbot extends AbiphoneNPC {
    private static final String GIVEN = "abiphone.talbot.given";
    private static final int PELT_FEE = 100;

    public AbiphoneTalbot() {
        super("talbot", "<e>Talbot", "Lets you use <5>Talbot's <7>shop from anywhere.");
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, GIVEN)) {
            super.onAdd(player, slot);
            return;
        }
        if (dialogue().isInDialogue(player)) return;
        dialogue().setDialogue(player, "abiphone").thenRun(() -> NPCOption.sendOption(player, "talbot_contact", false, List.of(
                new NPCOption.Option("give", NamedTextColor.GREEN, true, "GIVE ITEM", _ -> give(player, slot)))));
    }

    private void give(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, GIVEN) || dialogue().isInDialogue(player)) return;
        if (Pelts.balance(player) < PELT_FEE) {
            dialogue().setDialogue(player, "missing");
            return;
        }
        Pelts.spend(player, PELT_FEE);
        ChapterTriggers.fire(player, GIVEN);
        dialogue().setDialogue(player, "given").thenRun(() -> super.onAdd(player, slot));
    }

    @Override
    public void onCall(HypixelPlayer player) {
        player.openView(new HuntingNpcCatalogueView("talbot", null));
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head("5e78e4b8e980fb868c7d8bc5b8a9afd522e65afde5d45de0dd75508ea5d9178d", Text.empty(), List.of());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("abiphone").lines(
                        "<f>Hey!",
                        "<f>You want my <3>Abiphone Contact<f>, don't you?",
                        "<f>I should warn you, though, the connection can be a bit spotty around these parts!",
                        "<f>How about you bring me <5>100 Pelts<f>? Do that and I'll give you my contact.").build(),
                DialogueSet.builder().key("missing").lines(
                        "<f>You don't have enough <5>Pelts<f>!",
                        "<f>I asked for <a>100 <f>of them...").build(),
                DialogueSet.builder().key("given").lines(
                        "<f>Thanks for the pelts! A man can never have enough of them.",
                        "<f>Give me a call whenever you need to use my shop!").build()
        };
    }
}
