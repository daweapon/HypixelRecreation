package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.type.skyblockgeneric.starlyn.StarlynAbiphone;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynBracket;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynDialogue;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class AbiphoneMiria extends StarlynAbiphone {
    public AbiphoneMiria() {
        super(StarlynSister.MIRIA, "Calls you with your <d>Miria's Contest <7>results and opens her shop.");
    }

    @Override
    protected boolean canGiveContact(SkyBlockPlayer player) {
        return StarlynContests.data(player, StarlynSister.MIRIA).reachedBracket(StarlynBracket.LEGENDARY);
    }

    @Override
    protected String[] contactBefore() {
        return StarlynDialogue.miriaContactBefore();
    }

    @Override
    protected String[] contactAfter() {
        return StarlynDialogue.miriaContactAfter();
    }

    @Override
    protected String[] refusal() {
        return StarlynDialogue.miriaContactRefusal();
    }
}
