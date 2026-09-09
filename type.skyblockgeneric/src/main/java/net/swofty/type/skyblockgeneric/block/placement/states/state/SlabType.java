package net.swofty.type.skyblockgeneric.block.placement.states.state;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

// Block state values for a slab's bottom/top/double placement.
public enum SlabType implements State<SlabType> {
    BOTTOM,
    TOP,
    DOUBLE;

    @Override
    public @NotNull String getKey() {
        return "type";
    }

    @Override
    public @NotNull String getValue() {
        return name().trim().toLowerCase();
    }

    @NotNull
    @Override
    public SlabType parse(String rawValue) {
        return Stream.of(values()).filter(shape -> shape.name().equalsIgnoreCase(rawValue.trim())).findFirst().orElse(BOTTOM);
    }
}
