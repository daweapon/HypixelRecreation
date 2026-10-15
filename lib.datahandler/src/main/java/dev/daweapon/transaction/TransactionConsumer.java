package dev.daweapon.transaction;

@FunctionalInterface
public interface TransactionConsumer {
    void accept(Transaction tx);
}
