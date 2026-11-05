package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.DungeonChestIcons;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.GUIDungeonRewardChest;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestRun;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestStorage;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonRewards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The post-boss reward chests: every member's chests are rolled and stored at Croesus the moment the run is
 * won, then one row of chests appears beside the party. Each player who right-clicks a chest sees their own
 * loot for it, and whatever they leave unopened waits at Croesus.
 */
public final class CatacombsRewardChests {
    private static final double SPACING = 2.0;
    private static final double FORWARD = 3.0;
    private static final double MODEL_BASE = 0.75;

    private CatacombsRewardChests() {
    }

    public static void spawn(CatacombsRun run, DungeonScoreBreakdown score) {
        List<SkyBlockPlayer> members = run.onlineMembers();
        if (members.isEmpty()) return;
        List<String> party = members.stream().map(member -> "<a>" + member.getUsername()).toList();
        Map<UUID, String> runIds = new HashMap<>();
        List<DungeonChestTier> tiers = List.of();
        for (SkyBlockPlayer member : members) {
            try {
                DungeonChestRun rolled = DungeonRewards.completeRun(member, run.floorId().floor(), run.floorId().mode(),
                        score.total(), party);
                if (rolled == null) continue;
                runIds.put(member.getUuid(), rolled.id());
                tiers = rolled.chests().stream().map(chest -> chest.tier()).toList();
            } catch (Exception exception) {
                Logger.error(exception, "Couldn't roll {}'s Catacombs reward chests", member.getUsername());
            }
        }
        if (runIds.isEmpty()) return;
        place(run.instance(), members.getFirst().getPosition(), tiers, runIds);
        members.stream().filter(member -> runIds.containsKey(member.getUuid())).forEach(member -> {
            member.sendMessage("<e>The reward chests have appeared! <7>Right-click a chest to see its loot.");
            member.sendMessage("<7>Chests you don't open are kept by <e>Croesus <7>in the Dungeon Hub for <a>72 hours<7>.");
        });
    }

    private static void place(Instance instance, Pos anchor, List<DungeonChestTier> tiers, Map<UUID, String> runIds) {
        double start = anchor.x() - (tiers.size() - 1) * SPACING / 2;
        int floorY = anchor.blockY();
        double z = anchor.blockZ() + FORWARD + 0.5;
        for (int i = 0; i < tiers.size(); i++) {
            DungeonChestTier tier = tiers.get(i);
            double x = Math.floor(start + i * SPACING) + 0.5;
            instance.setBlock((int) Math.floor(x), floorY, (int) Math.floor(z), pedestal(tier));
            double size = size(tier);
            Entity display = new Entity(EntityType.ITEM_DISPLAY);
            display.setNoGravity(true);
            display.editEntityMeta(ItemDisplayMeta.class, meta -> {
                meta.setItemStack(DungeonChestIcons.modelStack(tier));
                meta.setDisplayContext(ItemDisplayMeta.DisplayContext.FIXED);
                meta.setScale(new Vec(size, size, size));
            });
            display.setInstance(instance, new Pos(x, floorY + MODEL_BASE, z, 0, 0));
            InteractionEntity click = new InteractionEntity(1f, 1f, (player, _) -> open((SkyBlockPlayer) player, runIds, tier));
            click.setInstance(instance, new Pos(x, floorY + 0.5, z));
        }
    }

    private static void open(SkyBlockPlayer player, Map<UUID, String> runIds, DungeonChestTier tier) {
        String id = runIds.get(player.getUuid());
        DungeonChestRun stored = id == null ? null : DungeonChestStorage.find(player, id);
        if (stored == null) {
            player.sendMessage("<c>These chests aren't yours!");
            return;
        }
        player.openView(new GUIDungeonRewardChest(stored, tier));
    }

    private static double size(DungeonChestTier tier) {
        return tier == DungeonChestTier.WOOD || tier == DungeonChestTier.GOLD ? 1.0 : 2.0;
    }

    private static Block pedestal(DungeonChestTier tier) {
        Block slab = switch (tier) {
            case WOOD -> Block.SPRUCE_SLAB;
            case OBSIDIAN -> Block.NETHER_BRICK_SLAB;
            case BEDROCK -> Block.QUARTZ_SLAB;
            default -> Block.STONE_SLAB;
        };
        return slab.withProperty("type", "bottom");
    }
}
