package dev.daweapon.type.skyblockgeneric.block.placement.states.state;

import org.jetbrains.annotations.NotNull;

// Contract for a single block-state property: its key, string value, and how to parse it back.
public interface State<T extends Comparable<T>> {

    @NotNull
    String getKey();

    @NotNull
    String getValue();

    @NotNull
    T parse(String rawValue);
}
