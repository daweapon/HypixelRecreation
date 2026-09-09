package net.swofty.type.skyblockgeneric.block.placement.states.state;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

// Block state values for whether a segment is the top or bottom half (e.g. stairs, trapdoors).
public enum Half implements State<Half> {
    TOP,
    BOTTOM;

    @Override
    public @NotNull Half parse(String input) {
        return Stream.of(values()).filter(shape -> shape.name().equalsIgnoreCase(input.trim())).findFirst().orElse(BOTTOM);
    }

    @Override
    public @NotNull String getKey() {
        return "half";
    }

    @Override
    public @NotNull String getValue() {
        return name().trim().toLowerCase();
    }
}
