package dev.daweapon.type.skyblockgeneric.block.placement.states.state;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

// Block state values for whether a piston is normal or sticky.
public enum PistonType implements State<PistonType> {
    NORMAL, STICKY;

    @NotNull
    @Override
    public String getKey() {
        return "type";
    }

    @NotNull
    @Override
    public String getValue() {
        return name().trim().toLowerCase();
    }

    @NotNull
    @Override
    public PistonType parse(String rawValue) {
        return Stream.of(values()).filter(shape -> shape.name().equalsIgnoreCase(rawValue.trim())).findFirst().orElse(NORMAL);
    }
}
