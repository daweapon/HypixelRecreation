package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.skyblockgeneric.crimson.questline.BarbarianStep;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Lasea studies lava fish: the Barbarian questline asks for a Flyfish and a Skeleton Fish of any tier (+100).
public class NPCLasea extends CrimsonNPC {
    private static final List<String> TIERS = List.of("BRONZE", "SILVER", "GOLD", "DIAMOND");

    public NPCLasea() {
        super("<d>Lasea", CrimsonSkins.LASEA, new Pos(-566, 108, -657));
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (CrimsonQuestlines.barbarian(player) != BarbarianStep.HELP_LASEA) {
            say(player, "We <c>Piglins<f> send our deads in the lava of the <c>Volcano<f> for their final journey.",
                    "The <c>Volcano<f> is everything to us, its <2>Sulphur<f> made us who we are, so it's only normal that we give everything back to the <c>Volcano<f>.",
                    "A long long time ago, when we cremated the corpse of our great <c>Chief Malak<f>, the <c>Volcano<f> erupted.",
                    "It was the largest eruption we have ever seen. As if the <c>Volcano <f>was talking to us.",
                    "And from that burst, thousands of <2>lava fishes <f>fell from the sky onto <c>Dragontail<f>.",
                    "We believe <c>Malak <f>and our ancestors found a new life inside the creatures of the lava.");
            return;
        }
        if (CrimsonQuestlines.counter(player, "lasea_asked") == 0) {
            CrimsonQuestlines.setCounter(player, "lasea_asked", 1);
            say(player, "Hello!", "Have you tried <2>lava fishing <f>before?",
                    "Us <c>Piglins <f>believe <2>lava fishes <f>are sacred beasts.",
                    "However recently something strange has been happening to the fishes.",
                    "I am trying to understand the cause of this turmoil.",
                    "My beliefs prevent me from fishing, but I cannot investigate without seeing some specimens myself.",
                    "Could you please bring me <2>1 Flyfish <f>and <2>1 Skeleton Fish<f>.",
                    "I don't know myself how to find those, but there is an expert who lives at the <2>top of the volcano<f>.",
                    "Their name is <c>Odger<f>.");
            return;
        }
        ItemType flyfish = firstHeld(player, "FLYFISH_");
        ItemType skeleton = firstHeld(player, "SKELETON_FISH_");
        if (flyfish == null || skeleton == null) {
            sayRandom(player, "I don't want to point fingers at anyone, but if anyone is messing with the lava, it's certainly not our faction.",
                    "<c>Odger <f>is a Goblin. I think he eats the fishes, raw...");
            return;
        }
        if (!CrimsonQuestlines.advance(player, BarbarianStep.HELP_LASEA, BarbarianStep.HELP_GRIS, 100)) return;
        player.removeItemFromPlayer(flyfish, 1);
        player.removeItemFromPlayer(skeleton, 1);
        say(player, "Thank you very much!", "I am glad we can count you in our ranks!",
                "I'll be studying those Fishes now.", "See you around.");
    }

    private static ItemType firstHeld(SkyBlockPlayer player, String prefix) {
        for (String tier : TIERS) {
            ItemType type = ItemType.get(prefix + tier);
            if (type != null && player.getAmountInInventory(type) > 0) return type;
        }
        return null;
    }
}
