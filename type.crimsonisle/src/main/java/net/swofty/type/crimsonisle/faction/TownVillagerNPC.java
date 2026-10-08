package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A talk-only town villager: an introduction the first time (if it has one), then one of its idle
 * lines at random. Built from {@link TownVillagers} rather than one class each.
 */
public class TownVillagerNPC extends FactionVillagerNPC {
    private final String[] introduction;
    private final String[][] idle;

    public TownVillagerNPC(String name, String texture, Pos home, CrimsonFaction faction, String[] introduction, String[]... idle) {
        super(name, texture, home, faction);
        this.introduction = introduction;
        this.idle = idle;
    }

    @Override
    protected void talk(SkyBlockPlayer player) {
        String key = "villager:" + getName().toLowerCase(Locale.ROOT).replace(' ', '_');
        if (introduction.length > 0 && CrimsonSideQuests.firstMeeting(player, key)) {
            say(player, introduction);
            return;
        }
        if (idle.length == 0) {
            say(player, introduction);
            return;
        }
        say(player, idle[ThreadLocalRandom.current().nextInt(idle.length)]);
    }
}
