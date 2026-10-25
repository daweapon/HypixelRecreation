package dev.daweapon.type.skyblockgeneric.greatspook;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;

public final class GreatSpookIngredients {
    public static final String APPALLED_PUMPKIN_ID = "DECORATION_APPALLED_PUMPKIN";
    public static final String SWEET_AXE_ID = "SWEET_AXE";
    public static final String ENCHANTED_BONE_MEAL_ID = "ENCHANTED_BONE_MEAL";
    public static final double SWEET_FLESH_CHANCE = 100;
    public static final double DEAD_SEED_CHANCE = 100;
    public static final double FROZEN_SPIDER_CHANCE = 10;
    public static final Set<EntityType> ZOMBIES = Set.of(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER,
            EntityType.HUSK, EntityType.DROWNED);
    private static final int CHECK_TICKS = 10;

    private GreatSpookIngredients() {
    }

    public static void initialize() {
        MinecraftServer.getSchedulerManager().buildTask(GreatSpookIngredients::tick)
                .repeat(TaskSchedule.tick(CHECK_TICKS)).schedule();
    }

    public static boolean questOpen(SkyBlockPlayer player) {
        return GreatSpookEvent.isActive() && TyashoiProgress.introduced(player);
    }

    public static boolean festivalOrQuestOpen(SkyBlockPlayer player) {
        return TyashoiProgress.introduced(player)
                && (GreatSpookEvent.isActive() || SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.SPOOKY_FESTIVAL));
    }

    public static void give(SkyBlockPlayer player, ItemType type) {
        SkyBlockItem item = new SkyBlockItem(type);
        player.addAndUpdateItem(item);
        player.sendMessage("<7>You received <a>1x {}<7>!", item.getDisplayName());
    }

    private static void tick() {
        if (!GreatSpookEvent.isActive()) return;
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getInstance() == null || player.isDead()) continue;
            if (!APPALLED_PUMPKIN_ID.equals(WornArmor.id(player.getHelmet())) || !questOpen(player)) continue;
            if (!headUnderwater(player)) continue;
            player.setHelmet(new SkyBlockItem(ItemType.WET_PUMPKIN).getItemStack());
            player.sendMessage("<7>Your <f>Appalled Pumpkin <7>has transformed into a <5>Wet Pumpkin<7>!");
        }
    }

    private static boolean headUnderwater(SkyBlockPlayer player) {
        Pos eyes = player.getPosition().add(0, player.getEyeHeight(), 0);
        return player.getInstance().getBlock(eyes).compare(Block.WATER);
    }
}
