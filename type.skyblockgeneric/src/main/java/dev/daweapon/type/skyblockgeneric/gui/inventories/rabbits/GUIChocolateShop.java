package dev.daweapon.type.skyblockgeneric.gui.inventories.rabbits;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateShopStock;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GUIChocolateShop implements StatefulView<GUIChocolateShop.State> {
    // Texture IDs (extracted from skull texture URLs)
    private static final String SUPREME_CHOCOLATE_BAR_TEXTURE = "254b7f3f2a6f0d1c2c054678128ec2322619aefff0f450c390d6a41b5950302e";
    private static final String EGGLOCATOR_TEXTURE = "14a7ff9a10fdae14446499ef3bc1df13b7888d6cd2e311ccab51b8352c6093b4";
    private static final String NIBBLE_CHOCOLATE_STICK_TEXTURE = "888188d62908af6e114f73a109e15ac7f1faded39abd6a2054034ec5cc70c727";
    private static final String SMOOTH_CHOCOLATE_BAR_TEXTURE = "a9372efd2ca1a6c6dfc066f1ec83f9456575c3850a0e7d01109c4f1af300ba8";
    private static final String RICH_CHOCOLATE_CHUNK_TEXTURE = "6f942717364c0fecf7ad11bac8cd98dd7ad4dbd72e3d3ce2b57eb48713824ff";
    private static final String GANACHE_CHOCOLATE_SLAB_TEXTURE = "f89512331edfdc27cb7d4e80f3e0db460d05caf66c7c1c42e0e712130a9b690";
    private static final String PRESTIGE_CHOCOLATE_REALM_TEXTURE = "af19ceeabf2ecb020610b8aabc9299264fa670048c010c9699ce687fc9bf351e";
    private static final String DARK_CACAO_TRUFFLE_TEXTURE = "db9db373cadbec1912a9ab386d31ceb3e0cd4d6a64f222426588a3b2eb31ed29";
    private static final String CHOCOLATE_DYE_TEXTURE = "a15e7208539306f65d68df9be6c3124c48027e307739fc8dc35526febd643c21";
    private static final String BARN_SKIN_TEXTURE = "af90da40c557af4ac01d39b6733e204c74ae9fee8c2bc40be1fd4f28f837d52";
    private static final String CHOCOLATE_SYRINGE_TEXTURE = "7dcb67a72c01f3ca75da846f957ffed6417f0c45ad814fb3e340c317cf316718";
    private static final String CHOCO_RABBIT_MINION_TEXTURE = "9a815398e7da89b1bc08f646cafc8e7b813da0be0eec0cce6d3eff5207801026";
    private static final String ZORROS_CAPE_TEXTURE = "81f7226a927558d069a6ae343b4e089fbd60fc6037190097c7713208e988faae";
    private static final String FISH_CHOCOLAT_TEXTURE = "422b0e5faa97ca109cd45f1fba2d84ca2b9b601de50b47f4add2d835aa360f78";
    private static final String HOT_CHOCOLATE_MIXIN_TEXTURE = "4fde9c68bc5a89f01a5e5203eecc5367d494d55a47c81e6b1d689a0c4488b6e";
    private static final String[] FACTORY_LEVELS = {"I", "II", "III", "IV", "V", "VI"};
    private static final int[] SHARD_SLOTS = {30, 31, 32, 33};

    private static final Offer SUPREME_CHOCOLATE_BAR = Offer.item("SUPREME_CHOCOLATE_BAR", "<a>Supreme Chocolate Bar", 2_000_000L, 0);
    private static final Offer EGGLOCATOR = Offer.item("EGGLOCATOR", "<f>Egglocator", 7_500_000L, 1);
    private static final Offer NIBBLE_CHOCOLATE_STICK = Offer.item("NIBBLE_CHOCOLATE_STICK", "<f>Nibble Chocolate Stick", 250_000_000L, 1);
    private static final Offer SMOOTH_CHOCOLATE_BAR = Offer.item("SMOOTH_CHOCOLATE_BAR", "<a>Smooth Chocolate Bar", 1_000_000_000L, 1)
            .withIngredient("NIBBLE_CHOCOLATE_STICK", "<f>Nibble Chocolate Stick");
    private static final Offer RICH_CHOCOLATE_CHUNK = Offer.item("RICH_CHOCOLATE_CHUNK", "<9>Rich Chocolate Chunk", 2_000_000_000L, 2)
            .withIngredient("SMOOTH_CHOCOLATE_BAR", "<a>Smooth Chocolate Bar");
    private static final Offer GANACHE_CHOCOLATE_SLAB = Offer.item("GANACHE_CHOCOLATE_SLAB", "<5>Ganache Chocolate Slab", 3_000_000_000L, 2)
            .withIngredient("RICH_CHOCOLATE_CHUNK", "<9>Rich Chocolate Chunk");
    private static final Offer PRESTIGE_CHOCOLATE_REALM = Offer.item("PRESTIGE_CHOCOLATE_REALM", "<6>Prestige Chocolate Realm", 4_500_000_000L, 3)
            .withIngredient("GANACHE_CHOCOLATE_SLAB", "<5>Ganache Chocolate Slab");
    private static final Offer DARK_CACAO_TRUFFLE = Offer.item("DARK_CACAO_TRUFFLE", "<9>Dark Cacao Truffle", 2_500_000_000L, 3)
            .unavailable("Eating it needs the evolving Global Fortune bonus, which is not built");
    private static final Offer CHOCOLATE_DYE = Offer.item("DYE_CHOCOLATE", "<6>Chocolate Dye", 40_000_000_000L, 5);
    private static final Offer BARN_SKIN = Offer.item("CHOCOLATE_FACTORY_BARN_SKIN", "<6>Chocolate Factory Barn Skin", 7_000_000_000L, 4)
            .unavailable("Garden Barn skins are not built");
    private static final Offer CHOCOLATE_SYRINGE = Offer.item("PET_ITEM_CHOCOLATE_SYRINGE", "<d>Chocolate Syringe", 10_000_000_000L, 4);
    private static final Offer CHOCO_RABBIT_MINION_SKIN = Offer.item("CHOCO_RABBIT_PERSONALITY", "<5>Choco Rabbit Minion Skin", 2_500_000_000L, 4);
    private static final Offer ZORROS_CAPE = Offer.item("ZORROS_CAPE", "<6>Zorro's Cape", 20_000_000_000L, 4)
            .requiring(ChocolateRabbit.ZORRO);
    private static final Offer FISH_CHOCOLAT = Offer.item("STEAMED_CHOCOLATE_FISH", "<5>Fish Chocolat à la Vapeur", 50_000_000_000L, 5)
            .requiring(ChocolateRabbit.FISH_THE_RABBIT);
    private static final Offer HOT_CHOCOLATE_MIXIN = Offer.item("HOT_CHOCOLATE_MIXIN", "<9>Hot Chocolate Mixin", 1_500_000_000L, 5)
            .unavailable("Mixins on God Potions are not built");
    private static final Offer CHOCOLATE_FORTUNE = new Offer("CHOCOLATE_FORTUNE", "<e>Chocolate Fortune", 2_000_000_000L, 5, null, null, null,
            "Cocoa Beans Fortune upgrades are not built", () -> null);

    private static final List<ShardOffer> SHARD_OFFERS = List.of(
            new ShardOffer("C22", "SHARD_RABBIT_MAFIOSO", 1_000_000_000L, 1),
            new ShardOffer("U106", "SHARD_RABBIT_CAT", 4_000_000_000L, 2),
            new ShardOffer("R94", "SHARD_RABBIT_NEIGHBOR", 10_000_000_000L, 3),
            new ShardOffer("E46", "SHARD_RABBIT_GODMOTHER", 20_000_000_000L, 4));

    public record State() {
    }

    private record Offer(String key, String name, long cost, int requiredPrestige, String ingredientKey, String ingredientName,
                         ChocolateRabbit requiredRabbit, String unavailable, Supplier<SkyBlockItem> reward) {
        static Offer item(String key, String name, long cost, int requiredPrestige) {
            return new Offer(key, name, cost, requiredPrestige, null, null, null, null, () -> {
                ItemType type = ItemType.get(key);
                return type == null ? null : new SkyBlockItem(type);
            });
        }

        Offer withIngredient(String itemKey, String itemName) {
            return new Offer(key, name, cost, requiredPrestige, itemKey, itemName, requiredRabbit, unavailable, reward);
        }

        Offer requiring(ChocolateRabbit rabbit) {
            return new Offer(key, name, cost, requiredPrestige, ingredientKey, ingredientName, rabbit, unavailable, reward);
        }

        Offer unavailable(String reason) {
            return new Offer(key, name, cost, requiredPrestige, ingredientKey, ingredientName, requiredRabbit, reason, reward);
        }
    }

    private record ShardOffer(String shardId, String key, long cost, int requiredPrestige) {
        Supplier<SkyBlockItem> reward() {
            return () -> AttributeShardComponent.create(AttributeRegistry.get(shardId), 1);
        }

        Offer offer() {
            String name = AttributeRegistry.get(shardId).shardName();
            return new Offer(key, "<a>" + name + " Shard", cost, requiredPrestige, null, null, null, null, reward());
        }
    }

    @Override
    public State initialState() {
        return new State();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Chocolate Shop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        offerSlot(layout, 10, SUPREME_CHOCOLATE_BAR, SUPREME_CHOCOLATE_BAR_TEXTURE, """
                <a>Supreme Chocolate Bar
                <7>Bring <6>3,000 <7>of these to <5>Carrolyn <7>in
                <5>Scarleton <7>on the <c>Crimson Isle <7>to
                <7>permanently gain <c>+5❤ Health <7>and
                <6>+12☘ Cocoa Beans Fortune<7>.

                <a><l>UNCOMMON
</l>
                <7>Cost
                <6>{} Chocolate

                %STOCK%

                %ACTION%
                <e>Right-click for more trading options!""");

        offerSlot(layout, 11, EGGLOCATOR, EGGLOCATOR_TEXTURE, """
                <f>Egglocator
                <7>Uses the magic of <a>Hoppity <7>to
                <7>uncover hidden <a>Chocolate Rabbit
                <a>Eggs<7>.

                <6>Ability: Egglocator  <e><l>RIGHT CLICK</l>
                <7>Points towards the nearest unclaimed
                <a>Chocolate Rabbit Egg<7>!
                <8>Cooldown: <a>5s

                <7>Only works during <d>Hoppity's Hunt<7>.

                <f><l>COMMON
</l>
                <7>Cost
                <6>{} Chocolate

                %STOCK%

                %ACTION%""");

        offerSlot(layout, 12, NIBBLE_CHOCOLATE_STICK, NIBBLE_CHOCOLATE_STICK_TEXTURE, """
                <f>Nibble Chocolate Stick
                <7>Grants <a>+2% <7>chance to find a
                <a>Chocolate Rabbit <7>that you haven't
                <7>found yet and grants <6>+10 Chocolate
                <7>per second.

                <8><o>A delightful treat from the Factory.</o>
                <8><o>Its crisp taste sparks joy with</o>
                <8><o>every bite.
</o>
                <f><l>COMMON ACCESSORY
</l>
                <7>Cost
                <6>{} Chocolate

                %ACTION%""");

        offerSlot(layout, 13, SMOOTH_CHOCOLATE_BAR, SMOOTH_CHOCOLATE_BAR_TEXTURE, """
                <a>Smooth Chocolate Bar
                <7>Grants <a>+4% <7>chance to find a
                <a>Chocolate Rabbit <7>that you haven't
                <7>found yet and grants <6>+20 Chocolate
                <7>per second.

                <8><o>Crafted in the Factory, its</o>
                <8><o>smoothness melts hearts and tastes</o>
                <8><o>like a sweet escape.
</o>
                <a><l>UNCOMMON ACCESSORY
</l>
                <7>Cost
                <6>{} Chocolate
                %INGREDIENT%

                %ACTION%""");

        offerSlot(layout, 14, RICH_CHOCOLATE_CHUNK, RICH_CHOCOLATE_CHUNK_TEXTURE, """
                <9>Rich Chocolate Chunk
                <7>Grants <a>+6% <7>chance to find a
                <a>Chocolate Rabbit <7>that you haven't
                <7>found yet and grants <6>+30 Chocolate
                <7>per second.

                <8><o>From the Factory's secret</o>
                <8><o>reserves, its rich flavor is a deep</o>
                <8><o>dive into indulgence.
</o>
                <9><l>RARE ACCESSORY
</l>
                <7>Cost
                <6>{} Chocolate
                %INGREDIENT%

                %ACTION%""");

        offerSlot(layout, 15, GANACHE_CHOCOLATE_SLAB, GANACHE_CHOCOLATE_SLAB_TEXTURE, """
                <5>Ganache Chocolate Slab
                <7>Grants <a>+8% <7>chance to find a
                <a>Chocolate Rabbit <7>that you haven't
                <7>found yet and grants <6>+40 Chocolate
                <7>per second.

                <8><o>A Factory masterpiece - its divine</o>
                <8><o>taste transcends reality, offering a</o>
                <8><o>heavenly escape.
</o>
                <5><l>EPIC ACCESSORY
</l>
                <7>Cost
                <6>{} Chocolate
                %INGREDIENT%

                %ACTION%""");

        offerSlot(layout, 16, PRESTIGE_CHOCOLATE_REALM, PRESTIGE_CHOCOLATE_REALM_TEXTURE, """
                <6>Prestige Chocolate Realm
                <7>Grants <a>+10% <7>chance to find a
                <a>Chocolate Rabbit <7>that you haven't
                <7>found yet and grants <6>+50 Chocolate
                <7>per second.

                <8><o>The Factory's pinnacle creation - its</o>
                <8><o>epic taste shatters expectations,</o>
                <8><o>offering a taste of utopia.
</o>
                <6><l>LEGENDARY ACCESSORY
</l>
                <7>Cost
                <6>{} Chocolate
                %INGREDIENT%

                %ACTION%""");

        offerSlot(layout, 19, DARK_CACAO_TRUFFLE, DARK_CACAO_TRUFFLE_TEXTURE, """
                <9>Dark Cacao Truffle
                <7>Consume to boost your <6>☘ Global
                <6>Fortune <7>for <a>60m<7>.

                <7>Keep this item in your inventory to
                <7>increase the bonus up to <6>+30<6>☘
                <6>Global Fortune<7>, at which point the
                <7>item <c><o>evolves<7>!
</o>
                <7>Current Bonus: <6>+0<6>☘ Global Fortune

                <9><l>RARE
</l>
                <7>Cost
                <6>{} Chocolate

                %STOCK%

                %ACTION%""");

        offerSlot(layout, 20, CHOCOLATE_DYE, CHOCOLATE_DYE_TEXTURE, """
                <6>Chocolate Dye
                <8>Combinable in Anvil

                <7>Changes the color of an armor piece
                <7>to <6>#7B3F00<7>!

                <5><l>EPIC DYE
</l>
                <7>Cost
                <6>{} Chocolate

                %STOCK%

                %ACTION%""");

        offerSlot(layout, 21, BARN_SKIN, BARN_SKIN_TEXTURE, """
                <6>Chocolate Factory Barn Skin
                <7>Consume this item to unlock the
                <6>Chocolate Factory Barn Skin <7>on <a>The
                <a>Garden<7>!

                <e>Click to consume!

                <6><l>LEGENDARY COSMETIC
</l>
                <7>Cost
                <6>{} Chocolate

                %ACTION%""");

        offerSlot(layout, 22, CHOCOLATE_SYRINGE, CHOCOLATE_SYRINGE_TEXTURE, """
                <d>Chocolate Syringe
                <7>Use at <b>Kat <7>to upgrade <e>Rabbit Pets <7>to
                <d>Mythic <7>rarity.

                <d><l>MYTHIC
</l>
                <7>Cost
                <6>{} Chocolate

                %ACTION%""");

        offerSlot(layout, 23, CHOCO_RABBIT_MINION_SKIN, CHOCO_RABBIT_MINION_TEXTURE, """
                <5>Choco Rabbit Minion Skin
                <7>This Minion skin changes your
                <7>minion's appearance to a <e>Choco
                <e>Rabbit<7>.

                <7>You can place this item in any minion
                <7>of your choice!

                <5><l>EPIC COSMETIC
</l>
                <7>Cost
                <6>{} Chocolate

                %STOCK%

                %ACTION%""");

        offerSlot(layout, 24, ZORROS_CAPE, ZORROS_CAPE_TEXTURE, """
                <6>Zorro's Cape
                <7>Strength: <c>+10
                <7>Ferocity: <c>+2
                <7>Farming Fortune: <6>+10
                <7>Farming Wisdom: <3>+1

                <7>The stats of this Cape <a>double\s
                <7>during <e>Jacob's Farming Contest<7>.
                <7>Additionally, you have a <a>20% <7>chance
                <7>to obtain an extra medal from
                <7>contests.

                <8><o>Not all Rabbits wear capes.
</o>
                <8>This item can be reforged!
                <4>❣ <c>Requires <d>Zorro <c>in Hoppity's Collection<c>.
                <6><l>LEGENDARY CLOAK
</l>
                <7>Cost
                <6>{} Chocolate

                %ACTION%""");

        offerSlot(layout, 25, FISH_CHOCOLAT, FISH_CHOCOLAT_TEXTURE, """
                <5>Fish Chocolat à la Vapeur
                <7>Give this dish to <a>Hoppity <7>to obtain his
                <a>Abiphone Contact<7>.

                <8><o>Savory fish with a chocolate twist.</o>
                <8><o>Mwah! C'est magnifique, no?
</o>
                <5><l>EPIC
</l>
                <7>Cost
                <6>{} Chocolate
                %REQUIREMENT%

                %STOCK%

                %ACTION%""");

        offerSlot(layout, 28, HOT_CHOCOLATE_MIXIN, HOT_CHOCOLATE_MIXIN_TEXTURE, """
                <9>Hot Chocolate Mixin
                <8>Brewing Ingredient

                <7>Mixins provide a buff that can be
                <7>added to <c>God Potions <7>in a brewing
                <7>stand and lasts for the full duration.

                <7>Gain <d>+15♣ Pet Luck <7>and <6>+0.05x
                <6>Chocolate <7>per second.

                <7>Duration: <a>36h 0m

                <7>The duration of Mixins can be stacked!

                <e>Right-click to consume!
                <8>(Requires active Booster Cookie)

                <9><l>RARE
</l>
                <7>Cost
                <6>{} Chocolate

                %STOCK%

                %ACTION%""");

        offerSlot(layout, 29, CHOCOLATE_FORTUNE, Material.COCOA_BEANS, """
                <e>Chocolate Fortune
                <7>Permanently gain <6>+1☘ Cocoa Beans
                <6>Fortune <7>per tier.

                <7>Cost
                <6>{} Chocolate

                %STOCK%

                %ACTION%""");

        for (int index = 0; index < SHARD_OFFERS.size(); index++) {
            shardSlot(layout, SHARD_SLOTS[index], SHARD_OFFERS.get(index));
        }

        Components.back(layout, 48, ctx);

        Components.close(layout, 49);

        layout.slot(50, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(p);

            return ItemStacks.item(Material.LADDER, """
                    <6>Chocolate Shop Milestones
                    <7>Unlock special <a>Chocolate Rabbits <7>by
                    <7>spending <6>Chocolate <7>in the <6>Chocolate
                    <6>Shop<7>.

                    <7>Chocolate Spent: <6>{}

                    <e>Click to view!""",
                    ChocolateFactoryHelper.formatChocolate(data.getTotalChocolateSpent()));
        }, (click, c) -> ((SkyBlockPlayer) c.player()).openView(new GUIChocolateShopMilestones()));
    }

    private void offerSlot(ViewLayout<State> layout, int slot, Offer offer, String texture, String template) {
        layout.slot(slot, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            if (isLocked(p, offer)) return createLockedItem(lockedText(offer));
            return ItemStacks.head(texture, fill(p, offer, template), ChocolateFactoryHelper.formatChocolate(offer.cost()));
        }, (click, c) -> purchase((SkyBlockPlayer) c.player(), offer, c));
    }

    private void offerSlot(ViewLayout<State> layout, int slot, Offer offer, Material material, String template) {
        layout.slot(slot, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            if (isLocked(p, offer)) return createLockedItem(lockedText(offer));
            return ItemStacks.item(material, fill(p, offer, template), ChocolateFactoryHelper.formatChocolate(offer.cost()));
        }, (click, c) -> purchase((SkyBlockPlayer) c.player(), offer, c));
    }

    private void shardSlot(ViewLayout<State> layout, int slot, ShardOffer shard) {
        Offer offer = shard.offer();
        layout.slot(slot, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            if (isLocked(p, offer)) return createLockedItem(lockedText(offer));
            List<Text> lore = new ArrayList<>();
            lore.add(Text.empty());
            lore.add(Text.of("<7>Cost"));
            lore.add(Text.of("<6>{} Chocolate", ChocolateFactoryHelper.formatChocolate(offer.cost())));
            lore.add(Text.empty());
            for (String line : stockMarkup(p, offer).split("\n")) lore.add(Text.of(line));
            lore.add(Text.empty());
            lore.add(Text.of(actionMarkup(p, offer)));
            return ItemStacks.appendLore(shard.reward().get().getItemStackBuilder(), lore);
        }, (click, c) -> purchase((SkyBlockPlayer) c.player(), offer, c));
    }

    private boolean isLocked(SkyBlockPlayer player, Offer offer) {
        return ChocolateFactoryHelper.getData(player).getPrestigeLevel() < offer.requiredPrestige();
    }

    private String lockedText(Offer offer) {
        return "<c>Chocolate Factory " + FACTORY_LEVELS[offer.requiredPrestige()] + ".";
    }

    private String fill(SkyBlockPlayer player, Offer offer, String template) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        String requirement = "";
        if (offer.requiredRabbit() != null) {
            requirement = (data.hasFoundRabbit(offer.requiredRabbit()) ? "<a>" : "<c>") + offer.requiredRabbit().getDisplayName();
        }
        String ingredient = offer.ingredientName() == null ? "" : offer.ingredientName();
        return template
                .replace("%STOCK%", stockMarkup(player, offer))
                .replace("%ACTION%", actionMarkup(player, offer))
                .replace("%INGREDIENT%", ingredient)
                .replace("%REQUIREMENT%", requirement);
    }

    private String stockMarkup(SkyBlockPlayer player, Offer offer) {
        if (!ChocolateShopStock.isLimited(offer.key())) return "<7>Annual Stock <8>Unlimited";
        int year = SkyBlockCalendar.getYear();
        int remaining = ChocolateFactoryHelper.getData(player).shopRemaining(offer.key(), year);
        String second = remaining <= 0 ? "<c>Sold out" : "<6>" + remaining + " <7>remaining";
        return "<7>Annual Stock <8>Year " + year + "\n" + second;
    }

    private String actionMarkup(SkyBlockPlayer player, Offer offer) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        if (offer.unavailable() != null) return "<c>Not available yet: " + offer.unavailable();
        if (offer.requiredRabbit() != null && !data.hasFoundRabbit(offer.requiredRabbit())) return "<c>Not unlocked!";
        if (data.shopRemaining(offer.key(), SkyBlockCalendar.getYear()) <= 0) return "<c>Sold out for this SkyBlock year!";
        return "<e>Click to trade!";
    }

    private ItemStack.Builder createLockedItem(String requirement) {
        return ItemStacks.item(Material.GRAY_DYE, 1, Text.of("<c>???"), List.of(
                Text.of("<7>???"),
                Text.empty(),
                Text.of(requirement)));
    }

    private void purchase(SkyBlockPlayer player, Offer offer, ViewContext c) {
        if (offer.unavailable() != null) {
            player.sendMessage("<c>This item is not available yet: {}.", offer.unavailable());
            return;
        }

        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        if (data.getPrestigeLevel() < offer.requiredPrestige()) {
            player.sendMessage("<c>You don't meet the requirements for this item!");
            return;
        }

        int year = SkyBlockCalendar.getYear();
        if (data.shopRemaining(offer.key(), year) <= 0) {
            player.sendMessage("<c>This item is sold out for this SkyBlock year!");
            return;
        }

        if (offer.requiredRabbit() != null && !data.hasFoundRabbit(offer.requiredRabbit())) {
            player.sendMessage("<c>You need {} in Hoppity's Collection to buy this!", offer.requiredRabbit().getDisplayName());
            return;
        }

        SkyBlockItem reward = offer.reward().get();
        if (reward == null) {
            player.sendMessage("<c>This item is not available yet!");
            return;
        }

        ItemType ingredient = offer.ingredientKey() == null ? null : ItemType.get(offer.ingredientKey());
        if (ingredient != null && player.getAmountInInventory(ingredient) < 1) {
            player.sendMessage("<c>You need {} <c>in your inventory to buy this!", Text.of(offer.ingredientName()));
            return;
        }

        if (data.getChocolate() < offer.cost()) {
            player.sendMessage("<c>You don't have enough Chocolate!");
            return;
        }

        if (ingredient != null) player.removeItemFromPlayer(ingredient, 1);
        ChocolateFactoryHelper.mutateData(player, d -> {
            d.removeChocolate(offer.cost());
            d.addChocolateSpent(offer.cost());
            d.recordShopPurchase(offer.key(), year);
        });
        player.addAndUpdateItem(reward);
        player.sendMessage("<a>Purchased {} <a>for <6>{} Chocolate<a>!", Text.of(offer.name()),
                ChocolateFactoryHelper.formatChocolate(offer.cost()));
        c.session(State.class).refresh();
    }

}
