package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.skills;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsSkillDisplay;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonsUnreleased;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class GUIDungeoneering extends StatelessView {
    public static final String MORT_SKULL_TEXTURE = "9b56895b9659896ad647f58599238af532d46db9c1b0389b8bbeb70999dab33d";
    private static final String CATACOMBS_TEXTURE = "17b8ef7e8e0af19399cc32485f2ee47b2bb4d5802c71b3021f4d5d6b5f3afe03";
    private static final String ESSENCE_BAG_TEXTURE = "1e5574a4ef94ef4ca300d02f644d98be07dbfba7df78808e8a9a021b067d996d";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Dungeoneering Skill", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(12, (s, c) -> catacombsIcon((SkyBlockPlayer) c.player(), true),
                (click, c) -> c.push(new GUICatacombsRewards(0)));
        layout.slot(13, ItemStacks.item(Material.RED_STAINED_GLASS_PANE, "<c>Not Coming Soon"));
        layout.slot(14, ItemStacks.item(Material.RED_STAINED_GLASS_PANE, "<c>Not Coming Soon"));

        DungeonsUnreleased.slot(layout, 29, Material.SPLASH_POTION, """
                <a>Healer
                <f><l>Class Passives
                <8>■<a> Renew
                <8>■<a> Healing Aura
                <8>■<a> Revive
                <8>■<a> Orbies
                <8>■<a> Soul Tether

                <f><l>Dungeon Orb Abilities
                <8>■<6> Healing Circle
                <8>■<6> Wish

                <f><l>Ghost Abilities
                <8>■<f> Healing Potion
                <8>■<f> Revive Self""");
        DungeonsUnreleased.slot(layout, 30, Material.BLAZE_ROD, """
                <a>Mage
                <7>Intelligence: <a>+250

                <f><l>Class Passives
                <8>■<a> Mage Staff
                <8>■<a> Efficient Spells

                <f><l>Dungeon Orb Abilities
                <8>■<6> Guided Sheep
                <8>■<6> Thunderstorm

                <f><l>Ghost Abilities
                <8>■<f> Pop-up Wall
                <8>■<f> Fireball""");
        DungeonsUnreleased.slot(layout, 31, Material.IRON_SWORD, """
                <a>Berserk
                <7>Melee Damage: <c>+40%
                <7>Walk Speed: <a>+30

                <f><l>Class Passives
                <8>■<a> Bloodlust
                <8>■<a> Lust for Blood

                <f><l>Dungeon Orb Abilities
                <8>■<6> Throwing Axe
                <8>■<6> Ragnarok

                <f><l>Ghost Abilities
                <8>■<f> Strength Potion
                <8>■<f> Ghost Axe""");
        DungeonsUnreleased.slot(layout, 32, Material.BOW, """
                <a>Archer
                <7>Arrow Damage: <c>+120%
                <7>Melee Damage: <c>-25%

                <f><l>Class Passives
                <8>■<a> Doubleshot
                <8>■<a> Bone Plating
                <8>■<a> Bouncy Arrows

                <f><l>Dungeon Orb Abilities
                <8>■<6> Explosive Shot
                <8>■<6> Machine Gun Bow

                <f><l>Ghost Abilities
                <8>■<f> Stun Bow
                <8>■<f> Healing Bow""");
        DungeonsUnreleased.slot(layout, 33, Material.LEATHER_CHESTPLATE, """
                <a>Tank
                <7>Health: <a>+100
                <7>Defense: <a>+50

                <f><l>Class Passives
                <8>■<a> Protective Barrier
                <8>■<a> Taunt
                <8>■<a> Diversion
                <8>■<a> Defensive Stance

                <f><l>Dungeon Orb Abilities
                <8>■<6> Seismic Wave
                <8>■<6> Castle of Stone

                <f><l>Ghost Abilities
                <8>■<f> Stun Potion
                <8>■<f> Absorption Potion""");

        layout.slot(50, ItemStacks.head(ESSENCE_BAG_TEXTURE, """
                        <a>Your Essence
                        <7>Collect Essence in Dungeons
                        <7>which can be used to upgrade
                        <7>gear and purchase perks and
                        <7>upgrades!

                        <e>Click to view!"""),
                (click, c) -> c.push(new GUIYourEssence()));
    }

    public static ItemStack.Builder catacombsIcon(SkyBlockPlayer player, boolean clickable) {
        long xp = CatacombsProgress.xp(player);
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>Complete this dungeon to earn"),
                Text.of("<7>experience and unlock new"),
                Text.of("<7>rewards!"),
                Text.empty()));
        CatacombsSkillDisplay.nextLevelProgress(xp).forEach(line -> lore.add(Text.parse(line)));
        if (clickable) {
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to view!"));
        }
        return ItemStacks.head(CATACOMBS_TEXTURE, Text.of("<c>The Catacombs <8>- <e>Level {}", CatacombsProgress.level(player)), lore);
    }

    public static ItemStack.Builder skillsMenuIcon() {
        return ItemStacks.head(MORT_SKULL_TEXTURE, """
                <a>Dungeoneering
                <7>Complete Dungeons to level up your
                <7>classes! Unlock new gear and class
                <7>upgrades by completing higher tier
                <7>dungeons!

                <7>Requires <b>Combat Level 15<7> to enter a
                <7>Dungeon.

                <e>Click to view!""");
    }
}
