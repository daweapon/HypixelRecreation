package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.skyblockgeneric.crimson.questline.BarbarianStep;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Gris wants a Heavy Pearl from the Matriarch, then hides in his basement (+150 Barbarian Reputation, 200,000 coins).
public class NPCGris extends CrimsonNPC {
    public static final int COINS = 200_000;
    private static final Pos UPSTAIRS = new Pos(-684.5, 124, -828.5);
    private static final Pos BASEMENT = new Pos(-686, 116, -832);
    private static final String ASKED = "gris_asked";

    public NPCGris() {
        super("<d>Gris", CrimsonSkins.GRIS, UPSTAIRS);
    }

    @Override
    protected boolean moves() {
        return true;
    }

    @Override
    protected Pos position(SkyBlockPlayer player) {
        boolean barricaded = CrimsonQuestlines.counter(player, ASKED) > 0
                || CrimsonQuestlines.barbarian(player).ordinal() > BarbarianStep.HELP_GRIS.ordinal();
        return barricaded ? BASEMENT : UPSTAIRS;
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        BarbarianStep step = CrimsonQuestlines.barbarian(player);
        if (step.ordinal() > BarbarianStep.HELP_GRIS.ordinal()) {
            sayRandom(player, "I'd rather you leave now.", "I don't really trust anyone.");
            return;
        }
        if (step != BarbarianStep.HELP_GRIS) {
            say(player, "There is no time...", "I need to prepare.");
            return;
        }
        if (CrimsonQuestlines.counter(player, ASKED) == 0) {
            CrimsonQuestlines.setCounter(player, ASKED, 1);
            say(player, "Ahhhh!", "Don't kill me!!!!", "...", "Excuse me. You really scared me!",
                    "I'm not a gambler, but if I had to bet on a faction to win this war, I wouldn't bet on us.",
                    "Soon enough I'll <2>barricade myself in my basement<f>.",
                    "I have enough provisions to last at least 7 months.",
                    "That's not enough...no that's far from enough.",
                    "What I need the most right now is a <2>Heavy Pearl<f>.",
                    "If you bring one to me I'll reward you handsomely.",
                    "There is only one way to obtain <2>Heavy Pearls<f>.",
                    "First you have to go down in that <2>giant cave below the Volcano<f>.",
                    "From there you need to enter the cave that goes <c>North-West<f>.",
                    "Inside that cave lives very rare creatures called <c>Hellwisps<f>.",
                    "If you keep going down and down, you'll find another cave, which leads to <c>The Matriarch<f>.",
                    "That creature is much larger than the other ones and has been there for decades, if not centuries.",
                    "To get a <2>Heavy Pearl<f>, you need to let the <c>Matriarch <f>eat you alive.",
                    "Once you are inside its stomach, you can <2>steal a few pearls<f>.",
                    "Then all you have to do is <2>escape before the acid kills you<f>, I think there is a way but I'm not so sure how.",
                    "You have to get the pearls on your first try because the <c>Matriarch <f>won't eat you again.",
                    "Usually it takes at least a day before she forgets your scent and eats you again.",
                    "Please bring me back one of those <2>Heavy Pearls<f>.");
            return;
        }
        if (player.getAmountInInventory(ItemType.HEAVY_PEARL) < 1) {
            sayRandom(player, "I think the best way to get out is for <c>The Matriarch <f>to spit you out, but I'll admit I don't know how.",
                    "<2>Heavy Pearls <f>have many, many properties.");
            return;
        }
        if (!CrimsonQuestlines.advance(player, BarbarianStep.HELP_GRIS, BarbarianStep.TALK_TO_CHIEF, 150)) return;
        player.removeItemFromPlayer(ItemType.HEAVY_PEARL, 1);
        say(player, "Eeeeeeeeeeeeeeeeeek!", "You almost gave me a heart attack!!!",
                "Oh, you managed to get a <2>Heavy Pearl<f>!",
                "Now that you mention it, I remember how to escape the belly of the <c>Matriarch<f>!",
                "All you have to do it punch its uvula!", "In a way they are not so different than us.",
                "Thank you very much, here take these <6>Coins<f>.")
                .thenRun(() -> {
                    player.addCoins(COINS);
                    player.sendMessage("<6>+200,000 Coins!");
                });
    }
}
