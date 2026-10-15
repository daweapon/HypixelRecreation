package dev.daweapon.type.skyblockgeneric.block.placement.rules;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import dev.daweapon.type.skyblockgeneric.block.placement.PlacementRule;
import dev.daweapon.type.skyblockgeneric.block.placement.states.BlockState;
import dev.daweapon.type.skyblockgeneric.block.placement.states.state.IntegerState;

// Placement rule that sets a 16-step rotation state from the placing player's yaw (e.g. signs, heads on the floor).
public class RotationPlacement extends PlacementRule {

    private final IntegerState ROTATION = IntegerState.Of("rotation", 0, 0, 15);

    public RotationPlacement(Block block) {
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
        float yaw = placementState.playerPosition().yaw();

        if (!block().name().contains("skull")) {
            yaw = yaw + 180;
        }
        double num = (yaw * 16.0F / 360.0F) + 0.5D;
        int floor = (int) num;
        int x = (double) floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);

        blockState.set(ROTATION, x & 15);
    }
}
