package net.swofty.type.crimsonisle.npcs.kuudra;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.FollowerStep;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import net.swofty.type.skyblockgeneric.crimson.kuudra.npc.KuudraFollowerNPC;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Arch, on the Stronghold roof, hands out the Follower Chestplate once six helpers in full Ember or
 * Rekindled Ember Armor with a level 81+ Kuudra Pet stand in pairs in the Stronghold, Dragontail
 * Townsquare and Scarleton Plaza. With that in place anyone on the server can claim it.
 */
public class NPCArch extends KuudraFollowerNPC {
    private static final List<RegionType> HELPER_SPOTS = List.of(RegionType.STRONGHOLD,
            RegionType.DRAGONTAIL_TOWNSQUARE, RegionType.SCARLETON_PLAZA);
    private static final int HELPERS_PER_SPOT = 2;

    public NPCArch() {
        super("<c>Arch", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYwOTkwYmY4OTE0NTQ0ZDNmNThhY2Q5MzQxY2Y1MWUxZjUzNzU1NjZjMjc1NjY5MmVjODFlYzVhNTRkMTgwYmMifX19",
                new Pos(-360.5, 189, -552.5));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (KuudraFollowerRules.holding(player, "KUUDRA_CAVITY") && KuudraFollowerProgress.plaqueStage(player) != null
                && !KuudraFollowerProgress.has(player, FollowerStep.DECAY_ARCH)) {
            if (KuudraFollowerRules.kuudraPetLevel(player) >= KuudraFollowerRules.DECAY_PET_LEVEL) {
                setDialogue(player, "plaque_pet").thenRun(() -> KuudraFollowerProgress.decay(player, FollowerStep.DECAY_ARCH));
            } else {
                setDialogue(player, "plaque_no_pet");
            }
            return;
        }
        if (!KuudraFollowerProgress.has(player, FollowerStep.CHESTPLATE_CLAIMED) && helpersInPlace(player)) {
            setDialogue(player, "united").thenRun(() -> {
                if (KuudraFollowerProgress.has(player, FollowerStep.CHESTPLATE_CLAIMED)) return;
                KuudraFollowerProgress.complete(player, FollowerStep.CHESTPLATE_CLAIMED);
                player.addAndUpdateItem(ItemType.KUUDRA_FOLLOWER_CHESTPLATE);
            });
            return;
        }
        setDialogue(player, KuudraFollowerRules.wearingEmberArmor(player) ? "ember" : "hello");
    }

    private static boolean helpersInPlace(SkyBlockPlayer player) {
        if (player.getInstance() == null) return false;
        Map<RegionType, Integer> helpers = new EnumMap<>(RegionType.class);
        for (Player online : player.getInstance().getPlayers()) {
            if (!(online instanceof SkyBlockPlayer helper) || !isHelper(helper)) continue;
            SkyBlockRegion region = helper.getRegion();
            if (region != null && HELPER_SPOTS.contains(region.getType())) helpers.merge(region.getType(), 1, Integer::sum);
        }
        return HELPER_SPOTS.stream().allMatch(spot -> helpers.getOrDefault(spot, 0) >= HELPERS_PER_SPOT);
    }

    private static boolean isHelper(SkyBlockPlayer player) {
        return KuudraFollowerRules.wearingEmberArmor(player)
                && KuudraFollowerRules.kuudraPetLevel(player) >= KuudraFollowerRules.ARCH_PET_LEVEL;
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "Hello traveler!",
                        "Kuudra will return one day but we must first put aside our differences and be ready.",
                        "We must show Kuudra that we stand united for its return."
                ).build(),
                DialogueSet.builder().key("ember").lines(
                        "Hello traveler!",
                        "That's a very impressive armor set you are wearing, Kuudra would be impressed!",
                        "Kuudra will return one day but we must first put aside our differences and be ready.",
                        "We must show Kuudra that we stand united for its return."
                ).build(),
                DialogueSet.builder().key("united").lines(
                        "We are united! Kuudra SHOW YOURSELF TO US.",
                        "...",
                        "...",
                        "...",
                        "Why didn't it show itself...",
                        "Maybe it won't ever return...",
                        "Thank you for your dedication adventurer, here is a token of my gratitude."
                ).build(),
                DialogueSet.builder().key("plaque_no_pet").lines(
                        "I see you holding something there...",
                        "And it seems as though you need some assistance.",
                        "I would be able to provide it, but you came without the correct friend."
                ).build(),
                DialogueSet.builder().key("plaque_pet").lines(
                        "Ah yes! You are here with the right companion.",
                        "It seems as though I can now properly assist you.",
                        "Let me go ahead and help you break down that item.",
                        "I wish you luck on your journey, adventurer!"
                ).build()
        };
    }
}
