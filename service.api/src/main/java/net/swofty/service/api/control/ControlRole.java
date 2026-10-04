package net.swofty.service.api.control;

/**
 * Scoped access for the control app.
 *
 * The pre-existing admin session grants a single undifferentiated level of access, which was
 * adequate while the only administrative capability was API-key CRUD. It is not adequate for a
 * surface that can stop containers, so every control endpoint declares the role it requires.
 */
public enum ControlRole {
    /** Can see fleet state, logs and alerts. Cannot change anything. */
    READ_ONLY,
    /** Can start, stop and restart containers. */
    OPERATOR,
    /** Can additionally enrol and revoke devices and change roles. */
    OWNER;

    public boolean canRead() {
        return true;
    }

    public boolean canOperate() {
        return this == OPERATOR || this == OWNER;
    }

    public boolean canAdminister() {
        return this == OWNER;
    }

    public boolean satisfies(ControlRole required) {
        return switch (required) {
            case READ_ONLY -> canRead();
            case OPERATOR -> canOperate();
            case OWNER -> canAdminister();
        };
    }

    public static ControlRole parse(String value, ControlRole fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
