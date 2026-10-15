package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.crimsonisle.faction.FactionVillagerNPC;
import dev.daweapon.type.crimsonisle.gui.faction.GUIQuestClaim;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Mollim's enchanting experiment (wiki: Mollim): a Titanic (or Colossal) Experience Bottle completes it
 * for +20 Mage Reputation and the first Rainbow II book. Players hand the bottle over by clicking him
 * while holding it instead of splashing it.
 */
public class NPCMollim extends FactionVillagerNPC {
    public static final int REPUTATION = 20;

    public NPCMollim() {
        super("<5>Mollim", CrimsonSkins.MOLLIM, new Pos(-132.5, 100, -789.5), CrimsonFaction.MAGE);
    }

    @Override
    protected void talk(SkyBlockPlayer player) {
        var hand = player.getItemInMainHand();
        ItemType held = hand.isAir() || !SkyBlockItem.isSkyBlockItem(hand) ? null
                : new SkyBlockItem(hand).getAttributeHandler().getPotentialType();
        boolean grand = held == ItemType.GRAND_EXP_BOTTLE;
        boolean titanic = held == ItemType.TITANIC_EXP_BOTTLE || held == ItemType.get("COLOSSAL_EXP_BOTTLE");
        if (!isMember(player)) {
            if (grand || titanic) say(player, "Is that how you behave in that other faction?", "Simply rude!");
            else say(player, "Leave me to my experiments, stranger!");
            return;
        }
        boolean done = CrimsonSideQuests.is(player, CrimsonSideQuests.MOLLIM_DONE);
        if (titanic) {
            if (done) {
                say(player, "Just because it worked before doesn't mean it will work again.",
                        "Please splash your experience onto someone who is less busy!");
                return;
            }
            player.removeItemFromPlayer(held, 1);
            CrimsonSideQuests.flag(player, CrimsonSideQuests.MOLLIM_DONE);
            CrimsonReputationService.add(player, CrimsonFaction.MAGE, REPUTATION, "Mollim");
            say(player, "That's it!", "Eureka!", "I created a new Enchantment!",
                    "As you helped greatly with this achievement, let me bestow upon you the first imbued book!")
                    .thenRun(() -> player.openView(new GUIQuestClaim("Claim Reward", new SkyBlockItem(ItemType.ENCHANTED_BOOK),
                            "<c>The Rainbow enchantment is not in the game yet!", () -> { })));
            return;
        }
        if (grand && !done) {
            player.removeItemFromPlayer(held, 1);
            long thrown = CrimsonSideQuests.get(player, CrimsonSideQuests.MOLLIM_GRAND) + 1;
            CrimsonSideQuests.set(player, CrimsonSideQuests.MOLLIM_GRAND, thrown);
            if (thrown == 1) say(player, "Yes! The table. it speaks to me!", "It's not quite there yet...");
            else say(player, "We are on the right track...", "But we need something stronger!");
            return;
        }
        if (done) {
            sayRandom(player, "My next work will be even more grandiose!", "Yes, yes...mhm...yes...interesting...");
            return;
        }
        if (CrimsonSideQuests.firstMeeting(player, "mollim")) {
            say(player, "Azax?", "Where is Azax?", "I need more bottles! More!",
                    "This experiment is close to succeeding, it could be my new Scarlet Prize!");
            return;
        }
        sayRandom(player, "I'm on the verge of inventing a new enchantment!", "So close...so close...");
    }
}
