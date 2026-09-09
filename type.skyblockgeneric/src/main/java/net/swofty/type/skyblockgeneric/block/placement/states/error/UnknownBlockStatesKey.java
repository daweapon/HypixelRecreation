package net.swofty.type.skyblockgeneric.block.placement.states.error;

// Thrown when a BlockState is set/read using a property key the underlying block doesn't have.
public class UnknownBlockStatesKey extends RuntimeException {

    public UnknownBlockStatesKey(String message) {
        super(message);
    }

}
