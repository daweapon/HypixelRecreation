package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.hunting.GUIAttributeFusion;
import dev.daweapon.type.skyblockgeneric.hunting.npc.ShardContactAbiphone;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class AbiphoneKysha extends ShardContactAbiphone {
    public AbiphoneKysha() {
        super("kysha", "<b>Kysha", "Lets you use the <5>Fusion Machine <7>from anywhere.", "L5");
    }

    @Override
    protected String reaction() {
        return "It was an amazing experience.";
    }

    @Override
    public void onCall(HypixelPlayer player) {
        player.openView(new GUIAttributeFusion());
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head("613328c0a46f97f1e7af86c45934aca87008612721b51b3efeae75a5f40c80fd", Text.empty(), List.of());
    }

    @Override
    protected DialogueSet[] stages() {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "In my spare time I like to go swimming down in the <2>Murkwater Loch<f>.",
                        "There are so many beautiful creatures to see down there.",
                        "<a>Verdants<f>, <9>Azures<f>, <a>Shellwise<f>, <9>Joydives<f>, I've seen them all.",
                        "However, there's one that I've never quite been able to find.",
                        "The folk around here call her <d>Nessie<f>.",
                        "She's so seldom seen that many don't even believe she truly exists.",
                        "But I do. I can sense her down there sometimes, while I'm swimming.",
                        "So, how about it?",
                        "Bring me a <6>Nessie Shard <f>and I'll give you my <3>Abiphone Contact<f>.").build(),
                DialogueSet.builder().key("missing").lines(
                        "Looks like you don't have the <6>Nessie Shard <f>I asked for.",
                        "I've heard that she's quite a rare catch, so I guess you'll just have to keep trying.").build(),
                DialogueSet.builder().key("given").lines(
                        "Wow! You really found her! What was she like?").build(),
                DialogueSet.builder().key("given-after").lines(
                        "I bet!",
                        "I can't believe she actually exists... I was beginning to lose hope.",
                        "Looks like I need to get down to the <2>Loch <f>as soon as possible.",
                        "Anyways, as promised, here's my contact.",
                        "Give me a call if you ever need to use the <5>Fusion Machine<f>!").build()
        };
    }
}
