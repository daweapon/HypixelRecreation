package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.skyblockgeneric.abiphone.impl.AbiphonePablo;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Pablo the painter wants a flower every 2 days for +25 Mage Reputation (wiki: Pablo).
public class NPCPablo extends CrimsonNPC implements NPCAbiphoneTrait {
    public static final int REPUTATION = 25;

    public NPCPablo() {
        super("<5>Pablo", CrimsonSkins.PABLO, new Pos(-130, 127.5, -815.5));
    }

    @Override
    public String getAbiphoneKey() {
        return "pablo";
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (!CrimsonReputationService.isMember(player, CrimsonFaction.MAGE)) {
            sayRandom(player, "I need quiet, please.", "Sorry, but I need to concentrate.");
            return;
        }
        if (!CrimsonSideQuests.isReady(player, CrimsonSideQuests.PABLO_LAST, AbiphonePablo.COOLDOWN)) {
            say(player, "I can sense a great deal of virtue in you. You are a source of inspiration.");
            return;
        }
        ItemType flower = CrimsonSideQuests.pabloFlower(player);
        String name = "<a>" + flower.getDisplayName() + "<f>";
        if (CrimsonSideQuests.firstMeeting(player, "pablo")) {
            say(player, "Hello.", "I am a modest painter.",
                    "Painting is a way to escape the harsh reality; I can create my own flawless world.",
                    "I paint all sorts of things, but it costs a lot of colored dyes.",
                    "Could you help me and bring me some flowers? Most flowers cannot spawn on the <c>Crimson Isle<f>.",
                    "I need a " + name + ".");
            return;
        }
        if (player.getAmountInInventory(flower) < 1) {
            say(player, "Could you bring me a " + name + "?");
            return;
        }
        player.removeItemFromPlayer(flower, 1);
        boolean first = !CrimsonSideQuests.is(player, CrimsonSideQuests.PABLO_EVER);
        CrimsonSideQuests.flag(player, CrimsonSideQuests.PABLO_EVER);
        CrimsonSideQuests.stamp(player, CrimsonSideQuests.PABLO_LAST);
        CrimsonReputationService.add(player, CrimsonFaction.MAGE, REPUTATION, "Pablo");
        if (first) say(player, "Thank you for supporting my passion.", "I often need flowers, feel free to come back in a few days.");
        else say(player, "Great, back to painting!");
    }
}
