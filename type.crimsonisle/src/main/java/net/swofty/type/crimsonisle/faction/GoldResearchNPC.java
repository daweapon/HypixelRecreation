package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crimsonisle.gui.faction.GUIQuestClaim;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Alchemage (Mage) and Baar (Barbarian) share one gold-research chain (wiki: Alchemage, Baar): eight
 * deliveries in order, then The Alchemist's Staff and +250 reputation with the player's faction.
 */
public abstract class GoldResearchNPC extends FactionVillagerNPC {
    public static final int REPUTATION = 250;

    public record Delivery(String itemId, int amount, String request) {
    }

    protected GoldResearchNPC(String name, String texture, Pos home, CrimsonFaction faction, boolean villager) {
        super(name, texture, home, faction, villager);
    }

    /** The eight deliveries; the two Diver's armour pieces differ between the two NPCs. */
    protected abstract List<Delivery> deliveries();

    @Override
    protected void talk(SkyBlockPlayer player) {
        boolean started = CrimsonSideQuests.is(player, CrimsonSideQuests.ALCHEMIST_STARTED);
        int stage = (int) CrimsonSideQuests.get(player, CrimsonSideQuests.ALCHEMIST_STAGE);
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction == null) {
            say(player, "Hello there, adventurer!", "You look like someone who could help me with my research!",
                    "If you join our faction I might have some tasks for you.", "I'll make it worth your while.");
            return;
        }
        if (stage >= CrimsonSideQuests.ALCHEMIST_STEPS) {
            say(player, "If I ever need your help, I will be sure to let you know!");
            return;
        }
        if (!started && faction != faction()) {
            say(player, "You have no business talking to me.");
            return;
        }
        if (!started) {
            CrimsonSideQuests.flag(player, CrimsonSideQuests.ALCHEMIST_STARTED);
            say(player, "Hello there, adventurer!", "I'm the local alchemist, I do research on different types of gold.",
                    "If you have some time maybe you could help me with some tasks.", "I'll make sure it's worth your time.",
                    "Talk to me again if you want to help out.");
            return;
        }
        Delivery delivery = deliveries().get(stage);
        ItemType item = ItemType.get(delivery.itemId());
        if (item == null || player.getAmountInInventory(item) < delivery.amount()) {
            say(player, delivery.request());
            return;
        }
        player.removeItemFromPlayer(item, delivery.amount());
        int next = stage + 1;
        CrimsonSideQuests.set(player, CrimsonSideQuests.ALCHEMIST_STAGE, next);
        if (next < CrimsonSideQuests.ALCHEMIST_STEPS) {
            say(player, deliveries().get(next).request());
            return;
        }
        CrimsonReputationService.add(player, faction, REPUTATION, "Gold Research");
        say(player, "Finally, my research is complete!", "You have single-handedly sped up my research by several years.",
                "As promised, here is your reward.")
                .thenRun(() -> player.openView(new GUIQuestClaim("Claim Reward", new SkyBlockItem(ItemType.ALCHEMISTS_STAFF),
                        () -> player.addAndUpdateItem(ItemType.ALCHEMISTS_STAFF))));
    }

    protected static List<Delivery> chain(Delivery thirdArmour, Delivery fourthArmour) {
        return List.of(
                new Delivery("GOLD_INGOT", 64, "To start out I'll need some generic gold to experiment on, could you get me a stack?"),
                new Delivery("GOLDEN_PLATE", 5, "Next I need some flat gold to test how reflective gold is, could you try forging gold into 5 gold plates?"),
                thirdArmour,
                fourthArmour,
                new Delivery("ENCHANTED_GOLD_BLOCK", 32, "Now I need a lot of compacted gold, it has to be extremely dense. A half stack should do."),
                new Delivery("GOLDEN_POWDER", 5, "There is a fine-grained gold substance somewhere in the Hub, I'll need 5 of that."),
                new Delivery("ENCHANTED_GOLDEN_CARROT", 32, "Next I'm going to need a vegetable that is made out of solid gold. I want to experiment with how gold interacts with organics, maybe you could find some, like a half stack?"),
                new Delivery("GOLDEN_BALL", 1, "I just need one last thing, there's an extremely dangerous scientist who sells an assortment of items, he has a special rounded type of gold. Try to convince him to sell you it."));
    }
}
