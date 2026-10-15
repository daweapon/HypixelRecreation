package dev.daweapon.velocity.data;

// Common CRUD contract implemented by the proxy's document-store-backed database wrappers.
public interface MongoDB {
    MongoDB connect(String connectionString);

    void set(String key, Object value);

    Object get(String key, Object def);

    void insertOrUpdate(String key, Object value);

    boolean remove(String id);

    boolean exists();
}
