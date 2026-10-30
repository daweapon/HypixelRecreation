package dev.daweapon.type.skyblockgeneric.miningevent;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.AbilityRegistry;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility.AbilityActivation;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility.AbilityManaCost;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

public final class FallenStarLozenge {
    public static final String ABILITY_ID = "REZONATE";
    private static final String ITEM_ID = "FALLEN_STAR_TRACKER";
    private static final String CLAIMED = "cult.lozenge_claimed";
    private static final int COOLDOWN_TICKS = 20;
    private static final int MANA_COST = 20;
    private static final double MAX_FACING_ANGLE = 100;
    private static final double VERTICAL_UP = 12;
    private static final double VERTICAL_WAY_UP = 60;
    private static final double AT_STAR = 4;
    private static final double[] DISTANCE_STEPS = {10, 30, 75, 150, 300};
    private static final String[] DISTANCE_LABELS = {"<d>VERY CLOSE!", "CLOSE BY AHEAD!", "AHEAD!", "FAR AHEAD!", "VERY FAR AHEAD!", "VERY VERY FAR AHEAD!"};
    private static final String PREFIX = "<e>The <5>lozenge <e>resonates: ";

    private FallenStarLozenge() {
    }

    public static boolean claimed(SkyBlockPlayer player) {
        return ChapterTriggers.done(player, CLAIMED);
    }

    public static void claim(SkyBlockPlayer player) {
        ChapterTriggers.fire(player, CLAIMED);
        player.addAndUpdateItem(ItemType.valueOf(ITEM_ID));
    }

    public static void register() {
        AbilityRegistry.register(new RegisteredAbility(
                ABILITY_ID,
                "Rezonate",
                (_, _) -> "<7>Leads you to the nearest <5>Fallen Star <7>while in the <2>Dwarven Mines<7>.",
                AbilityActivation.RIGHT_CLICK,
                COOLDOWN_TICKS,
                new AbilityManaCost(MANA_COST),
                (player, _, _, _) -> resonate(player)));
    }

    private static boolean resonate(SkyBlockPlayer player) {
        if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_DWARVEN_MINES) {
            player.sendMessage("<c>The lozenge has no reaction on this island!");
            return false;
        }
        Pos star = RandomMiningEvents.fallenStarPosition();
        if (star == null) {
            player.sendMessage(PREFIX + "<c>NO FALLEN STAR!");
            return false;
        }
        player.sendMessage(PREFIX + describe(player.getPosition(), star));
        return true;
    }

    static String describe(Pos from, Pos star) {
        double dx = star.x() - from.x();
        double dz = star.z() - from.z();
        double dy = star.y() - from.y();
        double horizontal = Math.hypot(dx, dz);
        if (horizontal <= AT_STAR && Math.abs(dy) <= AT_STAR) return "<d>VERY CLOSE! <5>EH EH!";
        double bearing = Math.toDegrees(Math.atan2(-dx, dz));
        double relative = Math.abs(((bearing - from.yaw()) % 360 + 540) % 360 - 180);
        if (relative > MAX_FACING_ANGLE) return "<c>NOT THIS WAY";
        String vertical = "";
        if (Math.abs(dy) >= VERTICAL_UP) {
            boolean up = dy > 0;
            boolean way = Math.abs(dy) >= VERTICAL_WAY_UP;
            vertical = "<a>" + (way ? (up ? "↑ WAY UP!" : "↓ WAY DOWN!") : (up ? "⬆ UP!" : "⬇ DOWN!")) + "<f>, ";
        }
        String label = DISTANCE_LABELS[DISTANCE_LABELS.length - 1];
        for (int i = 0; i < DISTANCE_STEPS.length; i++) {
            if (horizontal < DISTANCE_STEPS[i]) {
                label = DISTANCE_LABELS[i];
                break;
            }
        }
        return vertical.isEmpty() ? label : vertical + "<b>" + label;
    }
}
