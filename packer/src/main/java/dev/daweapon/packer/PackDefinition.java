package dev.daweapon.packer;

// Describes a resource pack to build: its display name and the source directory it is built from.
public interface PackDefinition {
    String getPackName();
    String getPackDirectory();
}
