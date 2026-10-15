package dev.daweapon.commons.reflection.fixtures;

public class ThrowingPlugin implements Plugin {
    public ThrowingPlugin() {
        throw new IllegalStateException("boom");
    }
}
