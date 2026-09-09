package net.swofty.type.generic.data;

@SuppressWarnings("unchecked")
// Small helpers for working with raw/wildcard Datapoint references.
public final class DatapointUtils {
    private DatapointUtils() {}

    /** Set a datapoint value even if you only have Datapoint<?>. */
    public static <X> void setUnchecked(Datapoint<?> dp, Object value) {
        ((Datapoint<X>) dp).setValue((X) value);
    }
}
