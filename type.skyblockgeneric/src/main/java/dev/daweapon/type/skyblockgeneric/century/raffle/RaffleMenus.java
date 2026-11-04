package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Supplier;

public final class RaffleMenus {
    private RaffleMenus() {
    }

    public static void openBox(SkyBlockPlayer player) {
        open(player, GUIRaffleBox::new);
    }

    static void open(SkyBlockPlayer player, Supplier<View<RaffleSnapshot>> view) {
        if (CenturyEvent.latest() == null || !RaffleStore.available()) {
            player.sendMessage("<c>The Raffle of the Century isn't running right now!");
            return;
        }
        CenturyRaffle.async(() -> {
            RaffleSnapshot snapshot = RaffleSnapshot.load(player.getUuid());
            CenturyRaffle.sync(() -> {
                if (!player.isOnline()) return;
                if (snapshot == null) {
                    player.sendMessage("<c>The Raffle of the Century isn't running right now!");
                    return;
                }
                if (snapshot.profile().team() == null) {
                    player.sendMessage("<e>[NPC] <6>Raffle Box<f>: Hey I'm just a box, I can't explain the event!");
                    player.sendMessage("<e>[NPC] <6>Raffle Box<f>: Talk with Simon or <a>Rezzus <f>first!");
                    return;
                }
                player.openView(view.get(), snapshot);
            });
        });
    }

    static String countdown(long millis) {
        long seconds = Math.max(0, millis / 1000);
        long days = seconds / 86_400;
        long hours = seconds % 86_400 / 3_600;
        long minutes = seconds % 3_600 / 60;
        long secs = seconds % 60;
        if (days > 0) return days + "d " + hours + "h " + minutes + "m";
        return hours + "h " + minutes + "m " + secs + "s";
    }
}
