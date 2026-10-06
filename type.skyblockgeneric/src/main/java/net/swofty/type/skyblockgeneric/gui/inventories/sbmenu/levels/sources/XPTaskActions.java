package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.sources;

import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.GUIBestiary;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.collection.GUICollections;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.skills.GUISkills;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;

import java.util.function.Consumer;

/**
 * Clicking an XP task on Hypixel takes you to the menu that task is about, rather than to a
 * breakdown screen. Tasks with nowhere to go say "Go earn some XP!" instead.
 */
public final class XPTaskActions {

    private XPTaskActions() {
    }

    private static Consumer<ViewContext> destination(SkyBlockXPSource source) {
        return switch (source) {
            case SKILL_LEVEL_UP -> ctx -> ctx.push(new GUISkills());
            case COLLECTIONS, BOSS_COLLECTIONS -> ctx -> ctx.push(new GUICollections());
            case BESTIARY_PROGRESS, COLLECT_MOB_TYPES -> ctx -> ctx.push(new GUIBestiary());
            case SLAY_DRAGONS, DEFEAT_SLAYERS, DEFEAT_KUUDRA, DEFEAT_ARACHNE ->
                    ctx -> ctx.push(new GUIXPSubTasks(), new GUIXPSubTasks.State(source));
            default -> null;
        };
    }

    public static void click(ViewContext ctx, SkyBlockXPSource source) {
        Consumer<ViewContext> destination = destination(source);
        if (destination == null) return;

        RecentlyViewedXPTasks.record(ctx.player().getUuid(), source);
        destination.accept(ctx);
    }
}
