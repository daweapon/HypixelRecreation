package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.List;

public class RuneComponent extends SkyBlockItemComponent {
    @Getter
    private final int requiredLevel;
    @Getter
    private final String color;
    @Getter
    private final String skullTexture;
    @Getter
    private final RuneableComponent.RuneApplicableTo applicableTo;

    public RuneComponent(int requiredLevel, String color, String applicableTo, String skullTexture) {
        this.requiredLevel = requiredLevel;
        this.color = color;
        this.applicableTo = RuneableComponent.RuneApplicableTo.valueOf(applicableTo);
        this.skullTexture = skullTexture;

        addInheritedComponent(new ExtraRarityComponent("COSMETIC"));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new ExtraUnderNameComponent(getExtraDisplay()));
        addInheritedComponent(new SkullHeadComponent((item) -> skullTexture));
        addInheritedComponent(new LoreUpdateComponent(applyLore(this.applicableTo), false));
    }

    public boolean canApplyTo(SkyBlockItem target) {
        if (!target.hasComponent(RuneableComponent.class)) return false;
        RuneableComponent.RuneApplicableTo slot = target.getComponent(RuneableComponent.class).getRuneApplicableTo();
        return switch (applicableTo) {
            case WEAPONS -> slot == RuneableComponent.RuneApplicableTo.WEAPONS;
            case BOWS -> slot == RuneableComponent.RuneApplicableTo.WEAPONS && isBow(target);
            default -> applicableTo == slot;
        };
    }

    private static boolean isBow(SkyBlockItem item) {
        if (item.hasComponent(BowComponent.class)) return true;
        return item.hasComponent(StandardItemComponent.class)
                && item.getComponent(StandardItemComponent.class).getType() == StandardItemComponent.StandardItemType.BOW;
    }

    public static List<String> applyLore(RuneableComponent.RuneApplicableTo applicableTo) {
        return List.of(
                "<7>Apply this rune to " + applicableTo.name().toLowerCase().replace('_', ' ') + " or",
                "<7>fuse two together at the Runic",
                "<7>Pedestal!"
        );
    }

    private List<String> getExtraDisplay() {
        return List.of(
                "<8>Requires level " + requiredLevel,
                StringUtility.toNormalCase(applicableTo.name())
        );
    }

    public String getDisplayName(ItemType type, int level) {
        return getColor() + "◆ " + StringUtility.toNormalCase(type.toString())
                + " " + StringUtility.getAsRomanNumeral(level);
    }
}
