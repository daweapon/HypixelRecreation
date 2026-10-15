package dev.daweapon.type.generic.data;

// Marks a Datapoint value type that reads/writes its own storage instead of the default account-field persistence.
public interface BackedField {
    String readData(DataHandler handler);

    void writeData(DataHandler handler, String serialized);
}
