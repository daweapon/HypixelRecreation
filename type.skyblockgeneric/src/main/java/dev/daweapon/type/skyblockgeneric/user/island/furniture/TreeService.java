package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class TreeService {
    private static final int TICK_SECONDS = 5;

    private TreeService() {
    }

    public static void initialize() {
        MinecraftServer.getSchedulerManager().buildTask(TreeService::tick)
                .delay(TaskSchedule.seconds(TICK_SECONDS)).repeat(TaskSchedule.seconds(TICK_SECONDS)).schedule();
    }

    public static long today() {
        return SkyBlockCalendar.getElapsed() / SkyBlockCalendar.DAY;
    }

    public static boolean isTree(PlacedFurniture placed) {
        FurnitureDefinition definition = FurnitureCatalog.get(placed.itemId());
        return definition != null && definition.interaction() == FurnitureInteraction.TREE;
    }

    public static boolean hasTree(IslandFurniture furniture) {
        return furniture.all().stream().anyMatch(TreeService::isTree);
    }

    private static TreeState.Dice dice(@Nullable UUID owner) {
        return new TreeState.Dice() {
            @Override
            public boolean generates(double percent) {
                return LootRoll.roll(null, null, percent);
            }

            @Override
            public boolean purple(double percent) {
                ItemType forced = LootRoll.claim(owner, List.of(ItemType.PURPLE_CANDY, ItemType.GREEN_CANDY));
                if (forced != null) return forced == ItemType.PURPLE_CANDY;
                return LootRoll.roll(null, null, percent);
            }
        };
    }

    public static TreeState sync(IslandFurniture furniture, PlacedFurniture placed, @Nullable UUID owner) {
        long today = today();
        boolean placedBefore = furniture.storageOf(placed) != null;
        TreeState state = TreeState.parse(furniture.storageOf(placed), today);
        if (placedBefore) state.advance(today, dice(owner));
        furniture.setStorage(placed, state.serialize());
        return state;
    }

    public static void collect(IslandFurniture furniture, PlacedFurniture placed, SkyBlockPlayer player) {
        TreeState state = sync(furniture, placed, player.getUuid());
        if (state.held() == 0) {
            player.sendMessage("<c>I don't have any candy for you right now! Go away! >:(");
            return;
        }
        give(player, state);
        furniture.setStorage(placed, state.serialize());
    }

    public static void give(SkyBlockPlayer player, TreeState state) {
        if (state.green() > 0) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.GREEN_CANDY, state.green()));
            player.sendMessage("<6>You got <a>" + state.green() + " Green Candy <6>from the <5>Great Spook Tree<6>!");
        }
        if (state.purple() > 0) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.PURPLE_CANDY, state.purple()));
            player.sendMessage("<6>You got <5>" + state.purple() + " Purple Candy <6>from the <5>Great Spook Tree<6>!");
        }
        state.take();
    }

    private static void tick() {
        try {
            SkyBlockGenericLoader.forEachLoadedPlayer(viewer -> {
                SkyBlockIsland island = SkyBlockIsland.forInstance(viewer.getInstance());
                if (island == null || island != viewer.getSkyBlockIsland() || island.getFurniture() == null) return;
                IslandFurniture furniture = island.getFurniture();
                for (PlacedFurniture placed : furniture.all()) {
                    if (!isTree(placed)) continue;
                    if (furniture.storageOf(placed) == null) {
                        furniture.setStorage(placed, TreeState.fresh(today()).serialize());
                        continue;
                    }
                    TreeState state = TreeState.parse(furniture.storageOf(placed), today());
                    if (state.checkedOn(today())) continue;
                    if (state.advance(today(), dice(viewer.getUuid())) > 0) {
                        viewer.sendMessage("<a>Your <5>Great Spook Tree <a>has just sprouted a fruit!");
                    }
                    furniture.setStorage(placed, state.serialize());
                }
            });
        } catch (RuntimeException exception) {
            org.tinylog.Logger.error(exception, "Great Spook Tree tick failed");
        }
    }
}
