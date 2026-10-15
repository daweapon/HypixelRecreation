package dev.daweapon.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import dev.daweapon.type.skyblockgeneric.block.placement.PlacementRule;
import dev.daweapon.type.skyblockgeneric.block.placement.states.BlockState;
import dev.daweapon.type.skyblockgeneric.block.placement.states.state.Axis;
import dev.daweapon.type.skyblockgeneric.block.placement.states.state.Facing;

// Placement rule that orients axis-based blocks (e.g. logs) according to the placed block face.
public class AxisPlacement extends PlacementRule {
    public AxisPlacement(Block block) {
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
        blockState.set(Axis.of(Facing.parse(placementState.blockFace())));
    }
}
