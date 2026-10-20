package dev.daweapon.type.skyblockgeneric.moonglade;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.hotf.HotfRewards;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MarshGathering {
    public static final String COLLECTOR_PERK = "collector";
    public static final String YUMMY_ATTRIBUTE = "C17";

    private static final Map<Long, Boolean> DEPLETED = new ConcurrentHashMap<>();

    private MarshGathering() {
    }

    public static double doubleChance(SkyBlockPlayer player) {
        return doubleChance(player, MarshResource.LUSHLILAC);
    }

    public static double doubleChance(SkyBlockPlayer player, MarshResource resource) {
        double percent = HotfService.perkValue(player, COLLECTOR_PERK);
        if (resource != MarshResource.RUBY_VEILSHROOM) {
            percent += AttributeEffectService.value(player.getHuntingData(), AttributeId.parse(YUMMY_ATTRIBUTE));
        }
        return Math.min(100, percent);
    }

    public static boolean handle(SkyBlockPlayer player, Point position, Block block) {
        MarshResource resource = MarshResource.ofBlock(block);
        if (resource == null || !HotfService.onForagingIsland() || !onIslandOf(resource) || player.getSkyblockDataHandler() == null) return false;
        Instance instance = player.getInstance();
        if (instance == null) return true;
        long key = ((long) position.blockX() & 0x3FFFFFFL) << 38 | ((long) position.blockZ() & 0x3FFFFFFL) << 12 | (position.blockY() & 0xFFFL);
        if (DEPLETED.putIfAbsent(key, Boolean.TRUE) != null) return true;
        instance.setBlock(position, resource.depletedBlock(block));
        ScheduleUtility.delay(() -> {
            instance.setBlock(position, block);
            DEPLETED.remove(key);
        }, resource.rollRespawnTicks());
        harvest(player, resource, position);
        return true;
    }

    private static boolean onIslandOf(MarshResource resource) {
        return HypixelConst.getTypeLoader().getType() == resource.getIsland();
    }

    private static void harvest(SkyBlockPlayer player, MarshResource resource, Point position) {
        int base = resource.rollAmount();
        int amount = base;
        if (LootRoll.roll(player.getUuid(), resource.getItem(), doubleChance(player, resource))) amount += base;

        deliver(player, resource.getItem(), amount);
        List<SkyBlockItem> drops = new ArrayList<>();
        drops.add(new SkyBlockItem(resource.getItem(), amount));
        HypixelEventHandler.callCustomEvent(new CustomBlockBreakEvent(player, resource.getSource(), position, drops, false));

        player.getSkills().increase(player, SkillCategories.FORAGING, resource.getForagingXpPerItem() * base);
        if (resource.getForestWhispers() > 0) HotfRewards.gatherWhispers(player, resource.getCurrency(), resource.getForestWhispers());
        for (int i = 0; i < base; i++) StarlynContests.recordGather(player, resource.getContestResource());
        ChapterTriggers.fire(player, resource.getChapterTrigger());
    }

    private static void deliver(SkyBlockPlayer player, ItemType item, int amount) {
        if (player.canInsertItemIntoSacks(item, amount)) {
            player.getSackItems().increase(item, amount);
            return;
        }
        player.addAndUpdateItem(item, amount);
    }
}
