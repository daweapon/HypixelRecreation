package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.town.CrimsonTownProgress;
import dev.daweapon.type.skyblockgeneric.crimson.town.GiveItemContact;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Carrolyn trades her contact for a Magma Core (added March 2026).
public class AbiphoneCarrolyn extends GiveItemContact {
    public AbiphoneCarrolyn() {
        super("carrolyn", "<5>Carrolyn", "Exports crops out of <5>Scarleton<7>.",
                "405db41a3555560be0d447f1c2edbc57dae5addccafbf90b3d0a802766db01bb", CrimsonTownProgress.CARROLYN_CONTACT);
    }

    @Override
    protected String[] ask(SkyBlockPlayer player) {
        return new String[]{"I suppose you want my <3>Abiphone Contact<f>.", "Well, what's in it for me?",
                "I'll tell you what - I want a <9>Magma Core<f>.", "Bring me that and I'm sure we can reach an agreement."};
    }

    @Override
    protected String[] missing() {
        return new String[]{"You don't have a <9>Magma Core<f>.",
                "Don't waste my time please, I have things to do and places to be."};
    }

    @Override
    protected String[] given() {
        return new String[]{"That does appear to be a <9>Magma Core<f>.",
                "Well, as per the terms of our agreement, I suppose you can have my contact.",
                "Only call me if you've got items to give me, please.", "Time is money, after all."};
    }

    @Override
    protected boolean take(SkyBlockPlayer player) {
        return player.getAmountInInventory(ItemType.MAGMA_CORE) > 0 && player.removeItemFromPlayer(ItemType.MAGMA_CORE, 1);
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        reply(player, "Only call me if you've got items to give me, please.", "Time is money, after all.");
    }
}
