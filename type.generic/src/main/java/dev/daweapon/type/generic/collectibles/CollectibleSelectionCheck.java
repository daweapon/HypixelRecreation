package dev.daweapon.type.generic.collectibles;

// Result of checking whether a collectible can currently be selected, with an optional block reason.
public record CollectibleSelectionCheck(boolean selectable, String reason) {

    public static CollectibleSelectionCheck allowed() {
        return new CollectibleSelectionCheck(true, null);
    }

    public static CollectibleSelectionCheck blocked(String reason) {
        return new CollectibleSelectionCheck(false, reason);
    }
}
