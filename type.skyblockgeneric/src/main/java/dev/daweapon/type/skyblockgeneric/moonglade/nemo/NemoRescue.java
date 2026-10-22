package dev.daweapon.type.skyblockgeneric.moonglade.nemo;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.PlayerDeathOverrides;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public final class NemoRescue {
    public static final String DROWNED = "nemo.drowned";
    public static final String RESCUED = "nemo.rescued";
    private static final int LINE_DELAY_TICKS = 24;
    private static boolean registered;

    private static final List<List<String>> DIALOGUES = List.of(
            List.of("Ah, you're awake... <o>*inhales*</o>... I pulled you from the depths just in time.",
                    "The pressure down there is... <o>*exhales*</o>... unforgiving.",
                    "Visit my shop for <3>⚶ Respiration <f>...<o>*inhales*</o>... and <9>❍ Pressure Resistance <f>gear.",
                    "It's essential for... <o>*exhales*</o>... surviving down there."),
            List.of("And we meet again!... <o>*inhales*</o>... Seems the loch embrace is too strong?",
                    "Perhaps lighten the load with some proper gear next time... <o>*exhales*</o>... just a thought."),
            List.of("You're really testing the waters, aren't you... <o>*inhales*</o>... Literally.",
                    "A bit more <3>Respiration <f>and... <o>*exhales*</o>... <9>Pressure Resistance <f>might stop the loch from swallowing you whole.",
                    "Just... <o>*inhales*</o>... saying."),
            List.of("You again... <o>*inhales*</o>... I'm no hero, but I might need a cape at this rate.",
                    "Maybe equip yourself a bit better, and I can retire my rescue hat... <o>*exhales*</o>... just a suggestion."));

    private NemoRescue() {
    }

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        PlayerDeathOverrides.register(player -> {
            if (drownedInDepths(player)) ChapterTriggers.fire(player, DROWNED);
            return false;
        });
    }

    static boolean drownedInDepths(SkyBlockPlayer player) {
        if (player.getInstance() == null) return false;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(player.getPosition());
        if (region == null || region.getType() != RegionType.MURKWATER_DEPTHS) return false;
        if (player.getLastDamageSource() != null && player.getLastDamageSource().getType() == DamageType.DROWN) return true;
        Pos eyes = player.getPosition().add(0, player.getEyeHeight(), 0);
        return player.getInstance().getBlock(eyes).isLiquid();
    }

    public static boolean pending(SkyBlockPlayer player) {
        var data = ChapterService.data(player);
        return data.trigger(DROWNED) > data.trigger(RESCUED);
    }

    public static void deliver(SkyBlockPlayer player) {
        if (!pending(player)) return;
        long count = ChapterService.data(player).trigger(DROWNED);
        ChapterTriggers.add(player, RESCUED, count - ChapterService.data(player).trigger(RESCUED));
        List<String> lines = dialogue(count);
        net.minestom.server.MinecraftServer.getSchedulerManager().submitTask(new java.util.function.Supplier<>() {
            private int index;

            @Override
            public TaskSchedule get() {
                if (player.getInstance() == null || index >= lines.size()) return TaskSchedule.stop();
                player.sendMessage(Text.parse("<e>[NPC] <b>Nemo<f>: " + lines.get(index++)));
                return TaskSchedule.tick(LINE_DELAY_TICKS);
            }
        }, net.minestom.server.timer.ExecutionType.TICK_START);
    }

    public static List<String> dialogue(long drownCount) {
        int index = (int) Math.max(0, Math.min(drownCount, DIALOGUES.size()) - 1);
        return DIALOGUES.get(index);
    }
}
