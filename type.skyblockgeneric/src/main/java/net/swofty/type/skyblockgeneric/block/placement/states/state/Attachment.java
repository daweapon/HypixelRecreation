package net.swofty.type.skyblockgeneric.block.placement.states.state;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

// Block state values for a bell's mounting style (ceiling/floor/single or double wall).
public enum Attachment implements State<Attachment> {
    CEILING, SINGLE_WALL, DOUBLE_WALL, FLOOR;

    @Override
    public @NotNull String getKey() {
        return "attachment";
    }

    @Override
    public @NotNull String getValue() {
        return name().trim().toLowerCase();
    }

    @Override
    public @NotNull Attachment parse(String rawValue) {
        return Stream.of(values()).filter(shape -> shape.name().equalsIgnoreCase(rawValue.trim())).findFirst().orElse(FLOOR);
    }
}
