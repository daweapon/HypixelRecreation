package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;

@Getter
public class ShortBowComponent extends BowComponent {
    private final float cooldown;
    private final int arrows;
    private final double spread;
    private final double critChanceDivisor;

    public ShortBowComponent(float cooldown, String bowHandlerId, boolean shouldBeArrow) {
        this(cooldown, bowHandlerId, shouldBeArrow, 1, 0, 1);
    }

    public ShortBowComponent(float cooldown, String bowHandlerId, boolean shouldBeArrow,
                             int arrows, double spread, double critChanceDivisor) {
        super(bowHandlerId, shouldBeArrow);

        this.cooldown = cooldown;
        this.arrows = Math.max(1, arrows);
        this.spread = spread;
        this.critChanceDivisor = Math.max(1, critChanceDivisor);
    }
}
