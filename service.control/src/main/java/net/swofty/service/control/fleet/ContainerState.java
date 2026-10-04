package net.swofty.service.control.fleet;

/**
 * Short status code shown beside the colour in every UI surface.
 *
 * Status is deliberately always a colour AND a text code so it never depends on colour alone.
 */
public enum ContainerState {
    /** Running, and either healthy or declaring no healthcheck. */
    UP,
    /** Running but not yet healthy, or healthcheck reporting unhealthy. */
    WRN,
    /** Docker is cycling the container - the retry-loop case that drives uniform high CPU. */
    LOOP,
    /** Stopped after having run. The two-week Mongo/Redis outage looked exactly like this. */
    EXIT,
    /** Created, paused, dead, or absent entirely. */
    DWN;

    public boolean isNominal() {
        return this == UP;
    }

    public boolean isCritical() {
        return this == EXIT || this == DWN || this == LOOP;
    }
}
