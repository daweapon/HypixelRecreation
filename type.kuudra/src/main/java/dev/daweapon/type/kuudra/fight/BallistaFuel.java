package dev.daweapon.type.kuudra.fight;

/**
 * The Ballista's fuel: a full load of Fuel Cells charges one shot, and firing empties it again (the user's
 * 2026-10-09 retest call; the wiki only says players mount and fire once all four cells are in).
 */
public final class BallistaFuel {
    public enum Load {REJECTED, LOADED, FIRST_FULL, REFUELLED}

    private final int cells;
    private int loaded;
    private boolean armed;
    private boolean loadedOnce;

    public BallistaFuel(int cells) {
        if (cells <= 0) throw new IllegalArgumentException("The Ballista needs at least one Fuel Cell");
        this.cells = cells;
    }

    public Load load() {
        if (armed) return Load.REJECTED;
        loaded++;
        if (loaded < cells) return Load.LOADED;
        armed = true;
        if (loadedOnce) return Load.REFUELLED;
        loadedOnce = true;
        return Load.FIRST_FULL;
    }

    /** Fills it outright, as when a phase or staff command skips the fuelling. */
    public void fill() {
        loaded = cells;
        armed = true;
        loadedOnce = true;
    }

    /** Spends the load on a shot; false when there was nothing to fire. */
    public boolean fire() {
        if (!armed) return false;
        armed = false;
        loaded = 0;
        return true;
    }

    /** Cells still to fish up, after those loaded, floating in the lava and being carried. */
    public int missing(long floating, long carried) {
        return (int) Math.max(0, cells - loaded - floating - carried);
    }

    public int cells() {
        return cells;
    }

    public int loaded() {
        return loaded;
    }

    public boolean armed() {
        return armed;
    }

    public boolean everLoaded() {
        return loadedOnce;
    }
}
