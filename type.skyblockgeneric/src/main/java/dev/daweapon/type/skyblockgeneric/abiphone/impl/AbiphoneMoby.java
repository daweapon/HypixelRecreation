package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.ItemContactAbiphone;
import dev.daweapon.type.skyblockgeneric.farming.chum.MobyShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class AbiphoneMoby extends ItemContactAbiphone {
    public AbiphoneMoby() {
        super("moby", "<2>Moby", "Opens his chumming supplies shop from anywhere.", ItemType.FULL_CHUM_BUCKET, 64);
    }

    @Override
    protected List<Step> steps() {
        return List.of(new Step("What do you need?", "ask"), new Step("Is it chum?", "task"));
    }

    @Override
    public void onCall(HypixelPlayer player) {
        sendNPCMessage(player, "Hey chum!");
        ((SkyBlockPlayer) player).openView(new MobyShopView());
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head("9b688545e326e4f1fd1c405873d9c5b8bbe69dd26889ce3dea10b4d9e3d7b4db", Text.empty(), List.of());
    }

    @Override
    protected DialogueSet[] stages() {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "Hey chum. Long time no see.",
                        "You want my <3>Abiphone Contact<f>?",
                        "Ok, listen.",
                        "I got all my chums in my phone, ok?",
                        "And we're chums too, you and I.",
                        "But I only got so many contact slots, you know?",
                        "And who am I to say which of my chums are the most important, you know?",
                        "What kind of chum would that make me?",
                        "So, if you want your digits in my phone, imma need you to get me something...").build(),
                DialogueSet.builder().key("ask").lines("What do you think it is?").build(),
                DialogueSet.builder().key("task").lines(
                        "Of course it's chum, chum!",
                        "Bring me <a>Full Chum Bucket <8>x64 <f>and I'll add you to my <3>Abiphone<f>!",
                        "Mates rates!").build(),
                DialogueSet.builder().key("missing").lines(
                        "Hey chum, that's not it.",
                        "Ok, listen.",
                        "I asked for <a>Full Chum Bucket <8>x64<f>.",
                        "Not whatever this is.").build(),
                DialogueSet.builder().key("given").lines(
                        "Wow ok, that's good.",
                        "This is the stuff. The good stuff.",
                        "Here's my contact!",
                        "You ever need chumming supplies just give your chum Moby a call!").build()
        };
    }
}
