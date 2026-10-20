package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.type.skyblockgeneric.starlyn.StarlynAbiphone;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynDialogue;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class AbiphoneAgatha extends StarlynAbiphone {
    public AbiphoneAgatha() {
        super(StarlynSister.AGATHA, "Calls you with your <d>Agatha's Contest <7>results and opens her shop.");
    }

    @Override
    protected boolean canGiveContact(SkyBlockPlayer player) {
        return true;
    }

    @Override
    protected String[] contactBefore() {
        return StarlynDialogue.agathaContactBefore();
    }

    @Override
    protected String[] contactAfter() {
        return StarlynDialogue.agathaContactAfter();
    }
}
