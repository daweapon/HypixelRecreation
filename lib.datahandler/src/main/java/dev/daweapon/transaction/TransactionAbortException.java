package dev.daweapon.transaction;

public class TransactionAbortException extends RuntimeException {
    public TransactionAbortException() {
        super("Transaction aborted");
    }
}
