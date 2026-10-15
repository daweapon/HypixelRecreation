package dev.daweapon.commons.mc;


// Engine-agnostic 3D position with yaw/pitch, independent of Minestom's Pos type, with basic vector arithmetic.
public record HypixelPosition(double x, double y, double z, float yaw, float pitch) {

    public static HypixelPosition ZERO = new HypixelPosition(0, 0, 0);

    public HypixelPosition(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public HypixelPosition sub(double x, double y, double z) {
        return new HypixelPosition(this.x - x, this.y - y, this.z - z, yaw, pitch);
    }

    public HypixelPosition sub(double value) {
        return sub(value, value, value);
    }

    public HypixelPosition sub(HypixelPosition other) {
        return sub(other.x, other.y, other.z);
    }

    public HypixelPosition add(double x, double y, double z) {
        return new HypixelPosition(this.x + x, this.y + y, this.z + z, yaw, pitch);
    }

    public HypixelPosition add(double value) {
        return add(value, value, value);
    }

    public HypixelPosition add(HypixelPosition other) {
        return add(other.x, other.y, other.z);
    }

}
