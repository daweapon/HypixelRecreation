package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class SpecialMessages {
    private SpecialMessages() {
    }

    static void send(SkyBlockPlayer player, CreatureSpec spec, String when) {
        send(player, spec, when, "");
    }

    static void send(SkyBlockPlayer player, CreatureSpec spec, String when, String percent) {
        for (CreatureSpec.Message message : spec.messages()) {
            if (when.equals(message.when())) player.sendMessage(message.text().replace("##", percent));
        }
    }
}
