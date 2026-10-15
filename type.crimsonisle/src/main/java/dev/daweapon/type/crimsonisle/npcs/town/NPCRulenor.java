package dev.daweapon.type.crimsonisle.npcs.town;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.town.TownSkins;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.crimson.town.CrimsonTownProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Rulenor, the "very famous" Scarleton recluse who charges 10,000 coins for a picture (wiki Rulenor). */
public class NPCRulenor extends CrimsonNPC {
    private static final long FEE = 10_000;
    private static final String[] ASKS = {"So you gonna pay me, or?", "Pay me, please. I need this.", "How 'bout that payment?"};

    public NPCRulenor() {
        super("<5>Rulenor", TownSkins.RULENOR, new Pos(-26.5, 122, -769.5));
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (CrimsonSideQuests.is(player, CrimsonTownProgress.RULENOR_PAID)) {
            sayRandom(player, "People have told me I'm very photogenic.", "Take all of the pictures you want of me!",
                    "Get your photos in while you can!");
            return;
        }
        if (CrimsonSideQuests.firstMeeting(player, "rulenor")) {
            say(player, "How did you find me?", "I don't even have an entrance to my house.", "You are paparazzi, right?",
                    "I am very, very famous. I can't blame you.",
                    "If you want to take a picture of me, you'll have to pay <610,000 Coins<f>.").thenRun(() -> offer(player));
            return;
        }
        say(player, ASKS[ThreadLocalRandom.current().nextInt(ASKS.length)]).thenRun(() -> offer(player));
    }

    private void offer(SkyBlockPlayer player) {
        NPCOption.sendOption(player, "rulenor", false, List.of(new NPCOption.Option("pay", NamedTextColor.GOLD, false,
                "PAY 10,000 Coins", _ -> pay(player))));
    }

    private void pay(SkyBlockPlayer player) {
        if (CrimsonSideQuests.is(player, CrimsonTownProgress.RULENOR_PAID)) return;
        if (player.getCoins() < FEE) {
            say(player, "You don't have enough scratch, pal. Get outta here!");
            return;
        }
        player.removeCoins(FEE);
        CrimsonSideQuests.flag(player, CrimsonTownProgress.RULENOR_PAID);
        say(player, "Fine, you can take a picture of me now.");
    }
}
