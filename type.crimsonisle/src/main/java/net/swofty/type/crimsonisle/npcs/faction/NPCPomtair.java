package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.crimsonisle.faction.FactionVillagerNPC;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

// Pomtair dreams of potatoes: 1 Enchanted Bone Meal a day for +50 Barbarian Reputation (wiki: Pomtair).
public class NPCPomtair extends FactionVillagerNPC {
    public static final int REPUTATION = 50;
    public static final Duration COOLDOWN = Duration.ofDays(1);

    public NPCPomtair() {
        super("<d>Pomtair", CrimsonSkins.POMTAIR, new Pos(-569.5, 120, -775.5), CrimsonFaction.BARBARIAN);
    }

    @Override
    protected void talk(SkyBlockPlayer player) {
        if (CrimsonSideQuests.firstMeeting(player, "pomtair")) {
            say(player, "Ahh...I don't know what I'm going to do...", "This is rough to be honest, my potatoes...",
                    "I just want to eat something other than nether wart for once in my life...",
                    "And potatoes look so yummy and juicy...but water...instantly evaporates around here...",
                    "I'm just...so sad...");
            return;
        }
        if (!isMember(player)) {
            sayRandom(player, "I don't know...", "What can I do, this is agony...", "If only I were smarter...");
            return;
        }
        boolean ready = CrimsonSideQuests.isReady(player, CrimsonSideQuests.POMTAIR_LAST, COOLDOWN);
        if (ready && player.getAmountInInventory(ItemType.ENCHANTED_BONE_MEAL) > 0) {
            player.removeItemFromPlayer(ItemType.ENCHANTED_BONE_MEAL, 1);
            boolean first = !CrimsonSideQuests.is(player, CrimsonSideQuests.POMTAIR_EVER);
            CrimsonSideQuests.flag(player, CrimsonSideQuests.POMTAIR_EVER);
            CrimsonSideQuests.stamp(player, CrimsonSideQuests.POMTAIR_LAST);
            CrimsonReputationService.add(player, CrimsonFaction.BARBARIAN, REPUTATION, "Pomtair");
            if (first) {
                say(player, "Oh sulfur! It worked! Potatoes!", "But how? I've been trying for years, and you just did it so easily...",
                        "Maybe I'm just...that bad...I'm worthless...", "Why can't I be successful like everyone else...",
                        "And I keep complaining...I'm so annoying...");
            } else {
                say(player, "Oh thanks, I really appreciate you! You are a hero to me.",
                        "You help me like someone would help an insect stuck in a puddle.");
            }
            return;
        }
        if (!ready) {
            sayRandom(player, "I'm so untalented, I'll probably burn the potatoes when I cook them.",
                    "I hope I won't lose this Enchanted Bone Meal...", "Why did I not think of this...");
            return;
        }
        if (CrimsonSideQuests.is(player, CrimsonSideQuests.POMTAIR_EVER)) {
            say(player, "I lost the Enchanted Bone Meal... of course...",
                    "Don't feel pressured to give me any more... I don't deserve this...");
            return;
        }
        sayRandom(player, "I tried everything.", "<o>sob", "Another day without potatoes...I can't handle it...",
                "Feels bad man, this is just terrible.");
    }
}
