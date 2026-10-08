package net.swofty.type.skyblockgeneric.minion;

import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.swofty.type.generic.utility.MathUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Base type for a minion definition: its tiers, armor colours, placement expectations, and per-tick action.
public abstract class SkyBlockMinion {
    public abstract List<MinionTier> getTiers();
    public abstract Color getBootColour();
    public abstract Color getLeggingsColour();
    public abstract Color getChestplateColour();
    public abstract List<MinionExpectation> getExpectations();
    public abstract MinionAction getAction();

    public List<String> getDescription(String name) {
        return List.of(
                "<7>Place this minion and it will start",
                "<7>generating and mining " + name + "!",
                "<7>Requires an open area to place",
                "<7>" + name + ". Minions also work",
                "<7>you are offline!");
    }

    public List<String> getAbilityLore(int tier) {
        return List.of();
    }

    public int getBonusSpeedPercentage(IslandMinionData.IslandMinion minion) {
        return 0;
    }

    /**
     * Divides the action time after every percentage speed bonus, for minions whose fuel multiplies
     * speed rather than adding to it.
     */
    public double getSpeedMultiplier(IslandMinionData.IslandMinion minion) {
        return 1;
    }

    public record MinionTier(int tier, float timeBetweenActions, int storage, String texture, Material heldItem, boolean craftable) {
        public int getSlots() {
            return storage / 64;
        }
    }

    public interface MinionExpectation {
        boolean meetsExpectation(Instance container,
                                 IslandMinionData.IslandMinion minion);
    }

    public record BlockExpectation(int yLevel, Block... material) implements MinionExpectation {
        @Override
        public boolean meetsExpectation(Instance container,
                                        IslandMinionData.IslandMinion minion) {
            int updatedYLevel = minion.getPosition().blockY() + yLevel;
            List<Pos> positions = MathUtility.getRangeExcludingSelf(minion.getPosition()
                    .withY(updatedYLevel), 2 + minion.getBonusRange());

            for (Pos position : positions) {
                if (!Arrays.asList(material).contains(container.getBlock(position))) {
                    return false;
                }
            }
            return true;
        }
    }

    public record MobGapExpectation(int yGap) implements MinionExpectation {
        @Override
        public boolean meetsExpectation(Instance container,
                                        IslandMinionData.IslandMinion minion) {
            List<Pos> horizontalPositions = MathUtility.getRangeExcludingSelf(
                    minion.getPosition(), 2 + minion.getBonusRange());
            List<Pos> verticalPositions = new ArrayList<>();

            for (Pos pos : horizontalPositions) {
                verticalPositions.add(pos.add(0, 1, 0));
                verticalPositions.add(pos.add(0, 2, 0));
                verticalPositions.add(pos);

                verticalPositions.add(pos.add(0, -1, 0));
                verticalPositions.add(pos.add(0, -2, 0));
                verticalPositions.add(pos.add(0, -3, 0));
            }

            for (Pos pos : verticalPositions) {
                if (container.getBlock(pos).air()) {
                    // Check if the gap is big enough above
                    boolean isBigEnough = true;
                    for (int i = 0; i < yGap; i++) {
                        if (!container.getBlock(pos.add(0, i, 0)).air()) {
                            isBigEnough = false;
                            break;
                        }
                    }
                    if (isBigEnough) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
