package dev.daweapon.api;

import dev.daweapon.DataField;
import dev.daweapon.LinkedField;
import dev.daweapon.PlayerField;
import dev.daweapon.validation.ValidationException;
import dev.daweapon.validation.ValidationResult;
import dev.daweapon.validation.Validator;

final class Validation {
    private Validation() {}

    @SuppressWarnings("unchecked")
    public static <T> void validate(DataField<T> field, T value) {
        Validator<T> validator = getValidator(field);
        if (validator != null && value != null) {
            ValidationResult result = validator.validate(value);
            if (!result.isValid()) {
                throw new ValidationException(result.error(), field, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Validator<T> getValidator(DataField<T> field) {
        if (field instanceof PlayerField<?> pf) return ((PlayerField<T>) pf).validator();
        if (field instanceof LinkedField<?, ?> lf) return ((LinkedField<?, T>) lf).validator();
        return null;
    }
}
