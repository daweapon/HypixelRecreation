package net.swofty.type.skyblockgeneric.minion.inferno;

import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.components.MinionFuelComponent;

@Getter
public class InfernoMinionFuelComponent extends SkyBlockItemComponent {
    public static final long DURATION_MS = 24L * 60 * 60 * 1000;

    private final InfernoFuelGrade grade;
    private final ItemType specialty;
    private final int minAmount;
    private final int maxAmount;

    public InfernoMinionFuelComponent(InfernoFuelGrade grade, ItemType specialty, int minAmount, int maxAmount) {
        this.grade = grade;
        this.specialty = specialty;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        addInheritedComponent(new MinionFuelComponent(0, DURATION_MS));
    }
}
