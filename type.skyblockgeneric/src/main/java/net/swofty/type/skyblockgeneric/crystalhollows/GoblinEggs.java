package net.swofty.type.skyblockgeneric.crystalhollows;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.DiamondGoblin;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.GoldenGoblin;
import net.swofty.type.skyblockgeneric.essence.EssencePerks;
import net.swofty.type.skyblockgeneric.essence.EssenceShops;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.region.mining.MiningBlockClassification;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Throwing a Goblin Egg (wiki: Goblin Egg, Green Goblin Egg; NEU Golden Goblin lore): a chance per colour to spawn a
 * Golden Goblin, replaced by a Diamond Goblin 3.33% of the time (raised by Gemma's Return to Sender). Golden Goblins
 * never spawn on the Private Island. No other colour has a throw effect; only the Golden Goblin chance differs.
 * The wiki gives no lifetime for the spawned goblin, so it despawns after two minutes as an estimate.
 */
public final class GoblinEggs {
    public static final String HANDLER_ID = "GOBLIN_EGG_THROW";
    private static final double DIAMOND_BASE_CHANCE = 3.33;
    private static final double ORE_CHANCE = 0.1;
    private static final double HARD_STONE_CHANCE = 0.01;
    private static final long LIFETIME_TICKS = 2400;
    private static final Map<ItemType, Double> GOLDEN_CHANCE = Map.of(
            ItemType.GOBLIN_EGG, 2D,
            ItemType.GREEN_GOBLIN_EGG, 2.5,
            ItemType.YELLOW_GOBLIN_EGG, 3.33,
            ItemType.RED_GOBLIN_EGG, 5D,
            ItemType.BLUE_GOBLIN_EGG, 10D);

    private GoblinEggs() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(HANDLER_ID, InteractableItemConfig.builder()
                .rightClickHandler(GoblinEggs::throwEgg)
                .build());
    }

    public static double goldenChance(ItemType egg) {
        return GOLDEN_CHANCE.getOrDefault(egg, 0D);
    }

    public static double diamondChance(SkyBlockPlayer player) {
        return DIAMOND_BASE_CHANCE * (1 + returnToSender(player) / 100);
    }

    private static void throwEgg(SkyBlockPlayer player, SkyBlockItem item) {
        ItemType egg = item.getAttributeHandler().getPotentialType();
        if (player.getInstance() == null || player.getItemInMainHand().isAir()) return;
        player.setItemInMainHand(player.getItemInMainHand().withAmount(player.getItemInMainHand().amount() - 1));
        if (HypixelConst.isIslandServer()) return;
        if (!LootRoll.roll(player.getUuid(), null, goldenChance(egg))) return;
        spawnGoblin(player, landingSpot(player));
    }

    public static double oreSpawnChance(Material material, RegionType region) {
        if (HardStone.isHardStone(material, region)) return HARD_STONE_CHANCE;
        if (CrystalHollowsZones.isCrystalHollows(region)) {
            if (CrystalHollowsOre.of(material, region) != null || isMithril(material)) return ORE_CHANCE;
            return 0;
        }
        return isCustomOre(material) ? ORE_CHANCE : 0;
    }

    public static void spawnFromOre(SkyBlockPlayer player, Material material, RegionType region, Point point) {
        if (HypixelConst.isIslandServer() || player.getInstance() == null) return;
        if (!LootRoll.roll(player.getUuid(), null, oreSpawnChance(material, region))) return;
        spawnGoblin(player, new Pos(point.x() + 0.5, point.y() + 1, point.z() + 0.5));
    }

    private static boolean isMithril(Material material) {
        for (MineableBlock block : MineableBlock.values()) {
            if (block.getMaterial() == material && block.name().startsWith("MITHRIL")) return true;
        }
        return false;
    }

    private static boolean isCustomOre(Material material) {
        for (MineableBlock block : MineableBlock.values()) {
            if (block.getMaterial() == material && block.getBlockType().equals(MiningBlockClassification.oreBlock())) return true;
        }
        return false;
    }

    private static void spawnGoblin(SkyBlockPlayer player, Pos landing) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        SkyBlockMob goblin = random.nextDouble() * 100 < diamondChance(player) ? new DiamondGoblin() : new GoldenGoblin();
        goblin.setInstance(player.getInstance(), landing);
        if (goblin instanceof GoldenGoblin) {
            player.getAchievementHandler().addProgressByTrigger("skyblock.the_next_generation_trigger", 1);
        }
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!goblin.isRemoved() && !goblin.isDead()) goblin.remove();
        }).delay(TaskSchedule.tick((int) LIFETIME_TICKS)).schedule();
    }

    private static Pos landingSpot(SkyBlockPlayer player) {
        var target = player.getTargetBlockPosition(6);
        if (target != null) return new Pos(target.x() + 0.5, target.y() + 1, target.z() + 0.5);
        return player.getPosition().add(player.getPosition().direction().mul(3));
    }

    private static double returnToSender(SkyBlockPlayer player) {
        return EssenceShops.shop(EssenceType.DIAMOND)
                .flatMap(shop -> shop.perks().stream().filter(perk -> perk.id().equals("return_to_sender")).findFirst())
                .map(perk -> EssencePerks.value(player, perk))
                .orElse(0D);
    }
}
