package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.trapper.MushroomDesert;
import dev.daweapon.type.skyblockgeneric.trapper.Pelts;
import dev.daweapon.type.skyblockgeneric.trapper.TrapperService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class AbiphoneTrevor extends AbiphoneNPC {
    private static final String GIVEN = "abiphone.trevor.given";
    private static final int PELT_FEE = 24;
    private static final int COIN_FEE = 1_000_000;

    public AbiphoneTrevor() {
        super("trevor", "<e>Trevor The Trapper", "Lets you take on <5>Trapper <7>tasks from anywhere in the <e>Mushroom Desert<7>.");
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, GIVEN)) {
            super.onAdd(player, slot);
            return;
        }
        if (dialogue().isInDialogue(player)) return;
        dialogue().setDialogue(player, "abiphone").thenRun(() -> NPCOption.sendOption(player, "trevor_contact", false, List.of(
                new NPCOption.Option("pay", NamedTextColor.GREEN, true, "PAY PELTS AND COINS", _ -> pay(player, slot)))));
    }

    private void pay(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, GIVEN) || dialogue().isInDialogue(player)) return;
        if (Pelts.balance(player) < PELT_FEE || player.getCoins() < COIN_FEE) {
            dialogue().setDialogue(player, "missing");
            return;
        }
        Pelts.spend(player, PELT_FEE);
        player.removeCoins(COIN_FEE);
        ChapterTriggers.fire(player, GIVEN);
        dialogue().setDialogue(player, "given").thenRun(() -> super.onAdd(player, slot));
    }

    @Override
    public void onCall(HypixelPlayer caller) {
        SkyBlockPlayer player = (SkyBlockPlayer) caller;
        if (!MushroomDesert.contains(player)) {
            sendNPCMessage(player, "<f>I'm currently hunting! Don't call again!");
            return;
        }
        TrapperService.offer(player, message -> sendNPCMessage(player, "<f>" + message));
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head("6102f82148461ced1f7b62e326eb2db3a94a33cba81d4281452af4d8aeca4991", Text.empty(), List.of());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("abiphone").lines(
                        "<f>Eh, I still have an Abiflip.",
                        "<f>It has 3 contact slots and I already have <e>Tammy <f>and <e>Tony<f>.",
                        "<f>I heard the new models are nice, but they're too smart for me!",
                        "<f>I could add you for let's say... <5>24 Pelts <f>and <6>1,000,000 coins<f>?").build(),
                DialogueSet.builder().key("missing").lines("<f>You don't have what it takes!").build(),
                DialogueSet.builder().key("given").lines("<f>That's awesome, but now my Abiflip is full.").build()
        };
    }
}
