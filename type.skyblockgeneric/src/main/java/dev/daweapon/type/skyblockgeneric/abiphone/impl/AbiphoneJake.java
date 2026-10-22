package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.ItemContactAbiphone;
import dev.daweapon.type.skyblockgeneric.farming.jake.JakeShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class AbiphoneJake extends ItemContactAbiphone {
    public AbiphoneJake() {
        super("jake", "<e>Jake", "Opens his shop of animal axes and reforge stones from anywhere.", ItemType.CHOCOBERRY, 1);
    }

    @Override
    public void onCall(HypixelPlayer player) {
        sendNPCMessage(player, "Feel free to analyze any mutations you find!");
        ((SkyBlockPlayer) player).openView(new JakeShopView());
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head("d51cc7c1d11aa0a08a73cbc184547051aebd07b3bb50d86f1188fd7a7dad2ed3", Text.empty(), List.of());
    }

    @Override
    protected DialogueSet[] stages() {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "You want my contact?",
                        "I don't give it out often.",
                        "I'm still getting calls from that one guy asking if I want to \"extend my shovel warranty\".",
                        "But if you help me with my research, I can trust you with it.",
                        "Bring me a <a>Chocoberry<f>. One sample shall be enough.").build(),
                DialogueSet.builder().key("missing").lines(
                        "I cannot add you yet, I still have to finish my research.",
                        "Bring me a <a>Chocoberry <f>first, and I'll give you my contact!").build(),
                DialogueSet.builder().key("given").lines(
                        "This <a>Chocoberry <f>looks great, perfect condition even!",
                        "This gives me everything I need.",
                        "You've earned my contact. I'll add you now.",
                        "But don't call me after 8pm, please.").build()
        };
    }
}
