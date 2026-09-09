package net.swofty.type.skyblockgeneric.block.placement.states.error;

// Thrown when a BlockState is queried with an ambiguous or unspecified state type.
public class StateError extends RuntimeException {

    public StateError(String message) {
        super(message);
    }

}
