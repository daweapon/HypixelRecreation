package dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

public record PlacedTrainingDummy(double x, double y, double z, float yaw, TrainingDummyForm form) {
    public Pos position() {
        return new Pos(x, y, z, yaw, 0);
    }

    public PlacedTrainingDummy withForm(TrainingDummyForm newForm) {
        return new PlacedTrainingDummy(x, y, z, yaw, newForm);
    }

    public String serialize() {
        return x + "," + y + "," + z + "," + yaw + "," + form.name();
    }

    public static @Nullable PlacedTrainingDummy deserialize(String raw) {
        String[] parts = raw.split(",");
        if (parts.length != 5) return null;
        try {
            return new PlacedTrainingDummy(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]), Float.parseFloat(parts[3]), TrainingDummyForm.parse(parts[4]));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
