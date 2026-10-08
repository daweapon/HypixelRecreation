package net.swofty.type.crimsonisle.town;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A talk-only Crimson Isle NPC that is not one of the faction villagers: its introduction the first
 * time (when it has one), then one of its idle lines at random.
 */
public class TalkNPC extends CrimsonNPC {
    private final String[] introduction;
    private final String[][] idle;

    public TalkNPC(String name, String texture, Pos home, String[] introduction, String[]... idle) {
        super(name, texture, home);
        this.introduction = introduction;
        this.idle = idle;
    }

    public TalkNPC(String name, EntityType entityType, Pos home, String[] introduction, String[]... idle) {
        super(name, entityType, home);
        this.introduction = introduction;
        this.idle = idle;
    }

    protected String meetingKey() {
        return "town:" + getName().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (introduction.length > 0 && CrimsonSideQuests.firstMeeting(player, meetingKey())) {
            say(player, introduction);
            return;
        }
        String[][] lines = idleLines(player);
        if (lines.length == 0) {
            say(player, introduction);
            return;
        }
        say(player, lines[ThreadLocalRandom.current().nextInt(lines.length)]);
    }

    protected String[][] idleLines(SkyBlockPlayer player) {
        return idle;
    }

    protected static String[] lines(String... lines) {
        return lines;
    }
}
