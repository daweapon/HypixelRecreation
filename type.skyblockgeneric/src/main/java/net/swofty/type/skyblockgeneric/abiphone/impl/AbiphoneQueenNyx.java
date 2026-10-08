package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.faction.ReputationTier;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonContact;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Queen Nyx only gives her contact to Mage Faction Heroes (12,000 Mage Reputation).
public class AbiphoneQueenNyx extends CrimsonContact {
    public AbiphoneQueenNyx() {
        super("queen_nyx", CrimsonScene.QUEEN_NYX, "The leader of the <5>Mage Faction<7>.",
                "9b3bce03918e4baef733ce4780796a9278935ada0c01aaf5b177dfced6e87b8");
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (refusal(player) == null) {
            reply(player, "You have quite the reputation around here!",
                    "I suppose you have more than earned my contact! Here you go!");
        }
        super.onAdd(player, slot);
    }

    @Override
    protected String[] refusal(SkyBlockPlayer player) {
        if (CrimsonReputationService.tier(player, CrimsonFaction.MAGE).isAtLeast(ReputationTier.HERO)) return null;
        return new String[]{"My contact?", "Maybe if you reach enough <a>reputation<f>...but maybe not..."};
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        reply(player, "I didn't give you my contact for this reason.",
                "Only call me if you have something urgent to talk about.");
    }
}
