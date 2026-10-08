package net.swofty.type.generic.utility;

/**
 * The sidebar arrow that points from where a player is facing toward a destination: one of eight
 * arrows, green while the player faces the destination and red otherwise.
 */
public final class DirectionArrow {
    public static final String[] ARROWS = {"⬆", "⬈", "➡", "⬊", "⬇", "⬋", "⬅", "⬉"};
    public static final String HERE = "•";
    public static final double FACING_DEGREES = 22.5;
    public static final double ARRIVED_DISTANCE = 1.5;

    private DirectionArrow() {
    }

    /** Degrees the destination lies clockwise of the player's view, in (-180, 180]. */
    public static double relativeYaw(double fromX, double fromZ, double yaw, double toX, double toZ) {
        double targetYaw = Math.toDegrees(Math.atan2(-(toX - fromX), toZ - fromZ));
        return normalise(targetYaw - yaw);
    }

    public static double normalise(double degrees) {
        double result = degrees % 360;
        if (result <= -180) result += 360;
        if (result > 180) result -= 360;
        return result;
    }

    public static String arrow(double relativeYaw) {
        double clockwise = (normalise(relativeYaw) + 360) % 360;
        int index = (int) Math.floor((clockwise + 22.5) / 45) % ARROWS.length;
        return ARROWS[index];
    }

    public static boolean isFacing(double relativeYaw) {
        return Math.abs(normalise(relativeYaw)) <= FACING_DEGREES;
    }

    public static String colour(double relativeYaw) {
        return isFacing(relativeYaw) ? "<a>" : "<c>";
    }

    /** Coloured, bold arrow markup for the sidebar. */
    public static String markup(double fromX, double fromZ, double yaw, double toX, double toZ) {
        if (Math.hypot(toX - fromX, toZ - fromZ) <= ARRIVED_DISTANCE) return "<a><l>" + HERE;
        double relative = relativeYaw(fromX, fromZ, yaw, toX, toZ);
        return colour(relative) + "<l>" + arrow(relative);
    }
}
