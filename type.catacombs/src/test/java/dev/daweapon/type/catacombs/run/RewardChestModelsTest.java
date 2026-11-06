package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.type.catacombs.engine.bonzo.BonzoArenaSpots;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardChestModelsTest {
    private static final Pos CENTRE = BonzoArenaSpots.BONZO_SPAWN;

    @Test
    void seaLanternsGoInTheCapturedPlus() {
        assertArrayEquals(new int[]{-47, 70, 20}, CatacombsRewardChests.lantern(CENTRE, DungeonChestTier.WOOD));
        assertArrayEquals(new int[]{-39, 70, 20}, CatacombsRewardChests.lantern(CENTRE, DungeonChestTier.GOLD));
        assertArrayEquals(new int[]{-43, 70, 16}, CatacombsRewardChests.lantern(CENTRE, DungeonChestTier.DIAMOND));
        assertArrayEquals(new int[]{-43, 70, 24}, CatacombsRewardChests.lantern(CENTRE, DungeonChestTier.EMERALD));
    }

    @Test
    void eachChestHasItsCapturedPartsFacingBonzo() {
        Map<DungeonChestTier, Integer> parts = Map.of(DungeonChestTier.WOOD, 5, DungeonChestTier.GOLD, 5,
                DungeonChestTier.DIAMOND, 9, DungeonChestTier.EMERALD, 9);
        Map<DungeonChestTier, Float> yaw = Map.of(DungeonChestTier.WOOD, -90f, DungeonChestTier.GOLD, 90f,
                DungeonChestTier.DIAMOND, 0f, DungeonChestTier.EMERALD, 180f);
        parts.forEach((tier, count) -> {
            RewardChestModels.Model model = RewardChestModels.model(tier).orElseThrow();
            assertEquals(count, model.parts().size(), tier.name());
            assertEquals(yaw.get(tier), model.yaw(), tier.name());
        });
        assertTrue(RewardChestModels.model(DungeonChestTier.OBSIDIAN).isEmpty());
    }

    @Test
    void partsStandExactlyWhereTheCaptureHadThem() {
        RewardChestModels.Model diamond = RewardChestModels.model(DungeonChestTier.DIAMOND).orElseThrow();
        List<double[]> captured = List.of(
                new double[]{-42.9375, 69.59375, 16.5}, new double[]{-42.3125, 69.59375, 16.5},
                new double[]{-42.875, 70.53125, 16.5}, new double[]{-42.40625, 70.53125, 16.5},
                new double[]{-42.625, 70.28125, 16.6875}, new double[]{-42.34375, 70.125, 16.5},
                new double[]{-42.96875, 70.125, 16.5}, new double[]{-42.90625, 70.625, 16.5},
                new double[]{-42.375, 70.625, 16.5});
        for (int index = 0; index < captured.size(); index++) {
            RewardChestModels.Part part = diamond.parts().get(index);
            assertEquals(captured.get(index)[0], CENTRE.x() + part.dx(), 1e-9);
            assertEquals(captured.get(index)[1], CENTRE.y() + part.dy(), 1e-9);
            assertEquals(captured.get(index)[2], CENTRE.z() + part.dz(), 1e-9);
        }
        RewardChestModels.Part wood = RewardChestModels.model(DungeonChestTier.WOOD).orElseThrow().parts().get(3);
        assertEquals(-46.5, CENTRE.x() + wood.dx(), 1e-9);
        assertEquals(71.3125, CENTRE.y() + wood.dy(), 1e-9);
        assertEquals("<9>Wooden Plank Chest", wood.name());
    }

    @Test
    void skullsTiltAndHeadsCarryTheirCapturedTextures() {
        for (DungeonChestTier tier : List.of(DungeonChestTier.WOOD, DungeonChestTier.GOLD, DungeonChestTier.DIAMOND, DungeonChestTier.EMERALD)) {
            for (RewardChestModels.Part part : RewardChestModels.model(tier).orElseThrow().parts()) {
                if (part.material() != Material.PLAYER_HEAD) continue;
                String json = new String(Base64.getMimeDecoder().decode(part.texture().strip()));
                assertTrue(json.contains("textures.minecraft.net/texture/"), tier + " " + part.name());
                boolean skull = part.name() != null && part.name().endsWith("Skull");
                assertEquals(skull ? -16f : 0f, part.headPitch(), tier + " " + part.name());
                if (skull) assertTrue(part.small());
            }
        }
        assertTrue(new String(Base64.getDecoder().decode(RewardChestModels.CHEST_DIAMOND_LEFT.strip()))
                .contains("2bb7b385e0aa748a7c6efdf3bcf209928e82471cc36e954337767c5e3d2e150e"));
    }
}
