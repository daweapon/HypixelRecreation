package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.faction.OdgerQuestTopics;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.crimson.questline.MageStep;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Odexar, Scarleton's self-proclaimed best lava fisher. His Volcanic Stonefish errand is worth +100 Mage Reputation and 80 coins.
public class NPCOdexar extends CrimsonNPC {
    public static final int COINS = 80;

    public NPCOdexar() {
        super("<5>Odexar", CrimsonSkins.ODEXAR, new Pos(-112.5, 99, -844.5));
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (CrimsonQuestlines.mage(player) != MageStep.HELP_ODEXAR) {
            if (CrimsonSideQuests.firstMeeting(player, "odexar")) {
                say(player, "Lava fishing is the best, it is my favorite pastime.", "Do you like my collection of Trophies?",
                        "Some fishes can only be caught under specific conditions, and it requires great focus.");
                return;
            }
            sayRandom(player, "My fishing rod is quite amazing but I'm still saving up for a better one, I can't wait!",
                    "The largest fish in my collection is almost a meter long, and weighs close to 100 lb!");
            return;
        }
        long stage = CrimsonQuestlines.counter(player, OdgerQuestTopics.ODEXAR);
        if (stage == 0) {
            say(player, "My name is Odexar! I have quite the reputation around here.",
                    "You see, I'm the best lava fisher in the world.",
                    "Recently I fished a magnificent Volcanic Stonefish from a secret spot I won't reveal.",
                    "I would like to show it to Odger but I hurt my feet badly.",
                    "Would you mind taking my Volcanic Stonefish to Odger's house on top of the Volcano?")
                    .thenRun(() -> {
                        CrimsonQuestlines.setCounter(player, OdgerQuestTopics.ODEXAR, OdgerQuestTopics.ODEXAR_CARRYING);
                        player.sendMessage("<a>You are carrying Odexar's Volcanic Stonefish to <e>Odger<a>.");
                        say(player, "Thanks, I'm sure Odger will be proud, that is the best Trophy Fish I got in a long time!");
                    });
            return;
        }
        if (stage < OdgerQuestTopics.ODEXAR_FRAMED || player.getAmountInInventory(ItemType.FRAMED_VOLCANIC_STONEFISH) < 1) {
            sayRandom(player, "Odger lives in a house inside the Volcano at the very top, you can't miss it.",
                    "Trophy Fishing is my passion, and Odger is the local specialist.");
            return;
        }
        if (!CrimsonQuestlines.advance(player, MageStep.HELP_ODEXAR, MageStep.HALLOWED_SKULL, 100)) return;
        player.removeItemFromPlayer(ItemType.FRAMED_VOLCANIC_STONEFISH, 1);
        say(player, "Marvelous! The Fish looks even better than I remembered!",
                "Odger has delicate hands, this is really top quality work!",
                "What do you say? Odger said I was a bad fisherman?",
                "Hahaha!! What a jokester this Odger, love that goblin.",
                "Thanks a lot friend, here take those 80 coins. Don't spend them all in one go!")
                .thenRun(() -> {
                    player.addCoins(COINS);
                    player.sendMessage("<6>+80 Coins!");
                });
    }
}
