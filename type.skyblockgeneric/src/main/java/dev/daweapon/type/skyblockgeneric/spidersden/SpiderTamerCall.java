package dev.daweapon.type.skyblockgeneric.spidersden;

import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.GUIEssenceShop;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** The Spider Tamer's Abiphone call (wiki: Spider Tamer): a spider fun fact, then the Spider Essence Shop once Arachne has fallen. */
public final class SpiderTamerCall {
    static final List<String> FACTS = List.of(
            "Did you know? Tarantulas can get so big that they can prey on some birds, and even lizards!",
            "Did you know? Spiders are arachnids, not insects!",
            "Did you know? Most spiders have blue blood.",
            "Did you know? Spiders don't have an internal skeleton.",
            "Did you know? Spiders sense their surroundings through vibrations and their sensitive leg hairs.",
            "Did you know? Most spiders liquify their food! That's how they can eat stuff that's bigger than them!");

    private SpiderTamerCall() {
    }

    public static void call(HypixelPlayer caller) {
        SkyBlockPlayer player = (SkyBlockPlayer) caller;
        player.sendMessage("<e>[NPC] <c>Spider Tamer<f>: <b>✆ <f>" + FACTS.get(ThreadLocalRandom.current().nextInt(FACTS.size())));
        if (ArachneFight.hasDefeatedEither(player)) player.openView(new GUIEssenceShop(EssenceType.SPIDER));
    }
}
