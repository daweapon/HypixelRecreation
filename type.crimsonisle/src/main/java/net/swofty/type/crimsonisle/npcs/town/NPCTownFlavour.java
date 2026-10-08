package net.swofty.type.crimsonisle.npcs.town;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.type.crimsonisle.faction.CrimsonChoices;
import net.swofty.type.crimsonisle.town.DayNightNPC;
import net.swofty.type.crimsonisle.town.RoamingNPC;
import net.swofty.type.crimsonisle.town.TalkNPC;
import net.swofty.type.crimsonisle.town.TownSkins;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.List;

/**
 * The talk-only town NPCs that are not "talk to 4 villagers" villagers: Corm, Eludore, Maggie, Aura,
 * the walkers (Pam, Forast, Uvia, Azax, Deng) and the day/night NPCs (Edelis, Keran, Prie). Walking
 * routes and stop timings are estimates; the wiki only names the stops.
 */
public final class NPCTownFlavour {
    private static final String[][] PROTEST_HOME_KERAN = {{"Make yourself at home!"}, {"Sharing is caring!"},
            {"Protesting sure takes a lot out of you. I need to rest!"}, {"Help spread our message! Sulphur for all!"}};
    private static final String[][] PROTEST_HOME_PRIE = {{"It takes all of us to enact change!"}, {"Feel free to look around!"},
            {"It's not much, but Keran and I love it here!"}, {"Back to the old grind tomorrow! For now...time to rest!"}};
    private static final Pos PROTEST = new Pos(-561.5, 98, -687.5);
    private static final Pos PROTESTERS_HOME = new Pos(-612.5, 108, -669.5);

    private NPCTownFlavour() {
    }

    private static String[] lines(String... lines) {
        return lines;
    }

    public static class Corm extends TalkNPC {
        public Corm() {
            super("<d>Corm", TownSkins.CORM, new Pos(-668.5, 126, -924.5), lines(), lines(
                    "Are you a tourist?",
                    "This is my favorite site, I know everything about the great <5>Dragons<f>.",
                    "This is of course <5>Jythar<f>, the Eternal One.",
                    "The dragon army attacked our dimension many centuries ago and sent the world into pieces.",
                    "The skeleton of <5>Chozon<f> can be seen on the island south of here. He was one of the generals of Jythar.",
                    "Dogen, Kirva, and Daydro perished and fell in the void, to my knowledge.",
                    "The 4 generals were so huge that their reach could cover the sun and bring shadows upon entire cities.",
                    "Thousands of Dragons attacked that day and all were defeated, but did they fail?",
                    "What even was their purpose?",
                    "I think they decided to break the world to weaken us, and by us I mean everyone in this dimension.",
                    "Despite the wars raging at the time, we were still able to unite and beat this common enemy.",
                    "Jythar knew there would be no path to victory against us for as long as we could unite.",
                    "But why not retreat?",
                    "Why did he keep attacking us until his death?",
                    "I doubt it was madness. Dragons live for millennia, their Emperor surely wasn't mad.",
                    "So why would he give up his life to guarantee a potential victory in the future?",
                    "It's impossible for us to know, but we can theorize.",
                    "It is known from the Dragons summoned nowadays that they worship their <5>Queen<f>.",
                    "Queen, Empress - the exact word might have been lost in translation.",
                    "I think Jythar did it for her.",
                    "I think the reason is <5>love<f>.",
                    "Maybe I am wrong, but I choose to believe it.",
                    "This titan next to us is known by many names, some call them the <5>Calamity<f>.",
                    "They are the enemies, but if they are motivated by love, maybe we can communicate with them.",
                    "What do you think? Am I too romantic, or did my explanation make sense?"));
        }
    }

    public static class Eludore extends TalkNPC {
        public Eludore() {
            super("<5>Eludore", TownSkins.ELUDORE, new Pos(-101.5, 91, -872.5),
                    lines("The Academy was great today, learned a lot of important things.",
                            "I'm just waiting for my friend to arrive so we can walk back home together."),
                    lines("My friend should be here any minute now.", "Annyyyyy minute now..."));
        }
    }

    public static class Maggie extends TalkNPC {
        public Maggie() {
            super("<c>Maggie", EntityType.MAGMA_CUBE, new Pos(-596.5, 123, -847.5), lines(), lines("Maggie."));
        }
    }

    public static class Aura extends RoamingNPC {
        public Aura() {
            super("<5>Aura", TownSkins.AURA, List.of(new Pos(-394.5, 69, -849.5), new Pos(-398.5, 72, -849.5)),
                    Duration.ofSeconds(30), lines(),
                    lines("I learned more than I could have from success alone."), lines("And I've been taking notes."),
                    lines("Next time, things will be different, you will see."), lines("I'm going back to the drawing boards."),
                    lines("I've noticed how many people have been stopping by."), lines("My lab doesn't feel very secret anymore."));
        }
    }

    public static class Pam extends RoamingNPC {
        public Pam() {
            super("<d>Pam", TownSkins.PAM, List.of(new Pos(-662.5, 102, -829.5), new Pos(-631.5, 100, -805.5),
                            new Pos(-575.5, 111, -754.5), new Pos(-598.5, 109, -665.5), new Pos(-673.5, 102, -767.5),
                            new Pos(-592.5, 107, -723.5)), Duration.ofSeconds(60),
                    lines("The layout of our village is so complex...",
                            "I just got a new job, but I don't know how to find the address.",
                            "Navigating around here is really tough..."),
                    lines("I haven't managed to find the place yet, but soon!"), lines("Hopefully I'll find the way tomorrow."),
                    lines("I got the wrong address again..."), lines("Looks like I'm lost."),
                    lines("Oh boy...this isn't it."), lines("I don't think this is it..."));
        }
    }

    public static class Forast extends RoamingNPC {
        public Forast() {
            super("<d>Forast", TownSkins.FORAST, List.of(new Pos(-625.5, 99, -805.5), new Pos(-631.5, 100, -799.5),
                            new Pos(-637.5, 101, -805.5), new Pos(-631.5, 100, -811.5)), Duration.ofSeconds(4), lines(),
                    lines("Can't stop, won't stop!"), lines("I was probably a rabbit in another life!"),
                    lines("I love running and jumping."));
        }
    }

    public static class Uvia extends RoamingNPC {
        public Uvia() {
            super("<5>Uvia", TownSkins.UVIA, List.of(new Pos(-128.5, 99, -789.5), new Pos(-120.5, 99, -815.5),
                            new Pos(-112.5, 99, -838.5), new Pos(-106.5, 100, -801.5)), Duration.ofSeconds(20), lines(),
                    lines("I enjoy long strolls in <5>Scarleton<f>."),
                    lines("It's so refreshing to see some new faces in the town!"));
        }
    }

    public static class Azax extends RoamingNPC {
        public Azax() {
            super("<5>Azax", TownSkins.AZAX, List.of(new Pos(-128.5, 89, -725.5), new Pos(-128.5, 99, -789.5)),
                    Duration.ofSeconds(45), lines(),
                    lines("I like to drag my feet because it's more time out of the house."),
                    lines("Fetch this, fetch that...", "Is that really all I'm good for?"),
                    lines("How many times do I have to go to the shop every day..."),
                    lines("She should really just wholesale at this rate. Cut out the middle mage."));
        }
    }

    public static class Deng extends RoamingNPC {
        public Deng() {
            super("<d>Deng", TownSkins.DENG, List.of(new Pos(-685.5, 102, -764.5), new Pos(-683.5, 102, -764.5)),
                    Duration.ofSeconds(4), lines(), lines("I need to think. I need to think."));
        }
    }

    public static class Edelis extends DayNightNPC {
        public Edelis() {
            super("<d>Edelis", TownSkins.EDELIS, new Pos(-61.5, 107, -812.5), new Pos(-92.5, 104, -821.5),
                    new Place(lines("This is the <9>Statue of the Hero<f>. I come here every day to pray.",
                            "The legend says that a Hero arrived centuries ago, and helped us in times of need.",
                            "Some of us still believe that the Hero will come back one day.",
                            "Do you want me to tell the story?"),
                            new String[][]{{"What do you think? Do you believe in the Hero too?"},
                                    {"Do you think the Hero was a man or a woman? There is so little information that remains..."}}),
                    new Place(lines("There are three different beliefs among the <a>Taika<f>.",
                            "After we started learning magic centuries ago, most people stopped praying in the <5>Cathedral<f>.",
                            "They were too busy to learn and create new spells, and even turned the <5>Cathedral <finto an Academy.",
                            "Those people will say that they believe in knowledge and magic.",
                            "Some others still believe that it was our prayers that gave us our powers.",
                            "They believe it was the Sky's plan to make us independent through <a>Sulphur<f>, and that the Sky is good and fair.",
                            "And finally there are still very few people like me, who hold their faith in the <9>Hero<f>.",
                            "The Hero was humble, yet they saved us all from doom."),
                            new String[][]{{"I'm trying to sleep."}, {"It is very late."}}));
        }

        @Override
        protected void introduce(SkyBlockPlayer player, boolean day, String[] lines) {
            if (!day) {
                super.introduce(player, false, lines);
                return;
            }
            say(player, lines).thenRun(() -> CrimsonChoices.noYes(player, "edelis_story",
                    () -> say(player, "I could tell you were not very interested.", "I will go back to my prayers."),
                    () -> say(player, "The Hero was humble, yet they saved us all from doom.",
                            "Some of us still believe that the Hero will come back one day.")));
        }
    }

    public static class Keran extends DayNightNPC {
        public Keran() {
            super("<d>Keran", TownSkins.KERAN, PROTEST, PROTESTERS_HOME,
                    new Place(lines(), new String[][]{{"Sulphurcare is our proposal - no taxes on Sulphur!",
                            "Any Piglin should have access to their share of the stash."}}),
                    new Place(lines(), PROTEST_HOME_KERAN));
        }
    }

    public static class Prie extends DayNightNPC {
        public Prie() {
            super("<d>Prie", TownSkins.PRIE, PROTEST.add(0, 0, -2), PROTESTERS_HOME.add(1, 0, 0),
                    new Place(lines(), new String[][]{{"There is plenty Sulphur for everyone."}}),
                    new Place(lines(), PROTEST_HOME_PRIE));
        }

        @Override
        protected void interact(SkyBlockPlayer player) {
            if (isDay()) {
                scene().say("<d>Prie", "There is plenty Sulphur for everyone.")
                        .say("<d>Keran", "I can barely buy enough for my kids.").play(player);
                return;
            }
            super.interact(player);
        }
    }
}
