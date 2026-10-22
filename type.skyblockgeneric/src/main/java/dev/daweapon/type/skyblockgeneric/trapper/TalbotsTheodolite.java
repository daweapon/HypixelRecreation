package dev.daweapon.type.skyblockgeneric.trapper;

import dev.daweapon.type.skyblockgeneric.item.handlers.ability.AbilityRegistry;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility.AbilityActivation;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility.AbilityManaCost;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class TalbotsTheodolite {
    public static final String ABILITY_ID = "THEODOLITE_TRACK";
    private static final int COOLDOWN_TICKS = 200;
    private static final int MANA_COST = 100;
    private static final int ROUNDING = 5;

    private TalbotsTheodolite() {
    }

    public static void register() {
        AbilityRegistry.register(new RegisteredAbility(
                ABILITY_ID,
                "Track",
                (_, _) -> "<7>Indicates the approximate height difference and angle between you and your tracked mob.",
                AbilityActivation.RIGHT_CLICK,
                COOLDOWN_TICKS,
                new AbilityManaCost(MANA_COST),
                (player, _, _, _) -> track(player)));
    }

    private static boolean track(SkyBlockPlayer player) {
        if (!MushroomDesert.contains(player)) {
            player.sendMessage("<c>You can only track animals in the Mushroom Desert!");
            return false;
        }
        TrapperMob mob = TrapperService.mobOf(player);
        if (mob == null) {
            player.sendMessage("<c>You are not tracking any animal! Talk to <e>Trevor <c>for a task.");
            return false;
        }
        double dx = mob.getPosition().x() - player.getPosition().x();
        double dz = mob.getPosition().z() - player.getPosition().z();
        double bearing = Math.toDegrees(Math.atan2(-dx, dz));
        double relative = ((bearing - player.getPosition().yaw()) % 360 + 360) % 360;
        int angle = (int) Math.round(relative / ROUNDING) * ROUNDING % 360;
        int height = (int) Math.round((mob.getPosition().y() - player.getPosition().y()) / ROUNDING) * ROUNDING;
        String vertical = height == 0 ? "<a>level with you" : "<a>~" + Math.abs(height) + " blocks " + (height > 0 ? "above" : "below") + " you";
        player.sendMessage("<5><l>TRACKING! <7>Your animal is {}<7> at an angle of <e>{}°<7> from where you are facing.", vertical, angle);
        return true;
    }
}
