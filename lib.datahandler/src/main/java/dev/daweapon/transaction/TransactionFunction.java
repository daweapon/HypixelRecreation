package dev.daweapon.transaction;

@FunctionalInterface
public interface TransactionFunction<R> {
    R apply(Transaction tx);
}
