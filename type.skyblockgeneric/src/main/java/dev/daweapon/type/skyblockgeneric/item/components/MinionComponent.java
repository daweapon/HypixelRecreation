package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.crafting.ShapedRecipe;
import dev.daweapon.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.minion.MinionIngredient;
import dev.daweapon.type.skyblockgeneric.minion.MinionRecipe;
import dev.daweapon.type.skyblockgeneric.minion.MinionRegistry;
import dev.daweapon.type.skyblockgeneric.minion.MinionReputationGates;
import dev.daweapon.type.skyblockgeneric.minion.MinionTierRecipe;
import dev.daweapon.type.skyblockgeneric.minion.SkyBlockMinion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MinionComponent extends SkyBlockItemComponent {
    private MinionRegistry minionRegistry;
    private ItemType baseItem;

    public MinionComponent(String minionType, String baseItem,
                           boolean isByDefaultCraftable, List<MinionIngredient> ingredients) {
        this(minionType, baseItem, isByDefaultCraftable, ingredients, Map.of());
    }

    public MinionComponent(String minionType, String baseItem, boolean isByDefaultCraftable,
                           List<MinionIngredient> ingredients, Map<Integer, MinionTierRecipe> tierRecipes) {
        this.minionRegistry = MinionRegistry.valueOf(minionType);
        this.baseItem = ItemType.get(baseItem);

        addInheritedComponent(new SkullHeadComponent((item -> {
            int tier = item.getAttributeHandler().getMinionData().tier();

            return minionRegistry.asSkyBlockMinion().getTiers().get(tier - 1).texture();
        })));
        addInheritedComponent(new PlaceEventComponent("minion"));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new LoreUpdateComponent(
                new LoreConfig((item, player) -> {
                    boolean mithrilInfusion = item.getAttributeHandler().isMithrilInfused();

                    int percentageSpeed = 0;
                    if (mithrilInfusion)
                        percentageSpeed += 10;

                    return getLore(item, percentageSpeed);
                }, (item, player) -> Text.of("<9>{} {:roman}", getMinionRegistry().getDisplay(),
                        item.getAttributeHandler().getMinionData().tier()).serialize()), true
        ));

        List<SkyBlockRecipe<?>> toReturn = new ArrayList<>();

        getMinionRegistry().asSkyBlockMinion().getTiers().forEach(tier -> {
            if (!tier.craftable()) return; // Skip non-craftable tiers

            SkyBlockItem item = new SkyBlockItem(getMinionRegistry().getItemType());
            item.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(tier.tier(), 0));

            MinionTierRecipe custom = tierRecipes.get(tier.tier());
            ShapedRecipe recipe = custom != null
                    ? customRecipe(item, custom)
                    : new ShapedRecipe(
                    SkyBlockRecipe.RecipeType.MINION,
                    item,
                    MinionRecipe.fromNumber(tier.tier() - 1).getRecipeFunction().apply(new MinionRecipe.MinionRecipeData(
                            ingredients,
                            this.baseItem,
                            getMinionRegistry().getItemType()
                    )),
                    new ArrayList<>(Arrays.asList("AAA", "ABA", "AAA"))
            );
            if (MinionReputationGates.factionFor(getMinionRegistry()) != null) {
                recipe.setCanCraft(player -> MinionReputationGates.check(player, getMinionRegistry(), tier.tier()));
            }
            recipe.addExtraRequirement('B', (minionItem) -> {
                if (minionItem.hasComponent(MinionComponent.class))
                    return minionItem.getAttributeHandler().getMinionData().tier() == Math.max(1, tier.tier() - 1);
                return true;
            });

            toReturn.add(recipe);
        });

        addInheritedComponent(new CraftableComponent(toReturn.toArray(new SkyBlockRecipe[0]), isByDefaultCraftable));
    }

    private ShapedRecipe customRecipe(SkyBlockItem result, MinionTierRecipe custom) {
        Map<Character, ItemQuantifiable> ingredientMap = new HashMap<>();
        custom.ingredients().forEach((symbol, ingredient) ->
                ingredientMap.put(symbol, new ItemQuantifiable(ingredient.getItem(), ingredient.getAmount())));
        ingredientMap.put('B', minionOfTier(custom.tier() - 1));
        custom.minionTiers().forEach((symbol, minionTier) -> ingredientMap.put(symbol, minionOfTier(minionTier)));

        ShapedRecipe recipe = new ShapedRecipe(SkyBlockRecipe.RecipeType.MINION, result, ingredientMap, custom.pattern());
        custom.minionTiers().forEach((symbol, minionTier) -> recipe.addExtraRequirement(symbol, minionItem ->
                !minionItem.hasComponent(MinionComponent.class)
                        || minionItem.getAttributeHandler().getMinionData().tier() == minionTier));
        return recipe;
    }

    private ItemQuantifiable minionOfTier(int tier) {
        SkyBlockItem minion = new SkyBlockItem(getMinionRegistry().getItemType());
        minion.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(tier, 0));
        return new ItemQuantifiable(minion, 1);
    }

    public static List<String> getLore(SkyBlockItem item, int percentageSpeed) {
        return getLore(item, 1. + percentageSpeed / 100.);
    }

    public static List<String> getLore(SkyBlockItem item, double speedDivisor) {
        MinionRegistry minionRegistry = item.getComponent(MinionComponent.class).getMinionRegistry();

        SkyBlockMinion minion = item.getAttributeHandler().getMinionType().asSkyBlockMinion();
        ItemAttributeMinionData.MinionData data = item.getAttributeHandler().getMinionData();
        SkyBlockMinion.MinionTier tier = minion.getTiers().get(data.tier() - 1);

        List<String> lore = new ArrayList<>(minion.getDescription(StringUtility.toNormalCase(minionRegistry.name())));
        lore.add("");
        List<String> abilities = minion.getAbilityLore(data.tier());
        if (!abilities.isEmpty()) {
            lore.addAll(abilities);
            lore.add("");
        }

        double timeBetweenActions = tier.timeBetweenActions() / speedDivisor;

        final DecimalFormat formattter = new DecimalFormat("#.##");

        lore.add("<7>Time Between Actions: <a>" + formattter.format(timeBetweenActions) + "s");
        lore.add("<7>Max Storage: <e>" + tier.storage());
        lore.add("<7>Resources Generated: <b>" + data.generatedResources());

        String reputation = MinionReputationGates.requirementLine(minionRegistry, data.tier());
        if (reputation != null) {
            lore.add(" ");
            lore.add(reputation);
        }
        lore.add(" ");
        lore.add("<9><l>RARE");
        return lore;
    }
}
