package dev.daweapon.type.skyblockgeneric.crimson.town;

import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.fairysouls.FairySoulGates;

import java.time.Duration;

/** Per-profile state of the remaining Crimson Isle town NPCs (Bruuh's toll, Aranya's meals, item-for-contact deals). */
public final class CrimsonTownProgress {
    public static final String BRUUH = "<d>Bruuh";
    public static final long BRUUH_TOLL = 1_000_000;
    /** The soul inside Bruuh's house in Dragontail (-644, 125, -689). */
    public static final int BRUUH_FAIRY_SOUL = 168;

    public static final String BRUUH_PAID = "bruuh:paid";
    public static final String RULENOR_PAID = "rulenor:paid";
    public static final String ARANYA_LAST_MEAL = "aranya:last";
    public static final String ARANYA_FED = "aranya:fed";
    public static final String ARANYA_REWARD = "aranya:reward";
    public static final String CARROLYN_CONTACT = "carrolyn:contact";
    public static final String ODGER_CONTACT = "odger:contact";
    public static final Duration ARANYA_COOLDOWN = Duration.ofDays(7);

    private CrimsonTownProgress() {
    }

    public static void registerGates() {
        FairySoulGates.register(BRUUH_FAIRY_SOUL, player -> CrimsonSideQuests.is(player, BRUUH_PAID),
                player -> CrimsonScene.create().say(BRUUH, "Nice try, bruh! Pay the toll to get the soul!").play(player));
    }

    /** /maxprofile: Bruuh's house unlocked and every Carrolyn exportation done. Coins and contacts are left alone. */
    public static void maxOut(SkyBlockPlayer player) {
        CrimsonSideQuests.flag(player, BRUUH_PAID);
        CarrolynExports.maxOut(player);
    }
}
