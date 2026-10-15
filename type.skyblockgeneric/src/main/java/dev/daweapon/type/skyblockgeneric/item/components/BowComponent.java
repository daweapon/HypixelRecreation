package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.PotatoType;
import dev.daweapon.commons.skyblock.item.reforge.ReforgeType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.bow.BowHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.bow.BowRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.List;

@Getter
public class BowComponent extends SkyBlockItemComponent {
    private final BowHandler shootHandler;
    private final boolean shouldBeArrow;

    public BowComponent(BowHandler shootHandler, boolean shouldBeArrow) {
        this.shootHandler = shootHandler;
        this.shouldBeArrow = shouldBeArrow;
        addInheritedComponent(new ExtraRarityComponent("BOW"));
        addInheritedComponent(new QuiverDisplayComponent(shouldBeArrow));
        addInheritedComponent(new ReforgableComponent(ReforgeType.BOWS));
        addInheritedComponent(new EnchantableComponent(List.of(EnchantItemGroups.BOW), true));
        addInheritedComponent(new RuneableComponent(RuneableComponent.RuneApplicableTo.BOWS));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new HotPotatoableComponent(PotatoType.WEAPONS));
    }

    public BowComponent(String handlerId, boolean shouldBeArrow) {
        this(BowRegistry.getHandler(handlerId), shouldBeArrow);
    }

    public void onBowShoot(SkyBlockPlayer player, SkyBlockItem item, double power) {
        if (shootHandler != null) {
            shootHandler.onShoot(player, item, power);
        }
    }
}