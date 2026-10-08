package net.swofty.type.skyblockgeneric.item.handlers.pet.definition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class PetNumbers {
    private static final ThreadLocal<DecimalFormat> FORMAT = ThreadLocal.withInitial(() ->
            new DecimalFormat("#,##0.####", DecimalFormatSymbols.getInstance(Locale.ROOT)));

    private PetNumbers() {
    }

    public static String format(double value) {
        double rounded = Math.abs(value) >= 1
                ? BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : BigDecimal.valueOf(value).round(new java.math.MathContext(3, RoundingMode.HALF_UP)).doubleValue();
        return FORMAT.get().format(rounded);
    }

    public static String floor(double value) {
        return FORMAT.get().format(Math.floor(value + 1e-9));
    }
}
