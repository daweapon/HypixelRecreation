package dev.daweapon.type.catacombs.run;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.DungeonChestIcons;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.GUIDungeonRewardChest;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestRun;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestStorage;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonRewards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The post-boss reward chests, as both captured runs show them: when the score screen comes up, a sea lantern appears
 * under each chest in a + around the boss's spawn and each chest is built from armor stands wearing blocks and heads
 * ({@link RewardChestModels}). Every member's chests are rolled and stored at Croesus the moment the run is won; each
 * player who right-clicks a chest sees their own loot for it, and whatever they leave unopened waits at Croesus.
 */
public final class CatacombsRewardChests {
    private static final Sound OPEN = Sound.sound(Key.key("block.lever.click"), Sound.Source.BLOCK, 0.5f, 1f);

    private CatacombsRewardChests() {
    }

    public static void spawn(CatacombsRun run, DungeonScoreBreakdown score, @Nullable Pos centre, boolean realModels) {
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
        Pos at = centre != null ? centre : members.getFirst().getPosition();
        for (DungeonChestTier tier : tiers) place(run.instance(), at, tier, realModels, runIds);
    }

    /** The sea lantern under a tier's chest: one block below the centre's feet, offset in the captured +. */
    static int[] lantern(Pos centre, DungeonChestTier tier) {
        int[] offset = RewardChestModels.model(tier).map(model -> new int[]{model.lanternX(), model.lanternZ()})
                .orElseGet(() -> RewardChestModels.UNCAPTURED_LANTERNS.getOrDefault(tier, new int[]{0, 0}));
        return new int[]{centre.blockX() + offset[0], centre.blockY() - 1, centre.blockZ() + offset[1]};
    }

    private static void place(Instance instance, Pos centre, DungeonChestTier tier, boolean realModels, Map<UUID, String> runIds) {
        int[] lantern = lantern(centre, tier);
        instance.setBlock(lantern[0], lantern[1], lantern[2], Block.SEA_LANTERN);
        RewardChestModels.Model model = realModels ? RewardChestModels.model(tier).orElse(null) : null;
        if (model == null) {
            packModel(instance, lantern, tier, runIds);
            return;
        }
        Pos origin = new Pos(Math.floor(centre.x()) + 0.5, centre.blockY(), Math.floor(centre.z()) + 0.5);
        for (RewardChestModels.Part part : model.parts()) {
            RewardChestStand stand = new RewardChestStand(part.small(), part.headPitch(), stack(part),
                    player -> open(player, runIds, tier));
            stand.setInstance(instance, origin.add(part.dx(), part.dy(), part.dz()).withView(model.yaw(), 0));
        }
    }

    static ItemStack stack(RewardChestModels.Part part) {
        ItemStack.Builder builder = ItemStack.builder(part.material());
        if (part.texture() != null) {
            builder.set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(part.texture(), null)));
        }
        if (part.name() != null) builder.set(DataComponents.CUSTOM_NAME, Text.parse(part.name()).asComponent());
        return builder.build();
    }

    private static void packModel(Instance instance, int[] lantern, DungeonChestTier tier, Map<UUID, String> runIds) {
        Pos at = new Pos(lantern[0] + 0.5, lantern[1] + 1, lantern[2] + 0.5);
        Entity display = new Entity(EntityType.ITEM_DISPLAY);
        display.setNoGravity(true);
        display.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(DungeonChestIcons.modelStack(tier));
            meta.setDisplayContext(ItemDisplayMeta.DisplayContext.FIXED);
            meta.setScale(new Vec(1, 1, 1));
        });
        display.setInstance(instance, at.add(0, 0.5, 0));
        RewardChestStand stand = new RewardChestStand(false, 0, ItemStack.AIR, player -> open(player, runIds, tier));
        stand.setInstance(instance, at.sub(0, 1, 0));
    }

    static void open(SkyBlockPlayer player, Map<UUID, String> runIds, DungeonChestTier tier) {
        String id = runIds.get(player.getUuid());
        DungeonChestRun stored = id == null ? null : DungeonChestStorage.find(player, id);
        if (stored == null) {
            player.sendMessage("<c>These chests aren't yours!");
            return;
        }
        player.playSound(OPEN);
        player.openView(new GUIDungeonRewardChest(stored, tier));
    }
}
