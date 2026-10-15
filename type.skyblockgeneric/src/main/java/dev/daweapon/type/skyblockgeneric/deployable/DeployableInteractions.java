package dev.daweapon.type.skyblockgeneric.deployable;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.ItemSkinComponent;
import dev.daweapon.type.skyblockgeneric.item.components.SkullHeadComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRequirement;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetItems;

public final class DeployableInteractions {
    public static final int DEBOUNCE_TICKS = 20;
    private static final String BUBBLEGUM = "PET_ITEM_BUBBLEGUM";
    private static final RegisteredAbility DEPLOY = new RegisteredAbility("DEPLOY", "Deploy", (_, _) -> "",
            RegisteredAbility.AbilityActivation.RIGHT_CLICK, 0, new RegisteredAbility.NoAbilityCost(),
            (_, _, _, _) -> true);

    private DeployableInteractions() {
    }

    public static boolean isDeployable(String itemId) {
        return DeployableType.ofItem(itemId) != null;
    }

    public static void use(SkyBlockPlayer player, SkyBlockItem item) {
        DeployableType type = DeployableType.ofItem(item.getAttributeHandler().getTypeAsString());
        if (type == null || player.getInstance() == null) return;
        if (!player.getAbilityHandler().canUseAbility(item, DEBOUNCE_TICKS)) return;
        player.getAbilityHandler().startAbilityCooldown(item);
        if (!meetsRequirements(player, type)) return;

        RegisteredAbility.AbilityManaCost cost = new RegisteredAbility.AbilityManaCost(
                DeployableMath.manaCost(player.getMaxMana(), DeployableType.MANA_COST_FRACTION));
        if (!cost.canUse(player)) {
            cost.onFail(player);
            return;
        }
        cost.onUse(player, DEPLOY);
        Deployables.deploy(player, type, textureOf(item), hasBubblegum(player));
    }

    public static boolean meetsRequirements(SkyBlockPlayer player, DeployableType type) {
        if (type.wolfSlayerLevel() > 0 && SlayerRequirement.level(player, SlayerType.SVEN_PACKMASTER) < type.wolfSlayerLevel()) {
            player.sendMessage("<c>You need Wolf Slayer {} to use this item!", type.wolfSlayerLevel());
            return false;
        }
        if (type.hotmTier() > 0 && HotmService.data(player).getTier() < type.hotmTier()) {
            player.sendMessage("<c>You need Heart of the Mountain Tier {} to use this item!", type.hotmTier());
            return false;
        }
        return true;
    }

    private static boolean hasBubblegum(SkyBlockPlayer player) {
        ItemType held = PetItems.heldType(player.getPetData().getEnabledPet());
        return held != null && BUBBLEGUM.equals(held.name());
    }

    private static String textureOf(SkyBlockItem item) {
        String skin = ItemSkinComponent.textureOf(item);
        if (skin != null) return skin;
        return item.hasComponent(SkullHeadComponent.class) ? item.getComponent(SkullHeadComponent.class).getSkullTexture(item) : null;
    }
}
