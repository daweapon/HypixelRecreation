package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crimson.town.CrimsonTownProgress;
import net.swofty.type.skyblockgeneric.crimson.town.GiveItemContact;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingRegistry;
import net.swofty.type.skyblockgeneric.fishing.catches.TrophyTier;
import net.swofty.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Odger trades his contact for any Diamond-tier Trophy Fish.
public class AbiphoneOdger extends GiveItemContact {
    public AbiphoneOdger() {
        super("odger", "<f>Odger", "The <6>Trophy Fish <7>expert atop the Volcano.",
                "973ec2ad381fc8831a8a6a238f38d8006ca51d56542dd9919e4955b9ac89af4d", CrimsonTownProgress.ODGER_CONTACT);
    }

    @Override
    protected String[] ask(SkyBlockPlayer player) {
        return new String[]{"Odger put name in funny device? Hmm...", "Help Odger. Then yes.",
                "Odger like Lasea. Lasea like fish. Odger like fish.", "Bring Odger. Trophy Fish. <b>Diamond<f>. Any kind."};
    }

    @Override
    protected String[] missing() {
        return new String[]{"You don't have what I need! Nice try!"};
    }

    @Override
    protected String[] given() {
        return new String[]{"Diamond fish. You have. For Odger?", "Thank you. Odger says.",
                "Trophy Fish. For Lasea.", "Contanct. For you. Here."};
    }

    @Override
    protected boolean take(SkyBlockPlayer player) {
        for (TrophyFishDefinition fish : FishingRegistry.getTrophyFish()) {
            ItemType diamond = ItemType.get(fish.itemStem() + "_" + TrophyTier.DIAMOND.name());
            if (diamond != null && player.getAmountInInventory(diamond) > 0) {
                return player.removeItemFromPlayer(diamond, 1);
            }
        }
        return false;
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        replyRandom(player, "Odger fishing. Call later.", "Trophy Fish? Bring to Odger.");
    }
}
