package dev.daweapon.validation;

import dev.daweapon.DataField;

public class ValidationException extends RuntimeException {
    private final DataField<?> field;
    private final Object value;

    public ValidationException(String message, DataField<?> field, Object value) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public DataField<?> getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}
