package dev.daweapon.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import dev.daweapon.type.skyblockgeneric.block.placement.PlacementRule;
import dev.daweapon.type.skyblockgeneric.block.placement.states.BlockState;

// Fallback no-op placement rule used for blocks that don't match any other placement rule.
public class UnknownPlacement extends PlacementRule {

    public UnknownPlacement(Block block) {
        super(block);
    }

    @Override
    public boolean canPlace(BlockState blockState, BlockPlacementRule.PlacementState placementState) {
        return true;
    }

    @Override
    public boolean canUpdate(BlockState blockState, BlockPlacementRule.UpdateState updateState) {
        return false;
    }

    @Override
    public void update(BlockState blockState, BlockPlacementRule.UpdateState updateState) {

    }

    @Override
    public void place(BlockState blockState, BlockPlacementRule.PlacementState placementState) {

    }
}
