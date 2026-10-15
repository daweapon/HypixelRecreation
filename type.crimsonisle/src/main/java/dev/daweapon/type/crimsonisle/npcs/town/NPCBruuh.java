package dev.daweapon.type.crimsonisle.npcs.town;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonChoices;
import dev.daweapon.type.crimsonisle.faction.FactionVillagerNPC;
import dev.daweapon.type.crimsonisle.town.TownSkins;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.crimson.town.CrimsonTownProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Bruuh, who built his Dragontail house around a Fairy Soul and charges 1,000,000 coins for access
 * (wiki Bruuh). One of the Barbarian villagers for the "talk to 4 villagers" step.
 */
public class NPCBruuh extends FactionVillagerNPC {
    public NPCBruuh() {
        super(CrimsonTownProgress.BRUUH, TownSkins.BRUUH, new Pos(-635.5, 124, -688.5), CrimsonFaction.BARBARIAN);
    }

    @Override
    protected void talk(SkyBlockPlayer player) {
        if (CrimsonSideQuests.is(player, CrimsonTownProgress.BRUUH_PAID)) {
            sayRandom(player, "Once I found this Fairy Soul I decided to build my house around it.",
                    "I actually use the Fairy Soul as a lamp. They kinda glow in the dark a bit.");
            return;
        }
        say(player, "Hold it right there!",
                "I have a <d>Fairy Soul <f>inside my property. If you want it you will have to pay!",
                "You can get access to my house for <61,000,000 coins<f>!", "Do you want access?")
                .thenRun(() -> CrimsonChoices.yesNo(player, "bruuh_toll", () -> pay(player),
                        () -> say(player, "In that case you better get off my property before I call the guards!")));
    }

    private void pay(SkyBlockPlayer player) {
        if (CrimsonSideQuests.is(player, CrimsonTownProgress.BRUUH_PAID)) return;
        if (player.getCoins() < CrimsonTownProgress.BRUUH_TOLL) {
            say(player, "Are you trying to scam me, bruh? Get off my property!");
            return;
        }
        player.removeCoins(CrimsonTownProgress.BRUUH_TOLL);
        player.getAchievementHandler().completeAchievement("skyblock.scam");
        CrimsonSideQuests.flag(player, CrimsonTownProgress.BRUUH_PAID);
        say(player, "Congratulations! You can now come and go as you please!");
    }
}
