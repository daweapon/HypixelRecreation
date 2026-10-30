package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

public record ShaftLayout(ShaftType type, String suffix) {
    public String code() {
        return type.name() + "_" + suffix;
    }

    public boolean crystal() {
        return suffix.equals(ShaftType.CRYSTAL_SUFFIX);
    }
}
