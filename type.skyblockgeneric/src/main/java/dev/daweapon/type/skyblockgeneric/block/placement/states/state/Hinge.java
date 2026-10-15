package dev.daweapon.type.skyblockgeneric.block.placement.states.state;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

// Block state values for a door's hinge side.
public enum Hinge implements State<Hinge> {
    LEFT, RIGHT;

    @NotNull
    @Override
    public String getKey() {
        return "hinge";
    }

    @NotNull
    @Override
    public String getValue() {
        return name().trim().toLowerCase();
    }

    @NotNull
    @Override
    public Hinge parse(String rawValue) {
        return Stream.of(values()).filter(shape -> shape.name().equalsIgnoreCase(rawValue.trim())).findFirst().orElse(LEFT);
    }
}
