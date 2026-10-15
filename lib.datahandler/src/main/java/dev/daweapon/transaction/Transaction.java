package dev.daweapon.transaction;

import dev.daweapon.LinkedField;
import dev.daweapon.PlayerField;

import java.util.function.UnaryOperator;

public interface Transaction {
    <T> T get(PlayerField<T> field);
    <T> void set(PlayerField<T> field, T value);
    <T> void update(PlayerField<T> field, UnaryOperator<T> updater);

    <K, T> T get(LinkedField<K, T> field);
    <K, T> void set(LinkedField<K, T> field, T value);
    <K, T> void update(LinkedField<K, T> field, UnaryOperator<T> updater);

    void abort();
}
