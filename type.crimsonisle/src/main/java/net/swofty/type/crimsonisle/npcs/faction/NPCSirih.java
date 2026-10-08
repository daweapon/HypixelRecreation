package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

/**
 * Sirih, the writer who keeps forgetting Sulphur (wiki: Sirih). As a pig, 1 Sulphur turns them back
 * (+10 Barbarian Reputation at most every 2 days); they become a pig again a day later.
 */
public final class NPCSirih {
    private static final Pos HOME = new Pos(-699.5, 131, -887.5);
    private static final Duration REWARD_COOLDOWN = Duration.ofDays(2);
    private static final int REPUTATION = 10;

    private NPCSirih() {
    }

    public static class Piglin extends CrimsonNPC implements NPCAbiphoneTrait {
        private static final String[][] NOVELS = {
                {"My new novel?", "It's about an old hero that everyone still considers as the strongest.",
                        "But he himself knows very well that his prime is over and that his next fight will be his last.",
                        "It's be a sad story about how the expectations of others can force you to do things that hurt you."},
                {"My new novel is about someone who has a strange power that allows him to inspect the memories of others.",
                        "The first part shows him having fun with his powers and exploring ways to use memories to his advantage.",
                        "But one day he finds a strange character, and in their memories he sees things he should have never seen...",
                        "That's it! I won't tell you more than that!"},
                {"I'm currently working on a complex love story.",
                        "A smart and beautiful girl is forced to marry someone they have never met before.",
                        "She's apprehensive at first but when she meets him they end up surpassing all expectations.",
                        "She falls in love quickly but he doesn't and for the first time in her life she has to deal with that emotional pain.",
                        "It's a simple story but it touches a lot of interesting subjects."},
                {"I'm writing a fictional story about Kaeman and his wife Sarah, before the Calamity.",
                        "Most people know Kaeman as the Wither King, but I think it's important to remind people that he once was a human.",
                        "The tragedy of his life isn't very known. He had an idyllic relationship with his wife, up until their daughter was born.",
                        "I'm trying to show how pain and sorrow turned this well respected man into an evil doer."},
                {"I think this new story will be my masterpiece.",
                        "It's a thriller about a genius serial killer who's obsessed with framing police inspectors with his murders.",
                        "But this time the inspector he framed escaped the justice system and hunts him down.",
                        "It's an incredible battle of wits between two brilliant minds."}};

        public Piglin() {
            super("<d>Sirih", CrimsonSkins.SIRIH, HOME);
        }

        @Override
        public String getAbiphoneKey() {
            return "sirih";
        }

        @Override
        protected boolean visible(SkyBlockPlayer player) {
            return !CrimsonSideQuests.isSirihPig(player);
        }

        @Override
        protected void interact(SkyBlockPlayer player) {
            if (CrimsonSideQuests.firstMeeting(player, "sirih_novel")) {
                say(player, "The name of my new novel? Hmm...", "I haven't decided yet.",
                        "I mostly write fictions. Because you helped me, I'll tell you some of the plot.",
                        "The story takes place in a dystopian Crimson Isle where the lava of the Volcano is turning into water.",
                        "We Piglins always dream of water. This book will be about showing that water also brings pain and struggle.");
                return;
            }
            say(player, NOVELS[java.util.concurrent.ThreadLocalRandom.current().nextInt(NOVELS.length)]);
        }
    }

    public static class Pig extends CrimsonNPC implements NPCAbiphoneTrait {
        public Pig() {
            super("<d>Sirih", EntityType.PIG, HOME);
        }

        @Override
        public String getAbiphoneKey() {
            return "sirih";
        }

        @Override
        protected boolean visible(SkyBlockPlayer player) {
            return CrimsonSideQuests.isSirihPig(player);
        }

        @Override
        protected void interact(SkyBlockPlayer player) {
            if (!CrimsonReputationService.isMember(player, CrimsonFaction.BARBARIAN) || player.getAmountInInventory(ItemType.SULPHUR) < 1) {
                say(player, "Oink.");
                return;
            }
            player.removeItemFromPlayer(ItemType.SULPHUR, 1);
            boolean first = CrimsonSideQuests.get(player, CrimsonSideQuests.SIRIH_CURED) == 0;
            CrimsonSideQuests.stamp(player, CrimsonSideQuests.SIRIH_CURED);
            if (CrimsonSideQuests.isReady(player, CrimsonSideQuests.SIRIH_REWARDED, REWARD_COOLDOWN)) {
                CrimsonSideQuests.stamp(player, CrimsonSideQuests.SIRIH_REWARDED);
                CrimsonReputationService.add(player, CrimsonFaction.BARBARIAN, REPUTATION, "Sirih");
            }
            if (first) {
                say(player, "What, when? Oh.", "How long have I been gone?", "I keep on forgetting about Sulphur...",
                        "Nowadays I live as a recluse, focusing on my work.",
                        "I'm a writer, so I need calm to concentrate on my new novel.", "I really need to get back to work.");
            } else {
                say(player, "Again...", "I get so absorbed by my work...", "Time really flies when you work on something you love.",
                        "Thanks for the Sulphur! I'll finish my new novel now.");
            }
        }
    }
}
